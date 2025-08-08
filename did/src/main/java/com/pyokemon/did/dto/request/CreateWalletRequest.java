package com.pyokemon.did.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for wallet creation request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateWalletRequestDto {
    @NotNull
    @JsonProperty("tenant_id")
    private Long tenantId;
}
