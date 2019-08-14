package software.wings.resources;

import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.harness.rest.RestResponse;
import io.swagger.annotations.Api;
import software.wings.beans.CyberArkConfig;
import software.wings.security.PermissionAttribute.PermissionType;
import software.wings.security.PermissionAttribute.ResourceType;
import software.wings.security.annotations.AuthRule;
import software.wings.security.annotations.Scope;
import software.wings.service.intfc.security.CyberArkService;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * @author marklu on 2019-08-01
 */
@Api("cyberark")
@Path("/cyberark")
@Produces("application/json")
@Scope(ResourceType.SETTING)
@AuthRule(permissionType = PermissionType.ACCOUNT_MANAGEMENT)
public class CyberArkResource {
  @Inject private CyberArkService cyberArkService;

  @POST
  @Timed
  @ExceptionMetered
  public RestResponse<String> saveCyberArkConfig(
      @QueryParam("accountId") final String accountId, CyberArkConfig cyberArkConfig) {
    return new RestResponse<>(cyberArkService.saveConfig(accountId, cyberArkConfig));
  }

  @DELETE
  @Timed
  @ExceptionMetered
  public RestResponse<Boolean> deleteCyberArkConfig(
      @QueryParam("accountId") final String accountId, @QueryParam("configId") final String configId) {
    return new RestResponse<>(cyberArkService.deleteConfig(accountId, configId));
  }
}
