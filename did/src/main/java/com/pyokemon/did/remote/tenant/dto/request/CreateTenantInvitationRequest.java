package com.pyokemon.did.remote.tenant.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateTenantInvitationRequest(
    List<String> accept,
    String alias,
    String goal,
    @JsonProperty("handshake_protocols")
    List<String> handshakeProtocols,
    @JsonProperty("my_label")
    String myLabel,
    @JsonProperty("protocol_version")
    String protocolVersion,
    @JsonProperty("use_did_method")
    String useDidMethod,
    @JsonProperty("use_public_did")
    Boolean usePublicDid
) {
    
    public static CreateTenantInvitationRequest generate(String alias, String myLabel) {
        return new CreateTenantInvitationRequest(
            List.of("didcomm/aip1", "didcomm/aip2;env=rfc19"),
            alias,
            "To establish a connection with Booking",
            List.of("https://didcomm.org/didexchange/1.0"),
            myLabel,
            "1.1",
            "did:peer:2",
            false
        );
    }

    public static CreateTenantInvitationRequest generate() {
        return new CreateTenantInvitationRequest(
                List.of("didcomm/aip1", "didcomm/aip2;env=rfc19"),
                "Barry3",
                "To establish a connection with Booking",
                List.of("https://didcomm.org/didexchange/1.0"),
                "Invitation to Barry",
                "1.1",
                "did:peer:2",
                false
        );
    }
    
    // 기본 생성자
    public CreateTenantInvitationRequest() {
        this(
            List.of("didcomm/aip1", "didcomm/aip2;env=rfc19"),
            "Barry3",
            "To establish a connection with Booking",
            List.of("https://didcomm.org/didexchange/1.0"),
            "Invitation to Barry",
            "1.1",
            "did:peer:2",
            false
        );
    }
}
