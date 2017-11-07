package software.wings.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static software.wings.service.impl.security.KmsServiceImpl.SECRET_MASK;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mongodb.morphia.query.Query;
import software.wings.WingsBaseTest;
import software.wings.annotation.Encryptable;
import software.wings.api.KmsTransitionEvent;
import software.wings.beans.Activity;
import software.wings.beans.AppDynamicsConfig;
import software.wings.beans.ConfigFile;
import software.wings.beans.ConfigFile.ConfigOverrideType;
import software.wings.beans.DelegateTask.SyncTaskContext;
import software.wings.beans.EntityType;
import software.wings.beans.ErrorCode;
import software.wings.beans.FeatureFlag;
import software.wings.beans.FeatureName;
import software.wings.beans.KmsConfig;
import software.wings.beans.Service;
import software.wings.beans.ServiceTemplate;
import software.wings.beans.ServiceVariable;
import software.wings.beans.ServiceVariable.OverrideType;
import software.wings.beans.ServiceVariable.Type;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.Category;
import software.wings.beans.User;
import software.wings.beans.UuidAware;
import software.wings.beans.VaultConfig;
import software.wings.beans.Workflow.WorkflowBuilder;
import software.wings.core.queue.Queue;
import software.wings.delegatetasks.DelegateProxyFactory;
import software.wings.dl.PageRequest.Builder;
import software.wings.dl.PageResponse;
import software.wings.dl.WingsPersistence;
import software.wings.exception.WingsException;
import software.wings.rules.RealMongo;
import software.wings.security.EncryptionType;
import software.wings.security.UserThreadLocal;
import software.wings.security.encryption.EncryptedData;
import software.wings.security.encryption.SecretChangeLog;
import software.wings.security.encryption.SecretUsageLog;
import software.wings.service.impl.security.KmsServiceImpl;
import software.wings.service.impl.security.KmsTransitionEventListener;
import software.wings.service.impl.security.SecretManagementDelegateServiceImpl;
import software.wings.service.intfc.ConfigService;
import software.wings.service.intfc.security.EncryptionService;
import software.wings.service.intfc.security.KmsService;
import software.wings.service.intfc.security.SecretManager;
import software.wings.service.intfc.security.VaultService;
import software.wings.settings.SettingValue.SettingVariableTypes;
import software.wings.utils.BoundedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/**
 * Created by rsingh on 11/3/17.
 */
@RunWith(Parameterized.class)
public class VaultTest extends WingsBaseTest {
  private static String VAULT_TOKEN = System.getProperty("vault.token");
  static {
    System.out.println("VAULT TOKEN: " + VAULT_TOKEN);
  }

