package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LD Proof 옵션 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdProofOptions {
    @JsonProperty("proofType")
    private String proofType;
    
    /**
     * 기본 Ed25519 서명 타입으로 옵션 생성
     */
    public static LdProofOptions createDefault() {
        return LdProofOptions.builder()
                .proofType(AcaPyConstants.Credential.PROOF_TYPE_ED25519)
                .build();
    }
}
