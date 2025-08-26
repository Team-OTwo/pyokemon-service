package com.pyokemon.did.remote.tenantAcaPy.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py의 /issue-credential-2.0/send API 응답을 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaPyIssueCredentialResponse {
    
    private String state;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    private Boolean trace;
    
    @JsonProperty("cred_ex_id")
    private String credExId;
    
    @JsonProperty("connection_id")
    private String connectionId;
    
    @JsonProperty("thread_id")
    private String threadId;
    
    private String initiator;
    
    private String role;
    
    @JsonProperty("cred_proposal")
    private CredentialProposal credProposal;
    
    @JsonProperty("cred_offer")
    private CredentialOffer credOffer;
    
    @JsonProperty("by_format")
    private ByFormat byFormat;
    
    @JsonProperty("auto_offer")
    private Boolean autoOffer;
    
    @JsonProperty("auto_issue")
    private Boolean autoIssue;
    
    @JsonProperty("auto_remove")
    private Boolean autoRemove;
    
    // --- Nested Classes ---
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialProposal {
        @JsonProperty("@type")
        private String type;
        
        @JsonProperty("@id")
        private String id;
        
        private String comment;
        
        private List<Format> formats;
        
        @JsonProperty("filters~attach")
        private List<FilterAttachment> filtersAttach;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialOffer {
        @JsonProperty("@type")
        private String type;
        
        @JsonProperty("@id")
        private String id;
        
        @JsonProperty("~thread")
        private Object thread;
        
        private String comment;
        
        private List<Format> formats;
        
        @JsonProperty("offers~attach")
        private List<OfferAttachment> offersAttach;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Format {
        @JsonProperty("attach_id")
        private String attachId;
        
        private String format;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterAttachment {
        @JsonProperty("@id")
        private String id;
        
        @JsonProperty("mime-type")
        private String mimeType;
        
        private DataWrapper data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfferAttachment {
        @JsonProperty("@id")
        private String id;
        
        @JsonProperty("mime-type")
        private String mimeType;
        
        private DataWrapper data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataWrapper {
        private String base64;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ByFormat {
        @JsonProperty("cred_proposal")
        private LdProofFormat credProposal;
        
        @JsonProperty("cred_offer")
        private LdProofFormat credOffer;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LdProofFormat {
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
        
        private String data;
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
     * VC 발급 상태가 완료되었는지 확인
     */
    public boolean isCompleted() {
        return "credential-issued".equals(state) || "done".equals(state);
    }
    
    /**
     * VC 발급 상태가 진행 중인지 확인
     */
    public boolean isInProgress() {
        return "offer-sent".equals(state) || "request-received".equals(state) || "credential-issued".equals(state);
    }
    
    /**
     * VC 발급 상태가 실패했는지 확인
     */
    public boolean isFailed() {
        return "abandoned".equals(state) || "credential-revoked".equals(state);
    }
}
