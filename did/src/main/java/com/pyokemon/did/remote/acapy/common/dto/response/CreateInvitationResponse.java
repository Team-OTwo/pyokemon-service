package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.AcaPyConnection.ConnectionStatus;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 초대장 생성 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationResponse implements AcaPyResponse {
    
    private String state;
    
    @JsonProperty("invi_msg_id")
    private String inviMsgId;
    
    private Invitation invitation;
    
    @JsonProperty("invitation_url")
    private String invitationUrl;
    
    /**
     * AcaPyConnection 엔티티로 변환
     */
    public AcaPyConnection toEntity(Long tenantId, Long userId) {
        return AcaPyConnection.builder()
                .connectionId(null)
                .inviMsgId(inviMsgId)
                .tenantId(tenantId)
                .userId(userId)
                .status(ConnectionStatus.PENDING)
                .build();
    }
}
