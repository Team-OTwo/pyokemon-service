package com.pyokemon.did.remote.tenant.dto.request;

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

    /**
     * OOB 초대장 생성 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcaPyCreateInvitationRequest {
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

        /**
         * 초대장 요청 객체를 생성하는 팩토리 메서드
         *
         * @param eventId 이벤트 식별자
         * @return 생성된 초대장 요청 객체
         */
        public static AcaPyCreateInvitationRequest generate(Long eventId) {
            return AcaPyCreateInvitationRequest.builder()
                    .accept(List.of("didcomm/aip1", "didcomm/aip2;env=rfc19"))
                    .alias("invitation:" + eventId)
                    .goal("To create OOB invitation with event_id: " + eventId)
                    .goal_code("issue-vc")
                    .handshakeProtocols(List.of("https://didcomm.org/didexchange/1.0"))
                    .myLabel("invitation:" + eventId)
                    .protocolVersion("1.1")
                    .useDidMethod("did:peer:2")
                    .usePublicDid(false)
                    .build();
        }
    }
    
    /**
     * OOB 초대장 조회 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetOobInvitation {
        @JsonProperty("invitation_id")
        private String invitationId;
    }
}
