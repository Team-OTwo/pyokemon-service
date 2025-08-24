package com.pyokemon.did.remote.commonAcaPy.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-PY에 OOB(Out of Band) 초대장 생성을 요청하기 위한 DTO 그룹
 */
public class InvitationRequest {


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AcaPyCreateInvitationRequest {
    private List<String> accept;

    private String alias;

    @JsonProperty("handshake_protocols")
    private List<String> handshakeProtocols;


    @JsonProperty("protocol_version")
    private String protocolVersion;

    @JsonProperty("use_did_method")
    private String useDidMethod;

    @JsonProperty("use_public_did")
    private Boolean usePublicDid;

    public static AcaPyCreateInvitationRequest of(Long userId, String deviceId) {
      String trackingAlias = String.format("credo:user:%d#device:%s", userId, deviceId);
      return AcaPyCreateInvitationRequest.builder()
              .accept(List.of("didcomm/aip1", "didcomm/aip2;env=rfc19")).alias(trackingAlias)
              .handshakeProtocols(List.of("https://didcomm.org/didexchange/1.1")).protocolVersion("1.1")
              .useDidMethod("did:peer:2").usePublicDid(false).build();
    }

    public static AcaPyCreateInvitationRequest of(Long userId, Long tenantId) {
      String trackingAlias = String.format("acapy:user:%d#tenant:%d", userId, tenantId);
      return AcaPyCreateInvitationRequest.builder()
              .accept(List.of("didcomm/aip1", "didcomm/aip2;env=rfc19")).alias(trackingAlias)
              .handshakeProtocols(List.of("https://didcomm.org/didexchange/1.1")).protocolVersion("1.1")
              .useDidMethod("did:peer:2").usePublicDid(false).build();
    }
  }


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GetOobInvitation {
    @JsonProperty("invitation_id")
    private String invitationId;
  }
}