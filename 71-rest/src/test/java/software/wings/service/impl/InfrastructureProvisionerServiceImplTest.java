package software.wings.service.impl;

import static io.harness.rule.OwnerRule.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mongodb.morphia.mapping.Mapper.ID_KEY;
import static software.wings.beans.AwsInfrastructureMapping.Builder.anAwsInfrastructureMapping;
import static software.wings.beans.InfrastructureMappingBlueprint.NodeFilteringType.AWS_INSTANCE_FILTER;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_ID;
import static software.wings.utils.WingsTestConstants.SETTING_ID;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import com.mongodb.DBCursor;
import io.harness.beans.PageRequest;
import io.harness.beans.PageResponse;
import io.harness.beans.SearchFilter.Operator;
import io.harness.category.element.UnitTests;
import io.harness.delegate.command.CommandExecutionResult.CommandExecutionStatus;
import io.harness.exception.InvalidRequestException;
import io.harness.rule.OwnerRule.Owner;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.joor.Reflect;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;
import software.wings.WingsBaseTest;
import software.wings.api.DeploymentType;
import software.wings.beans.AwsInfrastructureMapping;
import software.wings.beans.AwsInstanceFilter;
import software.wings.beans.BlueprintProperty;
import software.wings.beans.CloudFormationInfrastructureProvisioner;
import software.wings.beans.EntityType;
import software.wings.beans.FeatureName;
import software.wings.beans.InfrastructureMapping;
import software.wings.beans.InfrastructureMappingBlueprint;
import software.wings.beans.InfrastructureMappingBlueprint.CloudProviderType;
import software.wings.beans.InfrastructureMappingType;
import software.wings.beans.InfrastructureProvisioner;
import software.wings.beans.InfrastructureProvisionerDetails;
import software.wings.beans.NameValuePair;
import software.wings.beans.Service;
import software.wings.beans.Service.ServiceKeys;
import software.wings.beans.ServiceVariable.Type;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.Builder;
import software.wings.beans.SettingAttribute.SettingAttributeKeys;
import software.wings.beans.TerraformInfrastructureProvisioner;
import software.wings.dl.WingsPersistence;
import software.wings.expression.ManagerExpressionEvaluator;
import software.wings.helpers.ext.cloudformation.response.CloudFormationCommandResponse;
import software.wings.helpers.ext.cloudformation.response.CloudFormationCreateStackResponse;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.FeatureFlagService;
import software.wings.service.intfc.InfrastructureMappingService;
import software.wings.service.intfc.InfrastructureProvisionerService;
import software.wings.service.intfc.ResourceLookupService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.aws.manager.AwsCFHelperServiceManager;
import software.wings.settings.SettingValue.SettingVariableTypes;
import software.wings.sm.ExecutionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InfrastructureProvisionerServiceImplTest extends WingsBaseTest {
  @Mock WingsPersistence wingsPersistence;
  @Mock ExecutionContext executionContext;
  @Mock Query query;
  @Mock DBCursor dbCursor;
  @Mock MorphiaIterator infrastructureMappings;
  @Mock InfrastructureMappingService infrastructureMappingService;
  @Mock FeatureFlagService featureFlagService;
  @Mock AwsCFHelperServiceManager awsCFHelperServiceManager;
  @Mock ServiceResourceService serviceResourceService;
  @Mock SettingsService settingService;
  @Mock ResourceLookupService resourceLookupService;
  @Mock AppService appService;
  @Inject @InjectMocks InfrastructureProvisionerService infrastructureProvisionerService;

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void testRegenerateInfrastructureMappings() throws Exception {
    doReturn(false).when(featureFlagService).isEnabled(eq(FeatureName.INFRA_MAPPING_REFACTOR), any());
    InfrastructureProvisioner infrastructureProvisioner =
        CloudFormationInfrastructureProvisioner.builder()
            .appId(APP_ID)
            .uuid(ID_KEY)
            .mappingBlueprints(Arrays.asList(
                InfrastructureMappingBlueprint.builder()
                    .cloudProviderType(CloudProviderType.AWS)
                    .serviceId(SERVICE_ID)
                    .deploymentType(DeploymentType.SSH)
                    .properties(Arrays.asList(BlueprintProperty.builder()
                                                  .name("region")
                                                  .value("${cloudformation"
                                                      + ".myregion}")
                                                  .build(),
                        BlueprintProperty.builder().name("vpcs").value("${cloudformation.myvpcs}").build(),
                        BlueprintProperty.builder().name("tags").value("${cloudformation.mytags}").build()))
                    .nodeFilteringType(AWS_INSTANCE_FILTER)
                    .build()))
            .build();
    doReturn(infrastructureProvisioner)
        .when(wingsPersistence)
        .getWithAppId(eq(InfrastructureProvisioner.class), anyString(), anyString());
    doReturn(query).when(wingsPersistence).createQuery(eq(InfrastructureMapping.class));
    doReturn(query).doReturn(query).when(query).filter(anyString(), any());
    doReturn(infrastructureMappings).when(query).fetch();
    doReturn(new HashMap<>()).when(executionContext).asMap();

    doReturn(true).doReturn(true).doReturn(false).when(infrastructureMappings).hasNext();
    InfrastructureMapping infrastructureMapping = anAwsInfrastructureMapping()
                                                      .withAppId(APP_ID)
                                                      .withProvisionerId(ID_KEY)
                                                      .withServiceId(SERVICE_ID)
                                                      .withInfraMappingType(InfrastructureMappingType.AWS_SSH.name())
                                                      .build();

    doReturn(infrastructureMapping).when(infrastructureMappings).next();
    doReturn(dbCursor).when(infrastructureMappings).getCursor();

    Map<String, Object> tagMap = new HashMap<>();
    tagMap.put("name", "mockName");
    Map<String, Object> objectMap = new HashMap<>();
    objectMap.put("myregion", "us-east-1");
    objectMap.put("myvpcs", "vpc1,vpc2,vpc3");
    objectMap.put("mytags", "name:mockName");
    CloudFormationCommandResponse commandResponse = CloudFormationCreateStackResponse.builder()
                                                        .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                                                        .output(StringUtils.EMPTY)
                                                        .stackId("11")
                                                        .cloudFormationOutputMap(objectMap)
                                                        .build();

    doReturn(infrastructureMapping).when(infrastructureMappingService).update(any());
    infrastructureProvisionerService.regenerateInfrastructureMappings(ID_KEY, executionContext, objectMap);

    ArgumentCaptor<InfrastructureMapping> captor = ArgumentCaptor.forClass(InfrastructureMapping.class);
    verify(infrastructureMappingService).update(captor.capture());
    InfrastructureMapping mapping = captor.getValue();
    AwsInstanceFilter awsInstanceFilter = ((AwsInfrastructureMapping) mapping).getAwsInstanceFilter();
    assertThat(awsInstanceFilter).isNotNull();
    assertThat(((AwsInfrastructureMapping) mapping).getRegion()).isEqualTo("us-east-1");

    assertThat(awsInstanceFilter.getVpcIds()).isNotNull();
    assertThat(awsInstanceFilter.getVpcIds()).hasSize(3);
    assertThat(awsInstanceFilter.getVpcIds().contains("vpc1")).isTrue();
    assertThat(awsInstanceFilter.getVpcIds().contains("vpc2")).isTrue();
    assertThat(awsInstanceFilter.getVpcIds().contains("vpc3")).isTrue();

    assertThat(awsInstanceFilter.getTags()).isNotNull();
    assertThat(awsInstanceFilter.getTags()).hasSize(1);
    assertThat(awsInstanceFilter.getTags().get(0).getKey()).isEqualTo("name");
    assertThat(awsInstanceFilter.getTags().get(0).getValue()).isEqualTo("mockName");
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void testGetCFTemplateParamKeys() {
    String defaultString = "default";

    doReturn(Arrays.asList())
        .when(awsCFHelperServiceManager)
        .getParamsData(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

    infrastructureProvisionerService.getCFTemplateParamKeys("GIT", defaultString, defaultString, defaultString,
        defaultString, defaultString, defaultString, defaultString, defaultString, true);
    assertThatThrownBy(
        ()
            -> infrastructureProvisionerService.getCFTemplateParamKeys("GIT", defaultString, defaultString,
                defaultString, defaultString, "", defaultString, defaultString, defaultString, true))
        .isInstanceOf(InvalidRequestException.class);
    assertThatThrownBy(()
                           -> infrastructureProvisionerService.getCFTemplateParamKeys("GIT", defaultString,
                               defaultString, defaultString, defaultString, defaultString, "", defaultString, "", true))
        .isInstanceOf(InvalidRequestException.class);
    assertThatThrownBy(
        ()
            -> infrastructureProvisionerService.getCFTemplateParamKeys("TEMPLATE_BODY", defaultString, defaultString,
                "", defaultString, defaultString, defaultString, defaultString, defaultString, true))
        .isInstanceOf(InvalidRequestException.class);
    assertThatThrownBy(
        ()
            -> infrastructureProvisionerService.getCFTemplateParamKeys("TEMPLATE_URL", defaultString, defaultString, "",
                defaultString, defaultString, defaultString, defaultString, defaultString, true))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldValidateInfrastructureProvisioner() {
    TerraformInfrastructureProvisioner terraformProvisioner = TerraformInfrastructureProvisioner.builder()
                                                                  .accountId(ACCOUNT_ID)
                                                                  .appId(APP_ID)
                                                                  .name("tf-test")
                                                                  .sourceRepoBranch("master")
                                                                  .path("module/main.tf")
                                                                  .sourceRepoSettingId(SETTING_ID)
                                                                  .build();
    InfrastructureProvisionerServiceImpl provisionerService = spy(InfrastructureProvisionerServiceImpl.class);
    provisionerService.validateProvisioner(terraformProvisioner);

    shouldValidateRepoBranch(terraformProvisioner, provisionerService);
    provisionerService.validateProvisioner(terraformProvisioner);

    shouldValidatePath(terraformProvisioner, provisionerService);
    provisionerService.validateProvisioner(terraformProvisioner);

    shouldValidateSourceRepo(terraformProvisioner, provisionerService);
    provisionerService.validateProvisioner(terraformProvisioner);

    shouldVariablesValidation(terraformProvisioner, provisionerService);
    provisionerService.validateProvisioner(terraformProvisioner);

    shouldBackendConfigValidation(terraformProvisioner, provisionerService);
    provisionerService.validateProvisioner(terraformProvisioner);
  }

  private void shouldBackendConfigValidation(TerraformInfrastructureProvisioner terraformProvisioner,
      InfrastructureProvisionerServiceImpl provisionerService) {
    terraformProvisioner.setBackendConfigs(
        Arrays.asList(NameValuePair.builder().name("access.key").valueType(Type.TEXT.toString()).build(),
            NameValuePair.builder().name("secret_key").valueType(Type.TEXT.toString()).build()));
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));

    terraformProvisioner.setBackendConfigs(
        Arrays.asList(NameValuePair.builder().name("$access_key").valueType(Type.TEXT.toString()).build(),
            NameValuePair.builder().name("secret_key").valueType(Type.TEXT.toString()).build()));
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));

    terraformProvisioner.setBackendConfigs(null);
    provisionerService.validateProvisioner(terraformProvisioner);

    terraformProvisioner.setBackendConfigs(Collections.emptyList());
    provisionerService.validateProvisioner(terraformProvisioner);

    terraformProvisioner.setBackendConfigs(
        Arrays.asList(NameValuePair.builder().name("access_key").valueType(Type.TEXT.toString()).build(),
            NameValuePair.builder().name("secret_key").valueType(Type.TEXT.toString()).build()));
    provisionerService.validateProvisioner(terraformProvisioner);
  }

  private void shouldVariablesValidation(TerraformInfrastructureProvisioner terraformProvisioner,
      InfrastructureProvisionerServiceImpl provisionerService) {
    terraformProvisioner.setVariables(
        Arrays.asList(NameValuePair.builder().name("access.key").valueType(Type.TEXT.toString()).build(),
            NameValuePair.builder().name("secret_key").valueType(Type.TEXT.toString()).build()));
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));

    terraformProvisioner.setVariables(
        Arrays.asList(NameValuePair.builder().name("$access_key").valueType(Type.TEXT.toString()).build(),
            NameValuePair.builder().name("secret_key").valueType(Type.TEXT.toString()).build()));
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));

    terraformProvisioner.setVariables(null);
    provisionerService.validateProvisioner(terraformProvisioner);

    terraformProvisioner.setVariables(Collections.emptyList());
    provisionerService.validateProvisioner(terraformProvisioner);

    terraformProvisioner.setVariables(
        Arrays.asList(NameValuePair.builder().name("access_key").valueType(Type.TEXT.toString()).build(),
            NameValuePair.builder().name("secret_key").valueType(Type.TEXT.toString()).build()));
    provisionerService.validateProvisioner(terraformProvisioner);
  }

  private void shouldValidateSourceRepo(TerraformInfrastructureProvisioner terraformProvisioner,
      InfrastructureProvisionerServiceImpl provisionerService) {
    terraformProvisioner.setSourceRepoSettingId("");
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));
    terraformProvisioner.setSourceRepoSettingId(null);
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));
    terraformProvisioner.setSourceRepoSettingId("settingId");
  }

  private void shouldValidatePath(TerraformInfrastructureProvisioner terraformProvisioner,
      InfrastructureProvisionerServiceImpl provisionerService) {
    terraformProvisioner.setPath(null);
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));
    terraformProvisioner.setPath("module/main.tf");
  }

  private void shouldValidateRepoBranch(TerraformInfrastructureProvisioner terraformProvisioner,
      InfrastructureProvisionerServiceImpl provisionerService) {
    terraformProvisioner.setSourceRepoBranch("");
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));
    terraformProvisioner.setSourceRepoBranch(null);
    Assertions.assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> provisionerService.validateProvisioner(terraformProvisioner));
    terraformProvisioner.setSourceRepoBranch("master");
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void nonProvisionerExpressionsResolutionShouldNotFailOnNonResolution() {
    String workflowVariable = "${workflowVariables.var1}";
    Map<String, Object> contextMap = null;
    List<NameValuePair> properties = new ArrayList<>();
    properties.add(NameValuePair.builder().value(workflowVariable).build());
    ManagerExpressionEvaluator evaluator = Mockito.mock(ManagerExpressionEvaluator.class);
    Reflect.on(infrastructureProvisionerService).set("evaluator", evaluator);
    when(evaluator.evaluate(workflowVariable, contextMap)).thenReturn(null);

    ((InfrastructureProvisionerServiceImpl) infrastructureProvisionerService)
        .getPropertyNameEvaluatedMap(
            properties, contextMap, true, TerraformInfrastructureProvisioner.INFRASTRUCTURE_PROVISIONER_TYPE_KEY);

    verify(evaluator, times(1)).evaluate(workflowVariable, contextMap);
  }

  @Test(expected = InvalidRequestException.class)
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void provisionerExpressionsResolutionShouldFailOnNonResolution() {
    String provisionerVariable = "${terraform.var1}";
    Map<String, Object> contextMap = null;
    List<NameValuePair> properties = new ArrayList<>();
    properties.add(NameValuePair.builder().value(provisionerVariable).build());
    ManagerExpressionEvaluator evaluator = Mockito.mock(ManagerExpressionEvaluator.class);
    Reflect.on(infrastructureProvisionerService).set("evaluator", evaluator);
    when(evaluator.evaluate(provisionerVariable, contextMap)).thenReturn(null);

    ((InfrastructureProvisionerServiceImpl) infrastructureProvisionerService)
        .getPropertyNameEvaluatedMap(properties, contextMap, true, TerraformInfrastructureProvisioner.VARIABLE_KEY);
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldGetIdToServiceMapping() {
    PageRequest<Service> servicePageRequest = new PageRequest<>();
    servicePageRequest.addFilter(Service.APP_ID_KEY, Operator.EQ, APP_ID);
    Set<String> serviceIds = Sets.newHashSet(Arrays.asList("id1", "id2"));
    servicePageRequest.addFilter(ServiceKeys.uuid, Operator.IN, serviceIds.toArray());
    PageResponse<Service> services = new PageResponse<>();
    Service service1 = Service.builder().name("service1").uuid("id1").build();
    Service service2 = Service.builder().name("service2").uuid("id2").build();
    services.setResponse(Arrays.asList(service1, service2));
    when(serviceResourceService.list(servicePageRequest, false, false, false, null)).thenReturn(services);

    Map<String, Service> idToServiceMapping = ((InfrastructureProvisionerServiceImpl) infrastructureProvisionerService)
                                                  .getIdToServiceMapping(APP_ID, serviceIds);

    assertThat(idToServiceMapping).hasSize(2);
    assertThat(idToServiceMapping.get("id1")).isEqualTo(service1);
    assertThat(idToServiceMapping.get("id2")).isEqualTo(service2);
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldGetIdToServiceMappingForEmptyServiceIds() {
    Map<String, Service> idToServiceMapping = ((InfrastructureProvisionerServiceImpl) infrastructureProvisionerService)
                                                  .getIdToServiceMapping(APP_ID, Collections.emptySet());

    assertThat(idToServiceMapping).isEmpty();
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldGetIdToSettingAttributeMapping() {
    PageRequest<SettingAttribute> settingAttributePageRequest = new PageRequest<>();
    settingAttributePageRequest.addFilter(SettingAttribute.ACCOUNT_ID_KEY, Operator.EQ, ACCOUNT_ID);
    settingAttributePageRequest.addFilter(
        SettingAttribute.VALUE_TYPE_KEY, Operator.EQ, SettingVariableTypes.GIT.name());
    Set<String> settingAttributeIds = Sets.newHashSet(Arrays.asList("id1", "id2"));
    settingAttributePageRequest.addFilter(SettingAttributeKeys.uuid, Operator.IN, settingAttributeIds.toArray());
    PageResponse<SettingAttribute> settingAttributePageResponse = new PageResponse<>();
    SettingAttribute settingAttribute1 = Builder.aSettingAttribute().withUuid("id1").build();
    SettingAttribute settingAttribute2 = Builder.aSettingAttribute().withUuid("id2").build();
    settingAttributePageResponse.setResponse(Arrays.asList(settingAttribute1, settingAttribute2));
    when(settingService.list(settingAttributePageRequest, null, null)).thenReturn(settingAttributePageResponse);

    Map<String, SettingAttribute> idToSettingAttributeMapping =
        ((InfrastructureProvisionerServiceImpl) infrastructureProvisionerService)
            .getIdToSettingAttributeMapping(ACCOUNT_ID, settingAttributeIds);

    assertThat(idToSettingAttributeMapping).hasSize(2);
    assertThat(idToSettingAttributeMapping.get("id1")).isEqualTo(settingAttribute1);
    assertThat(idToSettingAttributeMapping.get("id2")).isEqualTo(settingAttribute2);
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldGetIdToSettingAttributeMappingForEmptySettingAttributeIds() {
    Map<String, SettingAttribute> idToSettingAttributeMapping =
        ((InfrastructureProvisionerServiceImpl) infrastructureProvisionerService)
            .getIdToSettingAttributeMapping(ACCOUNT_ID, Collections.emptySet());

    assertThat(idToSettingAttributeMapping).isEmpty();
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldListDetails() {
    InfrastructureProvisionerServiceImpl ipService = spy(new InfrastructureProvisionerServiceImpl());
    Reflect.on(ipService).set("resourceLookupService", resourceLookupService);
    Reflect.on(ipService).set("appService", appService);
    PageRequest<InfrastructureProvisioner> infraProvisionerPageRequest = new PageRequest<>();
    PageResponse<InfrastructureProvisioner> infraProvisionerPageResponse = new PageResponse<>();
    TerraformInfrastructureProvisioner provisioner =
        TerraformInfrastructureProvisioner.builder()
            .sourceRepoSettingId("settingId")
            .mappingBlueprints(
                Collections.singletonList(InfrastructureMappingBlueprint.builder().serviceId("serviceId").build()))
            .build();
    infraProvisionerPageResponse.setResponse(Collections.singletonList(provisioner));
    doReturn(infraProvisionerPageResponse)
        .when(resourceLookupService)
        .listWithTagFilters(infraProvisionerPageRequest, null, EntityType.PROVISIONER, true);
    doReturn(ACCOUNT_ID).when(appService).getAccountIdByAppId(APP_ID);
    HashSet<String> settingAttributeIds = new HashSet<>(Collections.singletonList("settingId"));
    Map<String, SettingAttribute> idToSettingAttributeMapping = new HashMap<>();
    doReturn(idToSettingAttributeMapping)
        .when(ipService)
        .getIdToSettingAttributeMapping(ACCOUNT_ID, settingAttributeIds);
    HashSet<String> servicesIds = new HashSet<>(Collections.singletonList("serviceId"));
    Map<String, Service> idToServiceMapping = new HashMap<>();
    doReturn(idToServiceMapping).when(ipService).getIdToServiceMapping(APP_ID, servicesIds);
    InfrastructureProvisionerDetails infrastructureProvisionerDetails =
        InfrastructureProvisionerDetails.builder().build();
    doReturn(infrastructureProvisionerDetails)
        .when(ipService)
        .details(provisioner, idToSettingAttributeMapping, idToServiceMapping);

    PageResponse<InfrastructureProvisionerDetails> infraProvisionerDetailsPageResponse =
        ipService.listDetails(infraProvisionerPageRequest, true, null, APP_ID);

    assertThat(infraProvisionerDetailsPageResponse.getResponse()).hasSize(1);
    assertThat(infraProvisionerDetailsPageResponse.getResponse().get(0)).isEqualTo(infrastructureProvisionerDetails);
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void shouldListDetailsForEmptyInfraMappingBlueprints() {
    InfrastructureProvisionerServiceImpl ipService = spy(new InfrastructureProvisionerServiceImpl());
    Reflect.on(ipService).set("resourceLookupService", resourceLookupService);
    Reflect.on(ipService).set("appService", appService);
    PageRequest<InfrastructureProvisioner> infraProvisionerPageRequest = new PageRequest<>();
    PageResponse<InfrastructureProvisioner> infraProvisionerPageResponse = new PageResponse<>();
    TerraformInfrastructureProvisioner provisioner =
        TerraformInfrastructureProvisioner.builder().sourceRepoSettingId("settingId").build();
    infraProvisionerPageResponse.setResponse(Collections.singletonList(provisioner));
    doReturn(infraProvisionerPageResponse)
        .when(resourceLookupService)
        .listWithTagFilters(infraProvisionerPageRequest, null, EntityType.PROVISIONER, true);
    doReturn(ACCOUNT_ID).when(appService).getAccountIdByAppId(APP_ID);
    HashSet<String> settingAttributeIds = new HashSet<>(Collections.singletonList("settingId"));
    Map<String, SettingAttribute> idToSettingAttributeMapping = new HashMap<>();
    doReturn(idToSettingAttributeMapping)
        .when(ipService)
        .getIdToSettingAttributeMapping(ACCOUNT_ID, settingAttributeIds);
    HashSet<String> servicesIds = new HashSet<>();
    Map<String, Service> idToServiceMapping = new HashMap<>();
    doReturn(idToServiceMapping).when(ipService).getIdToServiceMapping(APP_ID, servicesIds);
    InfrastructureProvisionerDetails infrastructureProvisionerDetails =
        InfrastructureProvisionerDetails.builder().build();
    doReturn(infrastructureProvisionerDetails)
        .when(ipService)
        .details(provisioner, idToSettingAttributeMapping, idToServiceMapping);

    PageResponse<InfrastructureProvisionerDetails> infraProvisionerDetailsPageResponse =
        ipService.listDetails(infraProvisionerPageRequest, true, null, APP_ID);

    assertThat(infraProvisionerDetailsPageResponse.getResponse()).hasSize(1);
    assertThat(infraProvisionerDetailsPageResponse.getResponse().get(0)).isEqualTo(infrastructureProvisionerDetails);
  }
}
