package io.harness.data.validator;

import static io.harness.data.validator.EntityNameValidator.ALLOWED_CHARS_SERVICE_VARIABLE_MESSAGE;
import static io.harness.data.validator.EntityNameValidator.ALLOWED_CHARS_SERVICE_VARIABLE_STRING;
import static io.harness.rule.OwnerRule.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.OwnerRule.Owner;
import lombok.Builder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class EntityNameTest extends CategoryTest {
  @Builder
  static class EntityNameTestStructure {
    @EntityName String str;
  }

  @Builder
  static class EntityNameServicaVariableTestStructure {
    @EntityName(charSetString = ALLOWED_CHARS_SERVICE_VARIABLE_STRING, message = ALLOWED_CHARS_SERVICE_VARIABLE_MESSAGE)
    private String str;
  }

  @Test
  @Owner(emails = UNKNOWN)
  @Category(UnitTests.class)
  public void testAllowedCharSet() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    String nonAllowed = "!@#$%^&*()+=\\/[]{}|~";
    for (char ch : nonAllowed.toCharArray()) {
      assertThat(validator.validate(EntityNameTestStructure.builder().str(String.format("foo%c", ch)).build()))
          .isNotEmpty();
    }
    String allowed = "_- ";
    for (char ch : allowed.toCharArray()) {
      assertThat(validator.validate(EntityNameTestStructure.builder().str(String.format("foo%c", ch)).build()))
          .isEmpty();
    }
    assertThat(validator.validate(EntityNameServicaVariableTestStructure.builder().str(String.format("foo%c", '-'))))
        .isEmpty();
  }
}
