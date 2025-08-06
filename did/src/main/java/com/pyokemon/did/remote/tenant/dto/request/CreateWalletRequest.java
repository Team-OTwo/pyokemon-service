package com.pyokemon.did.remote.tenant.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public static CreateWalletRequest generate(String tenantId, String walletKey) {
        return new CreateWalletRequest(
            tenantId,
            walletKey,
            "wallet:" + tenantId,
            "askar",
            "default",
            "managed"
        );
    }
} 