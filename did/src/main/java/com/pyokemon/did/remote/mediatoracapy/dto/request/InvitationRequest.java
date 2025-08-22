package com.pyokemon.did.remote.mediatoracapy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class InvitationRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcaPyCreateMediatorInvitationRequest {

        @JsonProperty("handshake_protocols")
        List<String> handshakeProtocols;
        String goal;
        String alias;

        public static AcaPyCreateMediatorInvitationRequest of() {
            return new AcaPyCreateMediatorInvitationRequest(
                    List.of("https://didcomm.org/didexchange/1.0"),
                    "To establish a connection with Mediator",
                    "mediator connection"
            );
        }

    }
}

