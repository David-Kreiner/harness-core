package io.harness.functional.users;

import static io.harness.rule.OwnerRule.NATARAJA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;

import io.harness.category.element.FunctionalTests;
import io.harness.functional.AbstractFunctionalTest;
import io.harness.rule.OwnerRule.Owner;
import io.harness.scm.ScmSecret;
import io.harness.scm.SecretName;
import io.harness.testframework.framework.Setup;
import io.harness.testframework.restutils.TwoFactorAuthRestUtils;
import io.harness.testframework.restutils.UserRestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.wings.beans.User;
import software.wings.security.authentication.TwoFactorAuthenticationSettings;

import java.util.List;

@Slf4j
public class TwoFactorAuthenticationTest extends AbstractFunctionalTest {
  @Inject ScmSecret scmSecret;
  String defaultUser = "default2fa@harness.io";
  String defaultPassword = "";

  @Test()
  @Owner(emails = NATARAJA, intermittent = false)
  @Category(FunctionalTests.class)
  public void verifyTwoFactorAuthLogin() {
    defaultPassword = scmSecret.decryptToString(new SecretName("user_default_password"));
    User user = Setup.retryLogin(defaultUser, defaultPassword);
    assertNotNull("User Object Should not be null", user.getToken());
    TwoFactorAuthenticationSettings otpSettings =
        TwoFactorAuthRestUtils.getOtpSettings(getAccount().getUuid(), user.getToken());
    user = TwoFactorAuthRestUtils.enableTwoFactorAuthentication(getAccount().getUuid(), user.getToken(), otpSettings);
    assertNotNull("User Object Should not be null", user.getEmail());
    Setup.signOut(user.getUuid(), user.getToken());
    user = TwoFactorAuthRestUtils.retryTwoFaLogin(
        defaultUser, defaultPassword, getAccount().getUuid(), otpSettings.getTotpSecretKey());
    assertNotNull("bearer token should not be null" + user.getToken());
    UserRestUtils urUtil = new UserRestUtils();
    List<User> userList = urUtil.getUserList(user.getToken(), getAccount().getUuid());
    assertTrue("Should be able to fetch the User list to ensure Login successfull with 2fa", userList.size() > 0);
    TwoFactorAuthRestUtils.disableTwoFactorAuthentication(getAccount().getUuid(), user.getToken());
    Setup.signOut(user.getUuid(), user.getToken());
    logger.info("Disabled 2FA Login");
    user = Setup.loginUser(defaultUser, defaultPassword);
    userList = urUtil.getUserList(user.getToken(), getAccount().getUuid());
    logger.info("Getting the User List to ensure 2fa login disabled");
    assertTrue("User List should not empty to ensure Two FA Authentication disabled", userList.size() > 0);
  }
}
