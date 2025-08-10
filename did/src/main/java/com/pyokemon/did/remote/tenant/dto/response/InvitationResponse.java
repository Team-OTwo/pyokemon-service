package com.pyokemon.did.remote.tenant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ACA-PY OOB(Out of Band) 초대장 응답을 위한 DTO 그룹
 */
public class InvitationResponse {

    /**
     * 초대장 상세 정보 DTO
     */
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

    /**
     * OOB 초대장 생성 응답 DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcaPyCreateInvitationResponse {
        private String state;
        private boolean trace;

        @JsonProperty("invi_msg_id")
        private String inviMsgId;

        @JsonProperty("oob_id")
        private String oobId;

        private Invitation invitation;

        @JsonProperty("invitation_url")
        private String invitationUrl;
    }
    
    /**
     * OOB 초대장 조회 응답 DTO
     */
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
}
