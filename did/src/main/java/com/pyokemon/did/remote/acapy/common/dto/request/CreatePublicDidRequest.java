package com.pyokemon.did.remote.acapy.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py에 공개 DID 생성을 요청하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublicDidRequest implements AcaPyRequest {
    
    private String method;
    private Options options;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        
        @JsonProperty("public")
        private boolean public_;
        
        @JsonProperty("key_type")
        private String keyType;
    }
    
    /**
     * 특정 DID 메서드를 위한 공개 DID 생성 요청 생성
     */
    public static CreatePublicDidRequest forMethod(String method) {
        return CreatePublicDidRequest.builder()
                .method(method)
                .options(Options.builder()
                        .public_(true)
                        .keyType("ed25519")
                        .build())
                .build();
    }
}
