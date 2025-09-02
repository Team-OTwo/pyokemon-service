package com.pyokemon.account.remote.did;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterWalletRequest {
  @NotNull
  @JsonProperty("account_id")
  private Long accountId;

  @NotNull
  @JsonProperty("account_role")
  private AccountRole accountRole;

  public enum AccountRole {
    USER, TENANT
  }

  public static RegisterWalletRequest of(Long accountId, AccountRole accountRole) {
    return RegisterWalletRequest.builder().accountId(accountId).accountRole(accountRole).build();
  }
}
