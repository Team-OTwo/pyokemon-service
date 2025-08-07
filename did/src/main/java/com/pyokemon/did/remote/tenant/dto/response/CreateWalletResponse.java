package com.pyokemon.did.remote.tenant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateWalletResponse(
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    @JsonProperty("updated_at")
    LocalDateTime updatedAt,
    @JsonProperty("wallet_id")
    String walletId,
    @JsonProperty("key_management_mode")
    String keyManagementMode,
    Map<String, Object> settings,
    String token
) {} 