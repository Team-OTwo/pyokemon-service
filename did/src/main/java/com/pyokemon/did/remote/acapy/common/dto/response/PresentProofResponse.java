package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 증명 제시 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentProofResponse implements AcaPyResponse {
    
    private String state;
    
    @JsonProperty("pres_ex_id")
    private String presExId;
}
