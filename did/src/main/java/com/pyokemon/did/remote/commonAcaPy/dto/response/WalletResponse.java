package com.pyokemon.did.remote.commonAcaPy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.Wallet.AccountRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class WalletResponse {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyCreateWalletResponse {
    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("wallet_id")
    private String walletId;

    @JsonProperty("key_management_mode")
    private String keyManagementMode;

    private Settings settings;

    private String token;


  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Settings {
    @JsonProperty("wallet.type")
    private String walletType;

    @JsonProperty("wallet.name")
    private String walletName;

    @JsonProperty("wallet.webhook_urls")
    private String[] walletWebhookUrls;

    @JsonProperty("wallet.dispatch_type")
    private String walletDispatchType;

    @JsonProperty("default_label")
    private String defaultLabel;

    @JsonProperty("wallet.id")
    private String walletId;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyCreatePublicDidResponse {
    private Result result;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
      private String did;
      private String verkey;
      private String posture;

      @JsonProperty("key_type")
      private String keyType;

      private String method;
      private Object metadata;
    }

    public Wallet toEntity(Long accountId, AccountRole accountRole, String token) {
      return Wallet.builder().accountId(accountId).accountRole(accountRole).token(token)
          .publicDid(result.getDid()).publicVerKey(result.verkey).build();
    }
  }
}
