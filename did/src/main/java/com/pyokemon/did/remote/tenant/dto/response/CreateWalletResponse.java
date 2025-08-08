package com.pyokemon.did.remote.tenant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.WalletMetadata;

/**
 * ACA-PY에서 지갑 생성 응답
 */
public record CreateWalletResponse(
    @JsonProperty("wallet_id")
    String walletId,
    String token
) {
    /**
     * 응답을 WalletMetadata 엔티티로 변환
     *
     * @param tenantId 테넌트 식별자
     * @return 새 WalletMetadata 엔티티
     */
    public WalletMetadata toEntity(Long tenantId) {
        return WalletMetadata.builder()
                .key(walletId)
                .token(token)
                .tenantId(tenantId)
                .build();
    }
}