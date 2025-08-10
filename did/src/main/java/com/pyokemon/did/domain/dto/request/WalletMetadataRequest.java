package com.pyokemon.did.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class WalletMetadataRequest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateWalletRequest {
        @NotNull
        @JsonProperty("tenant_id")
        private Long tenantId;
    }
}
