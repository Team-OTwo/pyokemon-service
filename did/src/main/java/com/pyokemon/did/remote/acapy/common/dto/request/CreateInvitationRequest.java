package com.pyokemon.did.remote.acapy.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.base.BaseInvitationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * ACA-Py에 초대장 생성을 요청하기 위한 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateInvitationRequest extends BaseInvitationRequest {
    
    @JsonProperty("use_did_method")
    private String useDidMethod;
    
    @JsonProperty("use_public_did")
    private Boolean usePublicDid;
    
    /**
     * 사용자-디바이스 연결을 위한 초대장 요청 생성
     */
    public static CreateInvitationRequest forUserDevice(Long userId, String deviceId) {
        String trackingAlias = String.format(AcaPyConstants.Alias.USER_DEVICE_FORMAT, userId, deviceId);
        return createBaseInvitation(trackingAlias);
    }
    
    /**
     * 사용자-테넌트 연결을 위한 초대장 요청 생성
     */
    public static CreateInvitationRequest forUserTenant(Long userId, Long tenantId) {
        String trackingAlias = String.format(AcaPyConstants.Alias.USER_TENANT_FORMAT, userId, tenantId);
        return createBaseInvitation(trackingAlias);
    }
    
    /**
     * 기본 초대장 요청 생성
     */
    private static CreateInvitationRequest createBaseInvitation(String alias) {
        return CreateInvitationRequest.builder()
                .accept(List.of(AcaPyConstants.Protocol.DIDCOMM_AIP1, AcaPyConstants.Protocol.DIDCOMM_AIP2))
                .alias(alias)
                .handshakeProtocols(List.of(AcaPyConstants.Protocol.DID_EXCHANGE_V1))
                .protocolVersion("1.1")
                .useDidMethod(AcaPyConstants.DidMethod.PEER2)
                .usePublicDid(false)
                .build();
    }
}
