package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 자격 증명(Credential)의 기본 추상 클래스 모든 자격 증명은 이 클래스를 상속받아야 함
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseCredential {
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
