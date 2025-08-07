package com.pyokemon.did.remote.mediator.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateMediatorInvitationRequest(
    @JsonProperty("handshake_protocols")
    List<String> handshakeProtocols,
    String goal,
    String alias
) {
    
    public static CreateMediatorInvitationRequest generate() {
        return new CreateMediatorInvitationRequest(
            List.of("https://didcomm.org/didexchange/1.0"),
            "To establish a connection with Mediator",
            "mediator connection"
        );
    }
    
    // 기본 생성자
    public CreateMediatorInvitationRequest() {
        this(
            List.of("https://didcomm.org/didexchange/1.0"),
            "To establish a connection with Mediator",
            "mediator connection"
        );
    }
}
