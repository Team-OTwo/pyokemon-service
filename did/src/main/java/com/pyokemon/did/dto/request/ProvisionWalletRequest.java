package com.pyokemon.did.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 테넌트 지갑 프로비저닝 요청을 위한 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvisionWalletRequest {
    @NotNull
    @JsonProperty("tenant_id")
    private Long tenantId;
}