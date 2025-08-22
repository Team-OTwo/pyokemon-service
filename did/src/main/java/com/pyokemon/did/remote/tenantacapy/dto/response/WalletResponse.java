package com.pyokemon.did.remote.tenantacapy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.WalletMetadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class WalletResponse {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyCreateWalletResponse {
    @JsonProperty("wallet_id")
    private String walletId;

    private String token;

    /**
     * 응답을 WalletMetadata 엔티티로 변환
     *
     * @param tenantId 테넌트 식별자
     * @return 새 WalletMetadata 엔티티
     */
    public WalletMetadata toEntity(Long tenantId) {
      return WalletMetadata.builder().key(walletId).token(token).tenantId(tenantId).build();
    }
  }
}
