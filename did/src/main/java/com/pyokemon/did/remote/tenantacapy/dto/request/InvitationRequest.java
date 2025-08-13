package com.pyokemon.did.remote.tenantacapy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ACA-PY에 OOB(Out of Band) 초대장 생성을 요청하기 위한 DTO 그룹
 */
public class InvitationRequest {


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcaPyCreateTenantInvitationRequest {
        private List<String> accept;
        
        private String alias;
        
        private String goal;

        private String goal_code;
        
        @JsonProperty("handshake_protocols")
        private List<String> handshakeProtocols;
        
        @JsonProperty("my_label")
        private String myLabel;
        
        @JsonProperty("protocol_version")
        private String protocolVersion;
        
        @JsonProperty("use_did_method")
        private String useDidMethod;
        
        @JsonProperty("use_public_did")
        private Boolean usePublicDid;


        public static AcaPyCreateTenantInvitationRequest of(Long tenantId) {
            return AcaPyCreateTenantInvitationRequest.builder()
                    .accept(List.of("didcomm/aip1", "didcomm/aip2;env=rfc19"))
                    .alias("invitation:" + tenantId)
                    .goal("To create OOB invitation with event_id: " + tenantId)
                    .goal_code("issue-vc")
                    .handshakeProtocols(List.of("https://didcomm.org/connections/1.0"))
                    .myLabel("invitation:" + tenantId)
                    .protocolVersion("1.1")
                    .useDidMethod("did:peer:2")
                    .usePublicDid(false)
                    .build();
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
