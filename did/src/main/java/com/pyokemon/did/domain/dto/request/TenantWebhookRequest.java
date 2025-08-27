package com.pyokemon.did.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TenantWebhookRequest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class HandleTenantConnectionsRequest {
    @NotNull
    private String state;

    @NotNull
    @JsonProperty("connection_id")
    private String connectionId;

    @NotNull
    @JsonProperty("invitation_msg_id")
    private String invitationMsgId;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class HandleTenantOOBRequest {
    @NotNull
    private String state;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("oob_id")
    private String oobId;
  }
}
