package com.pyokemon.did.remote.acapy.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.Invitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ACA-Py에 초대장 수락을 요청하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveInvitationRequest implements AcaPyRequest {
    
    @JsonProperty("@type")
    private String type;
    
    @JsonProperty("@id")
    private String id;
    
    private String label;
    
    @JsonProperty("handshake_protocols")
    private List<String> handshakeProtocols;
    
    private List<String> services;
    
    @JsonProperty("use_did_method")
    private String useDidMethod;
    
    /**
     * 초대장 정보를 기반으로 수락 요청 생성
     */
    public static ReceiveInvitationRequest fromInvitation(Invitation invitation) {
        return ReceiveInvitationRequest.builder()
                .type(invitation.getType())
                .id(invitation.getId())
                .label(invitation.getLabel())
                .handshakeProtocols(invitation.getHandshakeProtocols())
                .services(invitation.getServices())
                .useDidMethod(AcaPyConstants.DidMethod.PEER2)
                .build();
    }
}
