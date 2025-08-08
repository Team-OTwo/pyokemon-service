package com.pyokemon.did.service;

import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.tenant.dto.response.CreateTenantInvitationResponse;

/**
 * 초대장 관리 작업을 위한 서비스 인터페이스
 */
public interface InvitationService {
    
    /**
     * 미디에이터 초대장을 생성합니다.
     *
     * @return 미디에이터 초대장 응답
     */
    CreateMediatorInvitationResponse getMediatorInvitation();
    
    /**
     * 테넌트 초대장을 생성합니다.
     *
     * @return 테넌트 초대장 응답
     */
    CreateTenantInvitationResponse getTenantInvitation();
}