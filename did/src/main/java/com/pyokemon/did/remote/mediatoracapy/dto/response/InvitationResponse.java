package com.pyokemon.did.remote.mediatoracapy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class InvitationResponse {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcaPyCreateMediatorInvitationResponse {

        String state;
        boolean trace;
        @JsonProperty("invi_msg_id")
        String inviMsgId;
        @JsonProperty("oob_id")
        String oobId;
        Invitation invitation;
        @JsonProperty("invitation_url")
        String invitationUrl;

        String status;
        String message;
        LocalDateTime createdAt;

        public static class Invitation{
                @JsonProperty("@type")
                String type;
                @JsonProperty("@id")
                String id;
                String label;
                @JsonProperty("handshake_protocols")
                List<String> handshakeProtocols;
                List<Service> services;
        }

        public static class Service{}
                String id;
                String type;
                List<String> recipientKeys;
                String serviceEndpoint;

    }
}
