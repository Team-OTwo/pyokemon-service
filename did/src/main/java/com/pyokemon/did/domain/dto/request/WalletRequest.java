package com.pyokemon.did.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.Wallet.AccountRole;

import lombok.*;

public class WalletRequest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RegisterWalletRequest {
    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @JsonProperty("account_role")
    private AccountRole accountRole;
  }
}
