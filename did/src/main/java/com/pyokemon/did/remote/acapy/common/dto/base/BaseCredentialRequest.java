package com.pyokemon.did.remote.acapy.common.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 자격 증명(Credential) 관련 요청의 기본 추상 클래스 모든 자격 증명 요청은 이 클래스를 상속받아야 함
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseCredentialRequest implements AcaPyRequest {

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("auto_offer")
  private Boolean autoOffer;
}
