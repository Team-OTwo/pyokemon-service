package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 공개 DID 생성 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublicDidResponse implements AcaPyResponse {
    
    private String result;
    
    @JsonProperty("did")
    private String did;
    
    @JsonProperty("verkey")
    private String verkey;
    
    @JsonProperty("posture")
    private String posture;
    
    @JsonProperty("key_type")
    private String keyType;
    
    @JsonProperty("method")
    private String method;
}
