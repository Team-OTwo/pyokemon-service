package com.pyokemon.did.remote.commonAcaPy.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.AcaPyConnection.ConnectionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-PY OOB(Out of Band) 초대장 응답을 위한 DTO 그룹
 */
public class InvitationResponse {


  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Invitation {
    @JsonProperty("@type")
    private String type;

    @JsonProperty("@id")
    private String id;

    private String label;

    @JsonProperty("handshake_protocols")
    private List<String> handshakeProtocols;

    private List<String> services;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyCreateInvitationResponse {
    private String state;
    private Boolean trace;

    private String alias;

    @JsonProperty("oob_id")
    private String oobId;

    private Invitation invitation;

    @JsonProperty("invitation_url")
    private String invitationUrl;

    public AcaPyConnection toEntity(Long tenantId, Long userId) {
      return AcaPyConnection.builder().connectionId(null).alias(alias).tenantId(tenantId)
          .userId(userId).status(ConnectionStatus.PENDING).build();
    }
  }


  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class GetOobInvitationResponse {
    @JsonProperty("oob_id")
    private String oobId;

    private Invitation invitation;

    @JsonProperty("invitation_url")
    private String invitationUrl;

    private String state;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AcaPyReceiveInvitationResponse {

    private String state;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("@id")
    private String id;

    private String label;

    @JsonProperty("handshake_protocols")
    private List<String> handshakeProtocols;

    private List<String> accept;

    private List<String> services;

    @JsonProperty("use_did_method")
    private String useDidMethod;
  }
}
