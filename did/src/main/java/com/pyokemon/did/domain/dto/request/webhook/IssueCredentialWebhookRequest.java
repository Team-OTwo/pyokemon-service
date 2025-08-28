package com.pyokemon.did.domain.dto.request.webhook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueCredentialWebhookRequest {

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("cred_ex_id")
  private String credExId;

  private String role;
  private String initiator;

  @JsonProperty("auto_offer")
  private Boolean autoOffer;

  @JsonProperty("auto_issue")
  private Boolean autoIssue;

  @JsonProperty("auto_remove")
  private Boolean autoRemove;

  @JsonProperty("thread_id")
  private String threadId;

  private String state;

  @JsonProperty("by_format")
  private ByFormat byFormat;

  private Boolean trace;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @Data
  public static class ByFormat {
    @JsonProperty("cred_offer")
    private CredOffer credOffer;

    @JsonProperty("cred_request")
    private CredRequest credRequest;

    @JsonProperty("cred_issue")
    private CredIssue credIssue;
  }

  @Data
  public static class CredOffer {
    @JsonProperty("ld_proof")
    private LdProof ldProof;
  }

  @Data
  public static class CredRequest {
    @JsonProperty("ld_proof")
    private LdProof ldProof;
  }

  @Data
  public static class CredIssue {
    @JsonProperty("ld_proof")
    private LdProof ldProof;
  }

  @Data
  public static class LdProof {
    private Credential credential;
    private Options options;
    private Proof proof;
  }

  @Data
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
  public static class CredentialSubject {
    private String id;

    @JsonProperty("booking_id")
    private String bookingId;
  }

  @Data
  public static class Options {
    @JsonProperty("proofType")
    private String proofType;
  }

  @Data
  public static class Proof {
    private String type;

    @JsonProperty("proofPurpose")
    private String proofPurpose;

    @JsonProperty("verificationMethod")
    private String verificationMethod;

    private String created;

    @JsonProperty("proofValue")
    private String proofValue;
  }
}
