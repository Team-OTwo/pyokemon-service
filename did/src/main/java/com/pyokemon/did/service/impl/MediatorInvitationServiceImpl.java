package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.dto.response.MediatorInvitationResponse.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.mediatoracapy.RemoteMediatorAcaPyService;
import com.pyokemon.did.remote.mediatoracapy.dto.request.InvitationRequest.AcaPyCreateMediatorInvitationRequest;
import com.pyokemon.did.remote.mediatoracapy.dto.response.InvitationResponse.AcaPyCreateMediatorInvitationResponse;
import com.pyokemon.did.service.MediatorInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediatorInvitationServiceImpl implements MediatorInvitationService {


    private final RemoteMediatorAcaPyService remoteMediatorAcaPyService;

    @Override
    public CreateMediatorInvitationResponse getMediatorInvitation() {
        try {
            AcaPyCreateMediatorInvitationResponse response = remoteMediatorAcaPyService.createInvitation(AcaPyCreateMediatorInvitationRequest.of());

            return CreateMediatorInvitationResponse.builder()
                    .invitationUrl(response.getInvitationUrl())
                    .build();


        } catch (Exception e) {
            log.error("미디에이터 초대장 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException("미디에이터 초대장 생성에 실패했습니다.", DidErrorCodes.INVITATION_CREATION_FAILED, e);
        }
    }
}
