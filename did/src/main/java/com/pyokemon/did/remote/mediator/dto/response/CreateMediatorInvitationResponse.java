package com.pyokemon.did.remote.mediator.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateMediatorInvitationResponse(
    String state,
    boolean trace,
    @JsonProperty("invi_msg_id")
    String inviMsgId,
    @JsonProperty("oob_id")
    String oobId,
    Invitation invitation,
    @JsonProperty("invitation_url")
    String invitationUrl,
    // 하위 호환성을 위한 필드들
    String status,
    String message,
    LocalDateTime createdAt
) {
    
    public record Invitation(
        @JsonProperty("@type")
        String type,
        @JsonProperty("@id")
        String id,
        String label,
        @JsonProperty("handshake_protocols")
        List<String> handshakeProtocols,
        List<Service> services
    ) {}
    
    public record Service(
        String id,
        String type,
        List<String> recipientKeys,
        String serviceEndpoint
    ) {}
}


