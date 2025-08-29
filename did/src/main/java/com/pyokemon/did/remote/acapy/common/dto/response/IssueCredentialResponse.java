package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.IssuedVc.VcStatus;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 자격 증명 발급 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCredentialResponse implements AcaPyResponse {

  private String state;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @JsonProperty("cred_ex_id")
  private String credExId;

  /**
   * IssuedVc 엔티티로 변환
   */
  public IssuedVc toEntity(Long tenantId, Long userId, Long bookingId, String presExId,
      String verifyInviUrl) {
    return IssuedVc.builder().verifyInviUrl(verifyInviUrl).presExId(presExId).bookingId(bookingId)
        .userId(userId).tenantId(tenantId).status(VcStatus.PENDING).build();
  }
}
