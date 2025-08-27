package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자격 증명의 증거(Evidence) 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {
    private String type;
    
    @JsonProperty("sourceCredentialId")
    private String sourceCredentialId;
    
    /**
     * 특정 예매 ID에 대한 파생 증거 생성
     */
    public static Evidence derivedFrom(Long bookingId) {
        return Evidence.builder()
                .type("DerivedFrom")
                .sourceCredentialId("urn:booking:" + bookingId)
                .build();
    }
}
