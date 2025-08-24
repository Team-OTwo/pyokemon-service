package com.pyokemon.did.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MediatorInvitationResponse {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateMediatorInvitationResponse {
    private String invitationUrl;
  }
}
