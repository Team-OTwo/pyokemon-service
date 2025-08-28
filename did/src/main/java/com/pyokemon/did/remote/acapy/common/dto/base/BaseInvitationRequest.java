package com.pyokemon.did.remote.acapy.common.dto.base;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 초대장(Invitation) 관련 요청의 기본 추상 클래스 모든 초대장 요청은 이 클래스를 상속받아야 함
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseInvitationRequest implements AcaPyRequest {

  private List<String> accept;

  private String alias;

  @JsonProperty("handshake_protocols")
  private List<String> handshakeProtocols;

  @JsonProperty("protocol_version")
  private String protocolVersion;
}
