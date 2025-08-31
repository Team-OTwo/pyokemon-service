package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
    public static Evidence derivedFrom(String sourceCredentialId) {
      return Evidence.builder().type(AcaPyConstants.Evidence.DERIVED_FROME)
          .sourceCredentialId(sourceCredentialId).build();
    }
  }
}
