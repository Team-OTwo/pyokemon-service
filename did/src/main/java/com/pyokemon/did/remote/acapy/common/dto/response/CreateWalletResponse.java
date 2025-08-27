package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 지갑 생성 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletResponse implements AcaPyResponse {
    
    @JsonProperty("wallet_id")
    private String walletId;
    
    @JsonProperty("created")
    private boolean created;
    
    @JsonProperty("token")
    private String token;
}
