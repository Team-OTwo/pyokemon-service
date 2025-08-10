package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.dto.request.EventInvitationRequest.CreateEventInvitationRequest;
import com.pyokemon.did.domain.repository.EventInvitationRepository;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.remote.mediator.MediatorAcapyClient;
import com.pyokemon.did.remote.mediator.dto.request.CreateMediatorInvitationRequest;
import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.tenant.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.tenant.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.tenant.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.service.EventInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 초대장 관리 작업을 위한 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventInvitationServiceImpl implements EventInvitationService {

    private final MediatorAcapyClient mediatorAcapyClient;
    private final RemoteTenantAcaPyService remoteTenantAcaPyService;
    private final EventInvitationRepository eventInvitationRepository;
    private final WalletMetadataRepository walletMetadataRepository;
    
    @Value("${acapy.wallet.key}")
    private String walletKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateMediatorInvitationResponse getMediatorInvitation() {
        try {
            return mediatorAcapyClient.createInvitation(CreateMediatorInvitationRequest.generate());
        } catch (Exception e) {
            log.error("미디에이터 초대장 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException("미디에이터 초대장 생성에 실패했습니다.", DidErrorCodes.INVITATION_CREATION_FAILED, e);
        }
    }

    @Override
    @Transactional
    public void createEventInvitation(CreateEventInvitationRequest request) {
        try {
            // 테넌트 토큰 조회
            WalletMetadata walletMetadata = walletMetadataRepository.findByTenantId(request.getTenantId())
                    .orElseThrow(() -> new BusinessException("테넌트 지갑이 존재하지 않습니다.", DidErrorCodes.WALLET_NOTFOUND));

            String authorization = "Bearer " + walletMetadata.getToken();

            // ACA-PY에 초대장 생성 요청
            AcaPyCreateInvitationResponse invitationResponse = remoteTenantAcaPyService.createInvitation(
                    authorization,
                    AcaPyCreateInvitationRequest.generate(
                            request.getEventId()
                    )
            );
            
            log.info("이벤트 ID: {}, 테넌트 ID: {}에 대한 초대장이 생성되었습니다. OOB ID: {}", 
                request.getEventId(), request.getTenantId(), invitationResponse.getOobId());
                
            // 이벤트 초대장 정보 저장
            eventInvitationRepository.save(invitationResponse.toEntity(request.getEventId(), request.getTenantId()));
        } catch (BusinessException e) {
            // 이미 정의된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 기타 예외는 INVITATION_CREATION_FAILED로 래핑
            log.error("이벤트 초대장 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException("초대장 생성에 실패했습니다.", DidErrorCodes.INVITATION_CREATION_FAILED, e);
        }
    }
}
