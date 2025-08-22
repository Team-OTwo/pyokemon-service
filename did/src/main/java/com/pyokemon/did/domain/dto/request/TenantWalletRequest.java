package com.pyokemon.did.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

public class TenantWalletRequest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateWalletRequest {
    @NotNull
    @JsonProperty("tenant_id")
    private Long tenantId;
  }
}
