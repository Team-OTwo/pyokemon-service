package com.pyokemon.did.domain.dto.request.webhook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
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

  @NotNull
  @JsonProperty("by_format")
  private static ByFormat byFormat;

  public Long extractBookingId() {
    // credentialId = "urn:booking:{bookingId}" 형식에서 bookingId를 추출
    String credentialId = byFormat.credOffer.ldProof.credential.id;
    // 배열의 세 번째 요소(인덱스 2)가 {bookingId}에 해당
    // 이를 Long 타입으로 변환하여 반환
    return Long.parseLong(credentialId.split(":")[2]);
  }

  @Data
  public static class ByFormat {
    @NotNull
    @JsonProperty("cred_offer")
    private CredOffer credOffer;
  }

  @Data
  public static class CredOffer {
    @NotNull
    @JsonProperty("ld_proof")
    private LdProof ldProof;
  }

  @Data
  public static class LdProof {
    @NotNull
    private Credential credential;
  }

  @Data
  public static class Credential {
    @NotNull
    private String id;
  }


}
