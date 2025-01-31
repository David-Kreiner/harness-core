/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */
package io.harness.ng.core.environment.resources;

import static io.harness.rule.OwnerRule.vivekveman;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.EntityType;
import io.harness.beans.FeatureName;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import io.harness.utils.featureflaghelper.NGFeatureFlagHelperService;
import io.harness.yaml.validator.InvalidYamlException;
import io.harness.yaml.validator.YamlSchemaValidator;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EnvironmentEntityYamlSchemaHelperTest extends CategoryTest {
  @Mock NGFeatureFlagHelperService featureFlagHelperService;
  @Mock YamlSchemaValidator yamlSchemaValidator;
  @InjectMocks EnvironmentEntityYamlSchemaHelper environmentEntityYamlSchemaHelper;
  private final String ACCOUNT_ID = "account_id";
  private final ClassLoader classLoader = this.getClass().getClassLoader();
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = vivekveman)
  @Category(UnitTests.class)
  public void testValidateSchema() throws IOException {
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.DISABLE_CDS_SERVICE_ENV_SCHEMA_VALIDATION))
        .thenReturn(false);
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.NG_SVC_ENV_REDESIGN)).thenReturn(true);
    when(yamlSchemaValidator.processAndHandleValidationMessage(any(), any(), any())).thenReturn(Collections.emptySet());

    String yaml = readFile("ManifestYamlWithoutSpec.yaml");
    environmentEntityYamlSchemaHelper.validateSchema(ACCOUNT_ID, yaml);
    verify(yamlSchemaValidator, times(1)).validateWithDetailedMessage(yaml, EntityType.ENVIRONMENT);
    verify(yamlSchemaValidator, times(1)).processAndHandleValidationMessage(any(), any(), anyString());
  }

  @Test
  @Owner(developers = vivekveman)
  @Category(UnitTests.class)
  public void testServiceschemafeatureflagdisabledtest() throws IOException {
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.DISABLE_CDS_SERVICE_ENV_SCHEMA_VALIDATION))
        .thenReturn(true);
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.NG_SVC_ENV_REDESIGN)).thenReturn(true);
    when(yamlSchemaValidator.processAndHandleValidationMessage(any(), any(), any())).thenReturn(Collections.emptySet());

    String yaml = readFile("ManifestYamlWithoutSpec.yaml");
    environmentEntityYamlSchemaHelper.validateSchema(ACCOUNT_ID, yaml);
    verify(yamlSchemaValidator, times(0)).validateWithDetailedMessage(yaml, EntityType.ENVIRONMENT);
    verify(yamlSchemaValidator, times(0)).processAndHandleValidationMessage(any(), any(), anyString());
  }

  @Test
  @Owner(developers = vivekveman)
  @Category(UnitTests.class)
  public void testValidateSchemaForInvalidYamlException() throws IOException {
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.DISABLE_CDS_SERVICE_ENV_SCHEMA_VALIDATION))
        .thenReturn(false);
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.NG_SVC_ENV_REDESIGN)).thenReturn(true);

    String yaml = readFile("ManifestYamlWithoutSpec.yaml");
    when(yamlSchemaValidator.validateWithDetailedMessage(yaml, EntityType.ENVIRONMENT))
        .thenThrow(new InvalidYamlException("InvalidYamlException", null, null));

    assertThatThrownBy(() -> environmentEntityYamlSchemaHelper.validateSchema(ACCOUNT_ID, yaml))
        .isInstanceOf(InvalidYamlException.class)
        .hasMessageContaining("InvalidYamlException");
  }
  @Test
  @Owner(developers = vivekveman)
  @Category(UnitTests.class)
  public void testValidateSchemaForIOException() throws IOException {
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.DISABLE_CDS_SERVICE_ENV_SCHEMA_VALIDATION))
        .thenReturn(false);
    when(featureFlagHelperService.isEnabled(ACCOUNT_ID, FeatureName.NG_SVC_ENV_REDESIGN)).thenReturn(true);

    String yaml = readFile("ManifestYamlWithoutSpec.yaml");
    when(yamlSchemaValidator.validateWithDetailedMessage(yaml, EntityType.ENVIRONMENT))
        .thenThrow(new IOException("IOException"));

    assertThatThrownBy(() -> environmentEntityYamlSchemaHelper.validateSchema(ACCOUNT_ID, yaml))
        .isInstanceOf(InvalidYamlException.class)
        .hasMessageContaining("IOException");
  }

  private String readFile(String fileName) throws IOException {
    final URL testFile = classLoader.getResource(fileName);
    return Resources.toString(testFile, Charsets.UTF_8);
  }
}