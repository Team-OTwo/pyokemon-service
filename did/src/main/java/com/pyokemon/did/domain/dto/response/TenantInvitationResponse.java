package com.pyokemon.did.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TenantInvitationResponse {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TenantInvitation {
    private Long tenantId;
    private String invitationUrl;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateTenantInvitationResponse {
    private List<TenantInvitation> invitations;
  }
}
