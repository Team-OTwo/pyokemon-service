package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.EventInvitationRequest.CreateEventInvitationRequest;
import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;

public interface EventInvitationService {
    
    /**
     * 미디에이터 초대장을 생성합니다.
     *
     * @return 미디에이터 초대장 응답
     */
    CreateMediatorInvitationResponse getMediatorInvitation();

    void createEventInvitation(CreateEventInvitationRequest request);

}