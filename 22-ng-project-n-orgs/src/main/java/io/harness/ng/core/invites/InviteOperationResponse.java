package io.harness.ng.core.invites;

public enum InviteOperationResponse {
  ACCOUNT_INVITE_ACCEPTED_NEED_PASSWORD("ACCOUNT_INVITE_ACCEPTED_NEED_PASSWORD"),
  ACCOUNT_INVITE_ACCEPTED("ACCOUNT_INVITE_ACCEPTED"),
  USER_INVITED_SUCCESSFULLY("USER_INVITED_SUCCESSFULLY"),
  USER_ALREADY_ADDED("USER_ALREADY_ADDED"),
  USER_ALREADY_INVITED("USER_ALREADY_INVITED"),
  USER_REQUEST_REGISTERED("USER_REQUEST_REGISTERED"),
  FAIL("FAIL");

  private String type;
  InviteOperationResponse(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}