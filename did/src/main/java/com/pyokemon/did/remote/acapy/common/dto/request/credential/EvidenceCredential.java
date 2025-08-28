package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 증거(Evidence)가 포함된 자격 증명
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EvidenceCredential extends BaseCredential {
    
    @JsonProperty("evidence")
    private List<Evidence> evidence;

    /**
     * 자격 증명의 증거(Evidence) 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Evidence {
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
}
