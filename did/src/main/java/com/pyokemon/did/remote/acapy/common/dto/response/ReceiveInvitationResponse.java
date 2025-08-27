package com.pyokemon.did.remote.acapy.common.dto.response;

import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 초대장 수락 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveInvitationResponse implements AcaPyResponse {
    
    private String state;
}
