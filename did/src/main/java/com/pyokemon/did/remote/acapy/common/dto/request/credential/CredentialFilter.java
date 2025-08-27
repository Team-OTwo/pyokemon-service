package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자격 증명 필터 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialFilter {
    
    @JsonProperty("ld_proof")
    private LdProof ldProof;
    
    /**
     * 표준 자격 증명으로 필터 생성
     */
    public static CredentialFilter withStandardCredential(StandardCredential credential) {
        return CredentialFilter.builder()
                .ldProof(LdProof.withStandardCredential(credential))
                .build();
    }
    
    /**
     * 증거가 포함된 자격 증명으로 필터 생성
     */
    public static CredentialFilter withEvidenceCredential(EvidenceCredential credential) {
        return CredentialFilter.builder()
                .ldProof(LdProof.withEvidenceCredential(credential))
                .build();
    }
}
