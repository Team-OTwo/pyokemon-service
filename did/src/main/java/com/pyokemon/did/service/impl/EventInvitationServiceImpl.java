package com.pyokemon.did.service.impl;

import com.pyokemon.did.domain.dto.request.EventInvitationRequest.CreateEventInvitationRequest;
import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.service.EventInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventInvitationServiceImpl implements EventInvitationService {

    /**
     * 미디에이터 초대장을 생성합니다.
     *
     * @return 미디에이터 초대장 응답
     */
    @Override
    public CreateMediatorInvitationResponse getMediatorInvitation() {
        log.info("미디에이터 초대장 생성 요청");
        
        // TODO: 실제 미디에이터 초대장 생성 로직 구현
        // 현재는 기본 응답을 반환
        
        log.info("미디에이터 초대장 생성 완료");
        return new CreateMediatorInvitationResponse(
                "initial",
                false,
                "test-invi-msg-id",
                "test-oob-id",
                null,
                "http://localhost:8000/mediator-invitation",
                "created",
                "Mediator invitation created successfully",
                java.time.LocalDateTime.now()
        );
    }

    /**
     * 이벤트 초대장을 생성합니다.
     *
     * @param request 이벤트 초대장 생성 요청
     */
    @Override
    public void createEventInvitation(CreateEventInvitationRequest request) {
        log.info("이벤트 초대장 생성 요청: {}", request);
        
        // TODO: 실제 이벤트 초대장 생성 로직 구현
        // 현재는 로그만 출력
        
        log.info("이벤트 초대장 생성 완료");
    }
}