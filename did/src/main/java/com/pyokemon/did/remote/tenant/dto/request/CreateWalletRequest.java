package com.pyokemon.did.remote.tenant.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ACA-PY에서 지갑 생성을 위한 요청 객체
 */
public record CreateWalletRequest(
    String label,
    @JsonProperty("wallet_key")
    String walletKey,
    @JsonProperty("wallet_name")
    String walletName,
    @JsonProperty("wallet_type")
    String walletType,
    @JsonProperty("wallet_dispatch_type")
    String walletDispatchType,
    @JsonProperty("key_management_mode")
    String keyManagementMode
) {
    /**
     * 특정 테넌트를 위한 지갑 요청을 생성하는 팩토리 메서드
     * 
     * @param tenantId 테넌트 식별자
     * @param walletKey 지갑 암호화 키
     * @return 구성된 지갑 생성 요청
     */
    public static CreateWalletRequest create(Long tenantId, String walletKey) {
        String tenantIdStr = String.valueOf(tenantId);
        return new CreateWalletRequest(
            tenantIdStr,
            walletKey,
            "wallet:" + tenantIdStr,
            "askar",
            "default",
            "managed"
        );
    }
} 