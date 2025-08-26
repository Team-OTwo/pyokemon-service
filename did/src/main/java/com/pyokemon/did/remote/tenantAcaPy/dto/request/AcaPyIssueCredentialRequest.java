package com.pyokemon.did.remote.tenantAcaPy.dto.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-PY에 VC 발급을 요청하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaPyIssueCredentialRequest {
    
    @JsonProperty("connection_id")
    private String connectionId;
    
    private String comment;
    
    @JsonProperty("auto_offer")
    private Boolean autoOffer;
    
    private Filter filter;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        
        @JsonProperty("ld_proof")
        private LdProof ldProof;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LdProof {
        
        private Credential credential;
        private Options options;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credential {
        
        @JsonProperty("@context")
        private List<Object> context;
        
        private String id;
        private List<String> type;
        private String issuer;
        
        @JsonProperty("issuanceDate")
        private String issuanceDate;
        
        @JsonProperty("credentialSubject")
        private CredentialSubject credentialSubject;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialSubject {
        private String id;
        
        @JsonProperty("booking_id")
        private String bookingId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        
        @JsonProperty("proofType")
        private String proofType;
    }
    
    /**
     * 예매 VC 발급을 위한 기본 설정으로 생성
     */
    public static AcaPyIssueCredentialRequest of(String connectionId, String bookingId,
                                                         String issuerDid, String subjectDid) {
        return AcaPyIssueCredentialRequest.builder()
                .connectionId(connectionId)
                .comment("예매 VC 발급")
                .autoOffer(true)
                .filter(Filter.builder()
                        .ldProof(LdProof.builder()
                                .credential(Credential.builder()
                                        .context(List.of(
                                                "https://www.w3.org/2018/credentials/v1",
                                                "https://w3id.org/security/suites/ed25519-2020/v1",
                                                Map.of("booking_id", "https://schema.org/text")
                                        ))
                                        .id(bookingId)
                                        .type(List.of("VerifiableCredential"))
                                        .issuer(issuerDid)
                                        .issuanceDate(java.time.Instant.now().toString())
                                        .credentialSubject(CredentialSubject.builder()
                                                .id(subjectDid)
                                                .bookingId(bookingId)
                                                .build())
                                        .build())
                                .options(Options.builder()
                                        .proofType("Ed25519Signature2020")
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