  private final int numOfEncryptedValsForKms = 3;
  private final int numOfEncryptedValsForVault = 1;
  private int numOfEncRecords;
  @Parameter public boolean isKmsEnabled;
  @Inject private VaultService vaultService;
  @Inject private KmsService kmsService;
  @Inject private SecretManager secretManager;
  @Inject private WingsPersistence wingsPersistence;
  @Inject private ConfigService configService;
  @Inject private EncryptionService encryptionService;
  @Inject private Queue<KmsTransitionEvent> transitionKmsQueue;
  @Mock private DelegateProxyFactory delegateProxyFactory;
  private final String userEmail = "rsingh@harness.io";
  private final String userName = "raghu";
  private final User user = User.Builder.anUser().withEmail(userEmail).withName(userName).build();
  private String accountId;
  private String appId;
  private String workflowId;
  private String workflowName;
  private KmsTransitionEventListener transitionEventListener;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{true}, {false}});
  }

  @Before
  public void setup() {
    initMocks(this);
    appId = UUID.randomUUID().toString();
    workflowName = UUID.randomUUID().toString();
    workflowId = wingsPersistence.save(WorkflowBuilder.aWorkflow().withName(workflowName).build());
    when(delegateProxyFactory.get(anyObject(), any(SyncTaskContext.class)))
        .thenReturn(new SecretManagementDelegateServiceImpl());
    setInternalState(vaultService, "delegateProxyFactory", delegateProxyFactory);
    setInternalState(kmsService, "delegateProxyFactory", delegateProxyFactory);
    setInternalState(secretManager, "kmsService", kmsService);
    setInternalState(wingsPersistence, "secretManager", secretManager);
    setInternalState(vaultService, "kmsService", kmsService);
    setInternalState(secretManager, "vaultService", vaultService);
    setInternalState(configService, "secretManager", secretManager);
    wingsPersistence.save(user);
    UserThreadLocal.set(user);

    accountId = UUID.randomUUID().toString();
    enableKmsFeatureFlag();
    numOfEncRecords = numOfEncryptedValsForVault;
    if (isKmsEnabled) {
      final KmsConfig kmsConfig = getKmsConfig();
      kmsService.saveKmsConfig(accountId, kmsConfig);
      numOfEncRecords = numOfEncryptedValsForKms + numOfEncryptedValsForVault;
    }
  }

  @Test
  public void invalidConfig() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultConfig.setAuthToken(UUID.randomUUID().toString());
    vaultConfig.setAccountId(accountId);

    try {
      vaultService.saveVaultConfig(accountId, vaultConfig);
      fail("Saved invalid vault config");
    } catch (WingsException e) {
      assertEquals(ErrorCode.VAULT_OPERATION_ERROR, e.getResponseMessageList().get(0).getCode());
    }
  }

  @Test
  public void saveConfig() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);
    vaultConfig.setDefault(false);

    Collection<VaultConfig> vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(1, vaultConfigs.size());
    VaultConfig next = vaultConfigs.iterator().next();

    assertEquals(accountId, next.getAccountId());
    assertEquals(SECRET_MASK, String.valueOf(next.getAuthToken()));
    assertEquals(vaultConfig.getName(), next.getName());
    assertEquals(vaultConfig.getVaultUrl(), next.getVaultUrl());
    assertTrue(next.isDefault());
  }

  @Test
  public void saveConfigDefault() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    Collection<VaultConfig> vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(1, vaultConfigs.size());
    VaultConfig next = vaultConfigs.iterator().next();
    assertTrue(next.isDefault());

    vaultConfig = getVaultConfig();
    vaultConfig.setName("config1");
    vaultConfig.setDefault(true);
    vaultService.saveVaultConfig(accountId, vaultConfig);

    vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(2, vaultConfigs.size());

    int numOfDefault = 0;
    int numOfNonDefault = 0;

    for (VaultConfig config : vaultConfigs) {
      if (config.getName().equals(getVaultConfig().getName())) {
        assertFalse(config.isDefault());
        numOfNonDefault++;
      }

      if (config.getName().equals("config1")) {
        assertTrue(config.isDefault());
        numOfDefault++;
      }
    }

    assertEquals(1, numOfDefault);
    assertEquals(1, numOfNonDefault);

    vaultConfig = getVaultConfig();
    vaultConfig.setName("config2");
    vaultConfig.setDefault(true);
    vaultService.saveVaultConfig(accountId, vaultConfig);

    vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(3, vaultConfigs.size());

    for (VaultConfig config : vaultConfigs) {
      if (config.getName().equals(getVaultConfig().getName()) || config.getName().equals("config1")) {
        assertFalse(config.isDefault());
        numOfNonDefault++;
      }

      if (config.getName().equals("config2")) {
        assertTrue(config.isDefault());
        numOfDefault++;
      }
    }
  }

  @Test
  public void getConfigDefault() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    Collection<VaultConfig> vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(1, vaultConfigs.size());
    VaultConfig next = vaultConfigs.iterator().next();
    assertTrue(next.isDefault());

    vaultConfig = getVaultConfig();
    vaultConfig.setName("config1");
    vaultConfig.setDefault(true);
    vaultService.saveVaultConfig(accountId, vaultConfig);

    vaultConfig = getVaultConfig();
    vaultConfig.setName("config2");
    vaultConfig.setDefault(false);
    vaultService.saveVaultConfig(accountId, vaultConfig);

    vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(3, vaultConfigs.size());

    VaultConfig defaultConfig = vaultService.getSecretConfig(accountId);
    assertNotNull(defaultConfig);

    assertEquals(accountId, defaultConfig.getAccountId());
    assertEquals(VAULT_TOKEN, String.valueOf(defaultConfig.getAuthToken()));
    assertEquals("config1", defaultConfig.getName());
    assertEquals(vaultConfig.getVaultUrl(), defaultConfig.getVaultUrl());
    assertTrue(defaultConfig.isDefault());
  }

  @Test
  public void vaultNullEncryption() throws Exception {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);
    final String keyToEncrypt = null;
    final String name = "password";
    final String password = UUID.randomUUID().toString();
    EncryptedData encryptedData =
        vaultService.encrypt(name, keyToEncrypt, accountId, SettingVariableTypes.APP_DYNAMICS, vaultConfig, null);
    assertNull(encryptedData.getEncryptedValue());
    assertNotNull(encryptedData.getEncryptionKey());
    assertFalse(StringUtils.isBlank(encryptedData.getEncryptionKey()));

    char[] decryptedValue = vaultService.decrypt(encryptedData, accountId, vaultConfig);
    assertNull(decryptedValue);

    final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                    .accountId(accountId)
                                                    .controllerUrl(UUID.randomUUID().toString())
                                                    .username(UUID.randomUUID().toString())
                                                    .password(password.toCharArray())
                                                    .accountname(UUID.randomUUID().toString())
                                                    .build();

    SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                            .withAccountId(accountId)
                                            .withValue(appDynamicsConfig)
                                            .withAppId(UUID.randomUUID().toString())
                                            .withCategory(Category.CONNECTOR)
                                            .withEnvId(UUID.randomUUID().toString())
                                            .withName(UUID.randomUUID().toString())
                                            .build();

    String savedAttributeId = wingsPersistence.save(settingAttribute);
    SettingAttribute savedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    EncryptedData savedEncryptedData = wingsPersistence.get(
        EncryptedData.class, ((AppDynamicsConfig) savedAttribute.getValue()).getEncryptedPassword());

    vaultConfig = vaultService.getSecretConfig(accountId);
    encryptedData = vaultService.encrypt(
        name, keyToEncrypt, accountId, SettingVariableTypes.APP_DYNAMICS, vaultConfig, savedEncryptedData);
    assertNotNull(encryptedData.getEncryptedValue());
    assertNotNull(encryptedData.getEncryptionKey());
    assertFalse(StringUtils.isBlank(encryptedData.getEncryptionKey()));

    decryptedValue = vaultService.decrypt(encryptedData, accountId, vaultConfig);
    assertEquals(password, String.valueOf(decryptedValue));
  }

  @Test
  public void vaultEncryptionWhileSaving() throws IOException, IllegalAccessException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);
    String password = UUID.randomUUID().toString();

    final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                    .accountId(accountId)
                                                    .controllerUrl(UUID.randomUUID().toString())
                                                    .username(UUID.randomUUID().toString())
                                                    .password(password.toCharArray())
                                                    .accountname(UUID.randomUUID().toString())
                                                    .build();

    SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                            .withAccountId(accountId)
                                            .withValue(appDynamicsConfig)
                                            .withAppId(UUID.randomUUID().toString())
                                            .withCategory(Category.CONNECTOR)
                                            .withEnvId(UUID.randomUUID().toString())
                                            .withName(UUID.randomUUID().toString())
                                            .build();

    String savedAttributeId = wingsPersistence.save(settingAttribute);
    SettingAttribute savedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    assertEquals(appDynamicsConfig, savedAttribute.getValue());
    assertNull(((AppDynamicsConfig) savedAttribute.getValue()).getPassword());
    assertFalse(StringUtils.isBlank(((AppDynamicsConfig) savedAttribute.getValue()).getEncryptedPassword()));

    Query<EncryptedData> query =
        wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(settingAttribute.getUuid());
    assertEquals(1, query.asList().size());
    EncryptedData encryptedData = query.asList().get(0);
    assertEquals(vaultConfig.getUuid(), encryptedData.getKmsId());
    assertEquals(user.getUuid(), encryptedData.getCreatedBy().getUuid());
    assertEquals(userEmail, encryptedData.getCreatedBy().getEmail());
    assertEquals(userName, encryptedData.getCreatedBy().getName());

    List<SecretChangeLog> changeLogs = secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.APP_DYNAMICS);
    assertEquals(1, changeLogs.size());
    SecretChangeLog secretChangeLog = changeLogs.get(0);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());

    query = wingsPersistence.createQuery(EncryptedData.class);
    assertEquals(numOfEncRecords + 1, query.asList().size());

    encryptionService.decrypt((Encryptable) savedAttribute.getValue(),
        secretManager.getEncryptionDetails((Encryptable) savedAttribute.getValue(), workflowId, appId));

    AppDynamicsConfig value = (AppDynamicsConfig) savedAttribute.getValue();
    assertEquals(password, String.valueOf(value.getPassword()));
  }

  @Test
  public void vaultEncryptionSaveMultiple() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    int numOfSettingAttributes = 5;
    List<SettingAttribute> settingAttributes = new ArrayList<>();
    for (int i = 0; i < numOfSettingAttributes; i++) {
      String password = "password" + i;
      final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                      .accountId(accountId)
                                                      .controllerUrl(UUID.randomUUID().toString())
                                                      .username(UUID.randomUUID().toString())
                                                      .password(password.toCharArray())
                                                      .accountname(UUID.randomUUID().toString())
                                                      .build();

      SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                              .withAccountId(accountId)
                                              .withValue(appDynamicsConfig)
                                              .withAppId(UUID.randomUUID().toString())
                                              .withCategory(Category.CONNECTOR)
                                              .withEnvId(UUID.randomUUID().toString())
                                              .withName(UUID.randomUUID().toString())
                                              .build();

      settingAttributes.add(settingAttribute);
    }
    wingsPersistence.save(settingAttributes);

    assertEquals(numOfSettingAttributes, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(
        numOfEncRecords + numOfSettingAttributes, wingsPersistence.createQuery(EncryptedData.class).asList().size());
    for (int i = 0; i < numOfSettingAttributes; i++) {
      String id = settingAttributes.get(i).getUuid();
      SettingAttribute savedAttribute = wingsPersistence.get(SettingAttribute.class, id);
      assertEquals(settingAttributes.get(i), savedAttribute);
      AppDynamicsConfig appDynamicsConfig = (AppDynamicsConfig) settingAttributes.get(i).getValue();
      assertNull(appDynamicsConfig.getPassword());

      encryptionService.decrypt(
          appDynamicsConfig, secretManager.getEncryptionDetails(appDynamicsConfig, workflowId, appId));
      assertEquals("password" + i, new String(appDynamicsConfig.getPassword()));
      Query<EncryptedData> query = wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(id);
      assertEquals(1, query.asList().size());
      assertEquals(vaultConfig.getUuid(), query.asList().get(0).getKmsId());
    }

    Collection<VaultConfig> vaultConfigs = vaultService.listVaultConfigs(accountId);
    assertEquals(1, vaultConfigs.size());
    assertEquals(numOfSettingAttributes, vaultConfigs.iterator().next().getNumOfEncryptedValue());
  }

  @Test
  public void vaultEncryptionUpdateObject() throws IOException, IllegalAccessException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                    .accountId(accountId)
                                                    .controllerUrl(UUID.randomUUID().toString())
                                                    .username(UUID.randomUUID().toString())
                                                    .password(UUID.randomUUID().toString().toCharArray())
                                                    .accountname(UUID.randomUUID().toString())
                                                    .build();

    SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                            .withAccountId(accountId)
                                            .withValue(appDynamicsConfig)
                                            .withAppId(UUID.randomUUID().toString())
                                            .withCategory(Category.CONNECTOR)
                                            .withEnvId(UUID.randomUUID().toString())
                                            .withName(UUID.randomUUID().toString())
                                            .build();

    String savedAttributeId = wingsPersistence.save(settingAttribute);
    SettingAttribute savedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    assertEquals(settingAttribute, savedAttribute);
    assertEquals(1, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    ((AppDynamicsConfig) savedAttribute.getValue()).setUsername(UUID.randomUUID().toString());
    ((AppDynamicsConfig) savedAttribute.getValue()).setPassword(UUID.randomUUID().toString().toCharArray());
    User user1 = User.Builder.anUser().withEmail(UUID.randomUUID().toString()).withName("user1").build();
    wingsPersistence.save(user1);
    UserThreadLocal.set(user1);
    wingsPersistence.save(savedAttribute);

    SettingAttribute updatedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    assertEquals(savedAttribute, updatedAttribute);
    assertEquals(1, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    Query<EncryptedData> query =
        wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(savedAttributeId);
    assertEquals(1, query.asList().size());
    EncryptedData encryptedData = query.asList().get(0);
    assertEquals(vaultConfig.getUuid(), encryptedData.getKmsId());

    List<SecretChangeLog> changeLogs = secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.APP_DYNAMICS);
    assertEquals(2, changeLogs.size());
    SecretChangeLog secretChangeLog = changeLogs.get(0);
    assertEquals(user1.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user1.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user1.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed password", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(1);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Created", secretChangeLog.getDescription());

    User user2 = User.Builder.anUser().withEmail(UUID.randomUUID().toString()).withName("user2").build();
    wingsPersistence.save(user2);
    UserThreadLocal.set(user2);
    wingsPersistence.save(savedAttribute);

    query = wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(savedAttributeId);
    assertEquals(1, query.asList().size());

    changeLogs = secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.APP_DYNAMICS);
    assertEquals(3, changeLogs.size());
    secretChangeLog = changeLogs.get(0);
    assertEquals(user2.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user2.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user2.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed password", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(1);
    assertEquals(user1.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user1.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user1.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed password", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(2);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Created", secretChangeLog.getDescription());
  }

  @Test
  public void vaultEncryptionUpdateFieldSettingAttribute() throws IOException, IllegalAccessException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                    .accountId(accountId)
                                                    .controllerUrl(UUID.randomUUID().toString())
                                                    .username(UUID.randomUUID().toString())
                                                    .password(UUID.randomUUID().toString().toCharArray())
                                                    .accountname(UUID.randomUUID().toString())
                                                    .build();

    SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                            .withAccountId(accountId)
                                            .withValue(appDynamicsConfig)
                                            .withAppId(UUID.randomUUID().toString())
                                            .withCategory(Category.CONNECTOR)
                                            .withEnvId(UUID.randomUUID().toString())
                                            .withName(UUID.randomUUID().toString())
                                            .build();

    String savedAttributeId = wingsPersistence.save(settingAttribute);
    SettingAttribute savedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    assertEquals(settingAttribute, savedAttribute);
    assertEquals(1, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    String updatedAppId = UUID.randomUUID().toString();
    wingsPersistence.updateField(SettingAttribute.class, savedAttributeId, "appId", updatedAppId);

    SettingAttribute updatedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    assertEquals(updatedAppId, updatedAttribute.getAppId());
    savedAttribute.setAppId(updatedAppId);
    assertEquals(savedAttribute, updatedAttribute);
    assertEquals(1, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    Query<EncryptedData> query =
        wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(savedAttributeId);
    assertEquals(1, query.asList().size());

    List<SecretChangeLog> changeLogs = secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.APP_DYNAMICS);
    assertEquals(1, changeLogs.size());
    SecretChangeLog secretChangeLog = changeLogs.get(0);

    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Created", secretChangeLog.getDescription());

    final String newPassWord = UUID.randomUUID().toString();
    final AppDynamicsConfig newAppDynamicsConfig = AppDynamicsConfig.builder()
                                                       .accountId(accountId)
                                                       .controllerUrl(UUID.randomUUID().toString())
                                                       .username(UUID.randomUUID().toString())
                                                       .password(newPassWord.toCharArray())
                                                       .accountname(UUID.randomUUID().toString())
                                                       .build();

    updatedAppId = UUID.randomUUID().toString();
    String updatedName = UUID.randomUUID().toString();
    final Map<String, Object> keyValuePairs = new HashMap<>();
    keyValuePairs.put("name", updatedName);
    keyValuePairs.put("appId", updatedAppId);
    keyValuePairs.put("value", newAppDynamicsConfig);

    User user1 =
        User.Builder.anUser().withEmail(UUID.randomUUID().toString()).withName(UUID.randomUUID().toString()).build();
    wingsPersistence.save(user1);
    UserThreadLocal.set(user1);
    wingsPersistence.updateFields(SettingAttribute.class, savedAttributeId, keyValuePairs);

    query = wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(savedAttributeId);
    assertEquals(1, query.asList().size());
    EncryptedData encryptedData = query.asList().get(0);

    changeLogs = secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.APP_DYNAMICS);
    assertEquals(2, changeLogs.size());
    secretChangeLog = changeLogs.get(0);
    assertEquals(user1.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user1.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user1.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed password", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(1);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Created", secretChangeLog.getDescription());

    assertEquals(user.getUuid(), encryptedData.getCreatedBy().getUuid());
    assertEquals(userEmail, encryptedData.getCreatedBy().getEmail());
    assertEquals(userName, encryptedData.getCreatedBy().getName());

    updatedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    assertEquals(updatedAppId, updatedAttribute.getAppId());
    assertEquals(updatedName, updatedAttribute.getName());

    newAppDynamicsConfig.setPassword(null);
    assertEquals(newAppDynamicsConfig, updatedAttribute.getValue());

    assertEquals(1, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    User user2 =
        User.Builder.anUser().withEmail(UUID.randomUUID().toString()).withName(UUID.randomUUID().toString()).build();
    wingsPersistence.save(user2);
    UserThreadLocal.set(user2);
    wingsPersistence.updateFields(SettingAttribute.class, savedAttributeId, keyValuePairs);

    query = wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(savedAttributeId);
    assertEquals(1, query.asList().size());
    encryptedData = query.asList().get(0);

    changeLogs = secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.APP_DYNAMICS);
    assertEquals(3, changeLogs.size());
    secretChangeLog = changeLogs.get(0);
    assertEquals(user2.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user2.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user2.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed password", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(1);
    assertEquals(user1.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user1.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user1.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed password", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(2);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Created", secretChangeLog.getDescription());

    // test decryption
    savedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);
    AppDynamicsConfig savedConfig = (AppDynamicsConfig) savedAttribute.getValue();
    assertNull(savedConfig.getPassword());
    encryptionService.decrypt(savedConfig, secretManager.getEncryptionDetails(savedConfig, workflowId, appId));
    assertEquals(newPassWord, String.valueOf(savedConfig.getPassword()));
  }

  @Test
  public void vaultEncryptionSaveServiceVariable() throws IOException, IllegalAccessException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    final ServiceVariable serviceVariable = ServiceVariable.builder()
                                                .templateId(UUID.randomUUID().toString())
                                                .envId(UUID.randomUUID().toString())
                                                .entityType(EntityType.APPLICATION)
                                                .entityId(UUID.randomUUID().toString())
                                                .parentServiceVariableId(UUID.randomUUID().toString())
                                                .overrideType(OverrideType.ALL)
                                                .instances(Collections.singletonList(UUID.randomUUID().toString()))
                                                .expression(UUID.randomUUID().toString())
                                                .accountId(accountId)
                                                .name(UUID.randomUUID().toString())
                                                .value(UUID.randomUUID().toString().toCharArray())
                                                .type(Type.ENCRYPTED_TEXT)
                                                .build();

    String savedAttributeId = wingsPersistence.save(serviceVariable);
    ServiceVariable savedAttribute = wingsPersistence.get(ServiceVariable.class, savedAttributeId);
    assertEquals(serviceVariable, savedAttribute);
    assertEquals(1, wingsPersistence.createQuery(ServiceVariable.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    Map<String, Object> keyValuePairs = new HashMap<>();
    keyValuePairs.put("name", "newName");
    keyValuePairs.put("type", Type.ENCRYPTED_TEXT);
    keyValuePairs.put("value", "newValue".toCharArray());
    wingsPersistence.updateFields(ServiceVariable.class, savedAttributeId, keyValuePairs);
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    Query<EncryptedData> query =
        wingsPersistence.createQuery(EncryptedData.class).field("parentId").equal(savedAttributeId);
    assertEquals(1, query.asList().size());

    Collection<UuidAware> uuidAwares = secretManager.listEncryptedValues(accountId);
    assertEquals(2, uuidAwares.size());
    ServiceVariable listedVariable = null;
    for (UuidAware aware : uuidAwares) {
      if (aware.getUuid().equals(vaultConfig.getUuid())) {
        continue;
      }
      listedVariable = (ServiceVariable) aware;
    }
    assertEquals(SECRET_MASK, new String(listedVariable.getValue()));
    assertEquals(serviceVariable.getEntityType(), listedVariable.getEntityType());
    assertEquals(Type.ENCRYPTED_TEXT, listedVariable.getType());
    assertEquals(EncryptionType.VAULT, listedVariable.getEncryptionType());
    assertEquals(SettingVariableTypes.SERVICE_VARIABLE, listedVariable.getSettingType());
    assertEquals("newName", listedVariable.getName());

    // decrypt and verify
    ServiceVariable savedVariable = wingsPersistence.get(ServiceVariable.class, savedAttributeId);
    encryptionService.decrypt(savedVariable, secretManager.getEncryptionDetails(savedVariable, workflowId, appId));
    assertEquals("newValue", String.valueOf(savedVariable.getValue()));

    List<SecretChangeLog> changeLogs =
        secretManager.getChangeLogs(savedAttributeId, SettingVariableTypes.SERVICE_VARIABLE);
    assertEquals(2, changeLogs.size());
    SecretChangeLog secretChangeLog = changeLogs.get(0);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Changed value", secretChangeLog.getDescription());

    secretChangeLog = changeLogs.get(1);
    assertEquals(user.getUuid(), secretChangeLog.getUser().getUuid());
    assertEquals(user.getEmail(), secretChangeLog.getUser().getEmail());
    assertEquals(user.getName(), secretChangeLog.getUser().getName());
    assertEquals("Created", secretChangeLog.getDescription());
  }

  @Test
  public void vaultEncryptionSaveServiceVariableTemplate() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    String serviceId = wingsPersistence.save(Service.Builder.aService().withName(UUID.randomUUID().toString()).build());
    String serviceTemplateId =
        wingsPersistence.save(ServiceTemplate.Builder.aServiceTemplate().withServiceId(serviceId).build());

    final ServiceVariable serviceVariable = ServiceVariable.builder()
                                                .templateId(UUID.randomUUID().toString())
                                                .envId(UUID.randomUUID().toString())
                                                .entityType(EntityType.SERVICE_TEMPLATE)
                                                .entityId(serviceTemplateId)
                                                .parentServiceVariableId(UUID.randomUUID().toString())
                                                .overrideType(OverrideType.ALL)
                                                .instances(Collections.singletonList(UUID.randomUUID().toString()))
                                                .expression(UUID.randomUUID().toString())
                                                .accountId(accountId)
                                                .name(UUID.randomUUID().toString())
                                                .value(UUID.randomUUID().toString().toCharArray())
                                                .type(Type.ENCRYPTED_TEXT)
                                                .build();

    String savedAttributeId = wingsPersistence.save(serviceVariable);
    ServiceVariable savedAttribute = wingsPersistence.get(ServiceVariable.class, savedAttributeId);
    assertEquals(serviceVariable, savedAttribute);
    assertEquals(1, wingsPersistence.createQuery(ServiceVariable.class).asList().size());
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    Collection<UuidAware> uuidAwares = secretManager.listEncryptedValues(accountId);
    assertEquals(2, uuidAwares.size());
    ServiceVariable listedVariable = null;
    for (UuidAware aware : uuidAwares) {
      if (aware.getUuid().equals(vaultConfig.getUuid())) {
        continue;
      }
      listedVariable = (ServiceVariable) aware;
    }
    assertEquals(KmsServiceImpl.SECRET_MASK, new String(listedVariable.getValue()));
    assertEquals(serviceVariable.getEntityType(), listedVariable.getEntityType());
    assertEquals(Type.ENCRYPTED_TEXT, listedVariable.getType());
    assertEquals(EncryptionType.VAULT, listedVariable.getEncryptionType());
    assertEquals(SettingVariableTypes.SERVICE_VARIABLE, listedVariable.getSettingType());
    assertEquals(serviceId, listedVariable.getServiceId());
  }

  @Test
  public void kmsEncryptionDeleteSettingAttribute() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    int numOfSettingAttributes = 5;
    List<SettingAttribute> settingAttributes = new ArrayList<>();
    for (int i = 0; i < numOfSettingAttributes; i++) {
      final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                      .accountId(accountId)
                                                      .controllerUrl(UUID.randomUUID().toString())
                                                      .username(UUID.randomUUID().toString())
                                                      .password(UUID.randomUUID().toString().toCharArray())
                                                      .accountname(UUID.randomUUID().toString())
                                                      .build();

      SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                              .withAccountId(accountId)
                                              .withValue(appDynamicsConfig)
                                              .withAppId(UUID.randomUUID().toString())
                                              .withCategory(Category.CONNECTOR)
                                              .withEnvId(UUID.randomUUID().toString())
                                              .withName(UUID.randomUUID().toString())
                                              .build();

      wingsPersistence.save(settingAttribute);
      settingAttributes.add(settingAttribute);
    }

    assertEquals(numOfSettingAttributes, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(
        numOfEncRecords + numOfSettingAttributes, wingsPersistence.createQuery(EncryptedData.class).asList().size());
    for (int i = 0; i < numOfSettingAttributes; i++) {
      wingsPersistence.delete(settingAttributes.get(i));
      assertEquals(
          numOfSettingAttributes - (i + 1), wingsPersistence.createQuery(SettingAttribute.class).asList().size());
      assertEquals(numOfEncRecords + numOfSettingAttributes - (i + 1),
          wingsPersistence.createQuery(EncryptedData.class).asList().size());
    }
  }

  @Test
  public void kmsEncryptionDeleteSettingAttributeQueryUuid() throws IOException {
    VaultConfig vaultConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, vaultConfig);

    int numOfSettingAttributes = 5;
    List<SettingAttribute> settingAttributes = new ArrayList<>();
    for (int i = 0; i < numOfSettingAttributes; i++) {
      final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                      .accountId(accountId)
                                                      .controllerUrl(UUID.randomUUID().toString())
                                                      .username(UUID.randomUUID().toString())
                                                      .password(UUID.randomUUID().toString().toCharArray())
                                                      .accountname(UUID.randomUUID().toString())
                                                      .build();

      SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                              .withAccountId(accountId)
                                              .withValue(appDynamicsConfig)
                                              .withAppId(UUID.randomUUID().toString())
                                              .withCategory(Category.CONNECTOR)
                                              .withEnvId(UUID.randomUUID().toString())
                                              .withName(UUID.randomUUID().toString())
                                              .build();

      wingsPersistence.save(settingAttribute);
      settingAttributes.add(settingAttribute);
    }

    assertEquals(numOfSettingAttributes, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(
        numOfEncRecords + numOfSettingAttributes, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    for (int i = 0; i < numOfSettingAttributes; i++) {
      wingsPersistence.delete(SettingAttribute.class, settingAttributes.get(i).getUuid());
      assertEquals(
          numOfSettingAttributes - (i + 1), wingsPersistence.createQuery(SettingAttribute.class).asList().size());
      assertEquals(numOfEncRecords + numOfSettingAttributes - (i + 1),
          wingsPersistence.createQuery(EncryptedData.class).asList().size());
    }

    wingsPersistence.save(settingAttributes);
    assertEquals(numOfSettingAttributes, wingsPersistence.createQuery(SettingAttribute.class).asList().size());
    assertEquals(
        numOfEncRecords + numOfSettingAttributes, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    for (int i = 0; i < numOfSettingAttributes; i++) {
      wingsPersistence.delete(
          SettingAttribute.class, settingAttributes.get(i).getAppId(), settingAttributes.get(i).getUuid());
      assertEquals(
          numOfSettingAttributes - (i + 1), wingsPersistence.createQuery(SettingAttribute.class).asList().size());
      assertEquals(numOfEncRecords + numOfSettingAttributes - (i + 1),
          wingsPersistence.createQuery(EncryptedData.class).asList().size());
    }
  }

  @Test
  public void transitionKms() throws IOException, InterruptedException {
    Thread listenerThread = startTransitionListener();
    try {
      VaultConfig fromConfig = getVaultConfig();
      vaultService.saveVaultConfig(accountId, fromConfig);

      int numOfSettingAttributes = 5;
      Map<String, SettingAttribute> encryptedEntities = new HashMap<>();
      for (int i = 0; i < numOfSettingAttributes; i++) {
        String password = UUID.randomUUID().toString();
        final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                        .accountId(accountId)
                                                        .controllerUrl(UUID.randomUUID().toString())
                                                        .username(UUID.randomUUID().toString())
                                                        .password(password.toCharArray())
                                                        .accountname(UUID.randomUUID().toString())
                                                        .build();

        SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                                .withAccountId(accountId)
                                                .withValue(appDynamicsConfig)
                                                .withAppId(UUID.randomUUID().toString())
                                                .withCategory(Category.CONNECTOR)
                                                .withEnvId(UUID.randomUUID().toString())
                                                .withName(UUID.randomUUID().toString())
                                                .build();

        wingsPersistence.save(settingAttribute);
        appDynamicsConfig.setPassword(null);
        encryptedEntities.put(settingAttribute.getUuid(), settingAttribute);
      }

      Query<EncryptedData> query = wingsPersistence.createQuery(EncryptedData.class);
      List<EncryptedData> encryptedData = new ArrayList<>();
      assertEquals(numOfEncRecords + numOfSettingAttributes, query.asList().size());
      for (EncryptedData data : query.asList()) {
        if (data.getKmsId() == null || data.getType() == SettingVariableTypes.VAULT) {
          continue;
        }
        encryptedData.add(data);
        assertEquals(fromConfig.getUuid(), data.getKmsId());
        assertEquals(accountId, data.getAccountId());
      }

      assertEquals(numOfSettingAttributes, encryptedData.size());

      VaultConfig toConfig = getVaultConfig();
      vaultService.saveVaultConfig(accountId, toConfig);

      vaultService.transitionVault(accountId, fromConfig.getUuid(), toConfig.getUuid());
      Thread.sleep(TimeUnit.SECONDS.toMillis(10));
      query = wingsPersistence.createQuery(EncryptedData.class);

      assertEquals(numOfEncRecords + 1 + numOfSettingAttributes, query.asList().size());
      encryptedData = new ArrayList<>();
      for (EncryptedData data : query.asList()) {
        if (data.getKmsId() == null || data.getType() == SettingVariableTypes.VAULT) {
          continue;
        }
        encryptedData.add(data);
        assertEquals(toConfig.getUuid(), data.getKmsId());
        assertEquals(accountId, data.getAccountId());
      }
      assertEquals(numOfSettingAttributes, encryptedData.size());

      // read the values and compare
      PageResponse<SettingAttribute> attributeQuery =
          wingsPersistence.query(SettingAttribute.class, Builder.aPageRequest().build());
      assertEquals(numOfSettingAttributes, attributeQuery.size());
      for (SettingAttribute settingAttribute : attributeQuery) {
        assertEquals(encryptedEntities.get(settingAttribute.getUuid()), settingAttribute);
      }
    } finally {
      stopTransitionListener(listenerThread);
    }
  }

  @Test
  public void transitionAndDeleteVault() throws IOException, InterruptedException {
    Thread listenerThread = startTransitionListener();
    try {
      VaultConfig fromConfig = getVaultConfig();
      vaultService.saveVaultConfig(accountId, fromConfig);

      int numOfSettingAttributes = 5;
      Map<String, SettingAttribute> encryptedEntities = new HashMap<>();
      for (int i = 0; i < numOfSettingAttributes; i++) {
        String password = UUID.randomUUID().toString();
        final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                        .accountId(accountId)
                                                        .controllerUrl(UUID.randomUUID().toString())
                                                        .username(UUID.randomUUID().toString())
                                                        .password(password.toCharArray())
                                                        .accountname(UUID.randomUUID().toString())
                                                        .build();

        SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                                .withAccountId(accountId)
                                                .withValue(appDynamicsConfig)
                                                .withAppId(UUID.randomUUID().toString())
                                                .withCategory(Category.CONNECTOR)
                                                .withEnvId(UUID.randomUUID().toString())
                                                .withName(UUID.randomUUID().toString())
                                                .build();

        wingsPersistence.save(settingAttribute);
        appDynamicsConfig.setPassword(null);
        encryptedEntities.put(settingAttribute.getUuid(), settingAttribute);
      }

      Query<EncryptedData> query = wingsPersistence.createQuery(EncryptedData.class);
      List<EncryptedData> encryptedData = new ArrayList<>();
      assertEquals(numOfEncRecords + numOfSettingAttributes, query.asList().size());
      for (EncryptedData data : query.asList()) {
        if (data.getKmsId() == null || data.getType() == SettingVariableTypes.VAULT) {
          continue;
        }
        encryptedData.add(data);
        assertEquals(fromConfig.getUuid(), data.getKmsId());
        assertEquals(accountId, data.getAccountId());
      }

      assertEquals(numOfSettingAttributes, encryptedData.size());

      VaultConfig toConfig = getVaultConfig();
      vaultService.saveVaultConfig(accountId, toConfig);

      assertEquals(2, wingsPersistence.createQuery(VaultConfig.class).asList().size());
      try {
        vaultService.deleteVaultConfig(accountId, fromConfig.getUuid());
        fail("Was able to delete vault which has reference in encrypted secrets");
      } catch (WingsException e) {
        // expected
      }

      vaultService.transitionVault(accountId, fromConfig.getUuid(), toConfig.getUuid());
      Thread.sleep(TimeUnit.SECONDS.toMillis(10));
      vaultService.deleteVaultConfig(accountId, fromConfig.getUuid());
      assertEquals(1, wingsPersistence.createQuery(VaultConfig.class).asList().size());

      query = wingsPersistence.createQuery(EncryptedData.class);
      assertEquals(numOfEncRecords + numOfSettingAttributes, query.asList().size());
    } finally {
      stopTransitionListener(listenerThread);
    }
  }

  @Test
  @RealMongo
  public void saveConfigFileWithEncryption() throws IOException, InterruptedException, IllegalAccessException {
    final long seed = System.currentTimeMillis();
    System.out.println("seed: " + seed);
    Random r = new Random(seed);
    final String appId = UUID.randomUUID().toString();
    VaultConfig fromConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, fromConfig);

    Service service = Service.Builder.aService().withName(UUID.randomUUID().toString()).withAppId(appId).build();
    wingsPersistence.save(service);

    Activity activity = Activity.builder().workflowId(workflowId).build();
    activity.setAppId(appId);
    wingsPersistence.save(activity);

    ConfigFile.Builder configFileBuilder = ConfigFile.Builder.aConfigFile()
                                               .withTemplateId(UUID.randomUUID().toString())
                                               .withEnvId(UUID.randomUUID().toString())
                                               .withEntityType(EntityType.SERVICE)
                                               .withEntityId(service.getUuid())
                                               .withDescription(UUID.randomUUID().toString())
                                               .withParentConfigFileId(UUID.randomUUID().toString())
                                               .withRelativeFilePath(UUID.randomUUID().toString())
                                               .withTargetToAllEnv(r.nextBoolean())
                                               .withDefaultVersion(r.nextInt())
                                               .withEnvIdVersionMapString(UUID.randomUUID().toString())
                                               .withSetAsDefault(r.nextBoolean())
                                               .withNotes(UUID.randomUUID().toString())
                                               .withOverridePath(UUID.randomUUID().toString())
                                               .withConfigOverrideType(ConfigOverrideType.CUSTOM)
                                               .withConfigOverrideExpression(UUID.randomUUID().toString())
                                               .withAppId(appId)
                                               .withAccountId(accountId)
                                               .withFileName(UUID.randomUUID().toString())
                                               .withName(UUID.randomUUID().toString())
                                               .withEncrypted(true);

    File fileToSave = new File(getClass().getClassLoader().getResource("./encryption/file_to_encrypt.txt").getFile());

    String configFileId =
        configService.save(configFileBuilder.but().build(), new BoundedInputStream(new FileInputStream(fileToSave)));
    File download = configService.download(appId, configFileId);
    assertEquals(FileUtils.readFileToString(fileToSave), FileUtils.readFileToString(download));
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    List<EncryptedData> encryptedFileData = wingsPersistence.createQuery(EncryptedData.class)
                                                .field("type")
                                                .equal(SettingVariableTypes.CONFIG_FILE)
                                                .asList();
    assertEquals(1, encryptedFileData.size());
    assertFalse(StringUtils.isBlank(encryptedFileData.get(0).getParentId()));
    // test update
    File fileToUpdate = new File(getClass().getClassLoader().getResource("./encryption/file_to_update.txt").getFile());
    configService.update(configFileBuilder.withUuid(configFileId).but().build(),
        new BoundedInputStream(new FileInputStream(fileToUpdate)));
    download = configService.download(appId, configFileId);
    assertEquals(FileUtils.readFileToString(fileToUpdate), FileUtils.readFileToString(download));
    assertEquals(numOfEncRecords + 1, wingsPersistence.createQuery(EncryptedData.class).asList().size());

    encryptedFileData = wingsPersistence.createQuery(EncryptedData.class)
                            .field("type")
                            .equal(SettingVariableTypes.CONFIG_FILE)
                            .asList();
    assertEquals(1, encryptedFileData.size());
    assertFalse(StringUtils.isBlank(encryptedFileData.get(0).getParentId()));

    int numOfAccess = 7;
    for (int i = 0; i < numOfAccess; i++) {
      configService.downloadForActivity(appId, configFileId, activity.getUuid());
    }
    List<SecretUsageLog> usageLogs = secretManager.getUsageLogs(configFileId, SettingVariableTypes.CONFIG_FILE);
    assertEquals(numOfAccess, usageLogs.size());

    for (SecretUsageLog usageLog : usageLogs) {
      assertEquals(workflowName, usageLog.getWorkflowName());
      assertEquals(accountId, usageLog.getAccountId());
    }
  }

  @Test
  public void vaultEncryptionYaml() throws IllegalAccessException, NoSuchFieldException {
    String password = UUID.randomUUID().toString();
    String accountId = UUID.randomUUID().toString();
    String name = UUID.randomUUID().toString();
    VaultConfig fromConfig = getVaultConfig();
    vaultService.saveVaultConfig(accountId, fromConfig);

    final AppDynamicsConfig appDynamicsConfig = AppDynamicsConfig.builder()
                                                    .accountId(accountId)
                                                    .controllerUrl(UUID.randomUUID().toString())
                                                    .username(UUID.randomUUID().toString())
                                                    .password(password.toCharArray())
                                                    .accountname(UUID.randomUUID().toString())
                                                    .build();

    SettingAttribute settingAttribute = SettingAttribute.Builder.aSettingAttribute()
                                            .withAccountId(appDynamicsConfig.getAccountId())
                                            .withValue(appDynamicsConfig)
                                            .withAppId(UUID.randomUUID().toString())
                                            .withCategory(Category.CONNECTOR)
                                            .withEnvId(UUID.randomUUID().toString())
                                            .withName(name)
                                            .build();

    String savedAttributeId = wingsPersistence.save(settingAttribute);
    appDynamicsConfig.setPassword(password.toCharArray());
    SettingAttribute savedAttribute = wingsPersistence.get(SettingAttribute.class, savedAttributeId);

    String encryptedYamlRef = secretManager.getEncryptedYamlRef(
        (Encryptable) savedAttribute.getValue(), null, SettingVariableTypes.APP_DYNAMICS);
    assertTrue(encryptedYamlRef.startsWith(EncryptionType.VAULT.name()));
    char[] decryptedPassword = secretManager.decryptYamlRef(encryptedYamlRef);
    assertEquals(password, new String(decryptedPassword));

    password = UUID.randomUUID().toString();
    name = UUID.randomUUID().toString();
    String appId = UUID.randomUUID().toString();

    final ServiceVariable serviceVariable = ServiceVariable.builder()
                                                .templateId(UUID.randomUUID().toString())
                                                .envId(UUID.randomUUID().toString())
                                                .entityType(EntityType.APPLICATION)
                                                .entityId(UUID.randomUUID().toString())
                                                .parentServiceVariableId(UUID.randomUUID().toString())
                                                .overrideType(OverrideType.ALL)
                                                .instances(Collections.singletonList(UUID.randomUUID().toString()))
                                                .expression(UUID.randomUUID().toString())
                                                .accountId(accountId)
                                                .name(name)
                                                .value(password.toCharArray())
                                                .type(Type.ENCRYPTED_TEXT)
                                                .build();
    serviceVariable.setAppId(appId);

    savedAttributeId = wingsPersistence.save(serviceVariable);
    ServiceVariable savedServiceVariable = wingsPersistence.get(ServiceVariable.class, savedAttributeId);
    encryptedYamlRef =
        secretManager.getEncryptedYamlRef(savedServiceVariable, null, SettingVariableTypes.SERVICE_VARIABLE);
    assertTrue(encryptedYamlRef.startsWith(EncryptionType.VAULT.name()));
    decryptedPassword = secretManager.decryptYamlRef(encryptedYamlRef);
    assertEquals(password, new String(decryptedPassword));
  }

  private VaultConfig getVaultConfig() {
    return VaultConfig.builder()
        .vaultUrl("http://127.0.0.1:8200")
        .authToken(VAULT_TOKEN)
        .name("myVault")
        .isDefault(true)
        .build();
  }

  private KmsConfig getKmsConfig() {
    final KmsConfig kmsConfig = new KmsConfig();
    kmsConfig.setName("myKms");
    kmsConfig.setDefault(true);
    kmsConfig.setKmsArn("arn:aws:kms:us-east-1:830767422336:key/6b64906a-b7ab-4f69-8159-e20fef1f204d");
    kmsConfig.setAccessKey("AKIAJLEKM45P4PO5QUFQ");
    kmsConfig.setSecretKey("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");
    return kmsConfig;
  }

  private void enableKmsFeatureFlag() {
    FeatureFlag kmsFeatureFlag =
        FeatureFlag.builder().name(FeatureName.KMS.name()).enabled(true).obsolete(false).build();
    wingsPersistence.save(kmsFeatureFlag);
  }

  private Thread startTransitionListener() {
    setInternalState(vaultService, "transitionKmsQueue", transitionKmsQueue);
    transitionEventListener = new KmsTransitionEventListener();
    setInternalState(transitionEventListener, "timer", new ScheduledThreadPoolExecutor(1));
    setInternalState(transitionEventListener, "queue", transitionKmsQueue);
    setInternalState(transitionEventListener, "vaultService", vaultService);

    Thread eventListenerThread = new Thread(() -> transitionEventListener.run());
    eventListenerThread.start();
    return eventListenerThread;
  }

  private void stopTransitionListener(Thread thread) throws InterruptedException {
    transitionEventListener.shutDown();
    thread.join();
  }
}
