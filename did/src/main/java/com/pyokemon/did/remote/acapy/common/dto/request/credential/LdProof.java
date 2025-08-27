package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LD Proof 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdProof {
    private BaseCredential credential;
    private LdProofOptions options;
    
    /**
     * 표준 자격 증명으로 LdProof 생성
     */
    public static LdProof withStandardCredential(StandardCredential credential) {
        return LdProof.builder()
                .credential(credential)
                .options(LdProofOptions.createDefault())
                .build();
    }
    
    /**
     * 증거가 포함된 자격 증명으로 LdProof 생성
     */
    public static LdProof withEvidenceCredential(EvidenceCredential credential) {
        return LdProof.builder()
                .credential(credential)
                .options(LdProofOptions.createDefault())
                .build();
    }
}
