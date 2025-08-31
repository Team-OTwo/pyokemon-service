package com.pyokemon.did.domain.dto.request.webhook;


import com.pyokemon.common.exception.code.DidErrorCodes;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.common.exception.BusinessException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueCredentialWebhookRequest {
  @NotNull
  @JsonProperty("cred_ex_id")
  private String credExId;

  @NotNull
  private String role;

  @NotNull
  private String state;

  @JsonProperty("by_format")  // static 제거
  private ByFormat byFormat;

  public Long extractBookingId() {
    if (byFormat == null || byFormat.getCredOffer() == null ||
            byFormat.getCredOffer().getLdProof() == null ||
            byFormat.getCredOffer().getLdProof().getCredential() == null) {
      throw new BusinessException("Credential 정보가 누락되었습니다", DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    // credentialId = "urn:booking:{bookingId}" 형식에서 bookingId를 추출
    String credentialId = byFormat.getCredOffer().getLdProof().getCredential().getId();
    if (credentialId == null || credentialId.trim().isEmpty()) {
      throw new BusinessException("Credential ID가 누락되었습니다", DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    String[] parts = credentialId.split(":");
    if (parts.length < 3) {
      throw new BusinessException("Credential ID 형식이 올바르지 않습니다: " + credentialId, DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    // 배열의 세 번째 요소(인덱스 2)가 {bookingId}에 해당
    return Long.parseLong(parts[2]);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ByFormat {
    @JsonProperty("cred_offer")
    private CredOffer credOffer;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CredOffer {
    @JsonProperty("ld_proof")
    private LdProof ldProof;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LdProof {
    private Credential credential;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Credential {
    private String id;
  }
}