package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.EventInvitation;

import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.dto.request.EventInvitationRequest.ProvisionEventInvitationRequest;
import com.pyokemon.did.domain.repository.EventInvitationRepository;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.remote.mediator.MediatorAcapyClient;
import com.pyokemon.did.remote.mediator.dto.request.CreateMediatorInvitationRequest;
import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.tenant.TenantAcapyClient;
import com.pyokemon.did.remote.tenant.dto.request.OobInvitationRequest.CreateOobInvitationRequest;
import com.pyokemon.did.remote.tenant.dto.response.OobInvitationResponse.CreateOobInvitationResponse;
import com.pyokemon.did.service.EventInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 초대장 관리 작업을 위한 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventInvitationServiceImpl implements EventInvitationService {

    private final MediatorAcapyClient mediatorAcapyClient;
    private final TenantAcapyClient tenantAcapyClient;
    private final EventInvitationRepository eventInvitationRepository;
    private final WalletMetadataRepository walletMetadataRepository;
    
    @Value("${acapy.wallet.key}")
    private String walletKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateMediatorInvitationResponse getMediatorInvitation() {
        return mediatorAcapyClient.createInvitation(CreateMediatorInvitationRequest.generate());
    }

    @Override
    public void provisionEventInvitation(ProvisionEventInvitationRequest request) {

        WalletMetadata walletMetadata = walletMetadataRepository.findByTenantId(request.getTenantId())
                .orElseThrow(() -> new BusinessException("테넌트 지갑이 존재하지 않습니다.", DidErrorCodes.WALLET_NOTFOUND));

        String authorization = "Bearer " + walletMetadata.getToken();
        CreateOobInvitationResponse invitationResponse = tenantAcapyClient.createInvitation(
                authorization,
                CreateOobInvitationRequest.generate(
                        request.getEventId()
                )
        );
        
        log.info("이벤트 ID: {}, 테넌트 ID: {}에 대한 초대장이 생성되었습니다. OOB ID: {}", 
            request.getEventId(), request.getTenantId(), invitationResponse.getOobId());
            
        // 이벤트 초대장 정보 저장
        EventInvitation eventInvitation = EventInvitation.builder()
            .eventId(request.getEventId())
            .tenantId(request.getTenantId())
            .invitationUrl(invitationResponse.getInvitationUrl())
            .oobId(invitationResponse.getOobId())
            .build();
            
        eventInvitationRepository.save(eventInvitation);
    }
}
