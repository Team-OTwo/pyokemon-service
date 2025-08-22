package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.TenantInvitationRequest;
import com.pyokemon.did.domain.dto.response.TenantInvitationResponse.CreateTenantInvitationResponse;

public interface TenantInvitationService {
    
    /**
     * 미디에이터 초대장을 생성합니다.
     *
     * @return 미디에이터 초대장 응답
     */
     CreateTenantInvitationResponse createTenantInvitation(TenantInvitationRequest.CreateTenantInvitationRequest request) ;

}