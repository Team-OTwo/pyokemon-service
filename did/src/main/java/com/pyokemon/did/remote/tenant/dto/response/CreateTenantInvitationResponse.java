package com.pyokemon.did.remote.tenant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record CreateTenantInvitationResponse(
    String state,
    boolean trace,
    @JsonProperty("invi_msg_id")
    String inviMsgId,
    @JsonProperty("oob_id")
    String oobId,
    Invitation invitation,
    @JsonProperty("invitation_url")
    String invitationUrl
) {
    
    public record Invitation(
        @JsonProperty("@type")
        String type,
        @JsonProperty("@id")
        String id,
        String label,
        @JsonProperty("handshake_protocols")
        List<String> handshakeProtocols,
        List<String> services
    ) {}
}


