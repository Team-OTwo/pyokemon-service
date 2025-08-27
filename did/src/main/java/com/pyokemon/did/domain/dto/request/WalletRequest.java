package com.pyokemon.did.domain.dto.request;

import com.pyokemon.did.domain.Wallet.AccountRole;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

public class WalletRequest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateWalletRequest {
    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @JsonProperty("role")
    private AccountRole accountRole;
  }
}
