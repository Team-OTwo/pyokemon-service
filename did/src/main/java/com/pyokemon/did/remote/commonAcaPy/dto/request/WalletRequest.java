package com.pyokemon.did.remote.commonAcaPy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

public class WalletRequest {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyCreateWalletRequest {
    private String label;

    @JsonProperty("wallet_key")
    private String walletKey;

    @JsonProperty("wallet_name")
    private String walletName;

    @JsonProperty("wallet_type")
    private String walletType;

    @JsonProperty("wallet_dispatch_type")
    private String walletDispatchType;

    @JsonProperty("key_management_mode")
    private String keyManagementMode;

    /**
     * 특정 계정을 위한 지갑 요청을 생성하는 팩토리 메서드
     *
     * @param accountId 계정 식별자
     * @return 구성된 지갑 생성 요청
     */
    public static AcaPyCreateWalletRequest of(Long accountId) {
      return AcaPyCreateWalletRequest.builder()
          .label("{" + accountId + "}")
          .walletKey(UUID.randomUUID().toString())
          .walletName("wallet:" + accountId)
          .walletType("askar")
          .walletDispatchType("default")
          .keyManagementMode("managed")
          .build();
    }

  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyCreatePublicDidRequest {
    private String method;
    private Options options;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Options {
      @JsonProperty("public")
      private boolean public_;

      @JsonProperty("key_type")
      private String keyType;
    }

    public static AcaPyCreatePublicDidRequest of(String method) {
      return AcaPyCreatePublicDidRequest.builder()
          .method(method)
          .options(Options.builder()
              .public_(true)
              .keyType("ed25519")
              .build())
          .build();
    }
  }


}
