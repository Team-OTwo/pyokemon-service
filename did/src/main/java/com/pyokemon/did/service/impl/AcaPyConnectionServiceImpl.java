package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;

import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.ReceiveInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.ReceiveInvitationResponse;
import com.pyokemon.did.remote.acapy.service.tenant.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.acapy.service.user.RemoteUserAcaPyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;

import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcaPyConnectionServiceImpl implements AcaPyConnectionService {
    private final AcaPyConnectionRepository acaPyConnectionRepository;

    private final WalletService walletService;
    private final RemoteTenantAcaPyService remoteTenantAcaPyService;
    private final RemoteUserAcaPyService remoteUserAcaPyService;

    @Override
    @Transactional
    public void createAcaPyConnection(Long tenantId, Long userId) throws BusinessException {
        log.info("테넌트 ID {} 및 사용자 ID {}에 대한 AcaPy 연결 생성 시작", tenantId, userId);

        try {
            // 기존 연결 존재 여부 확인
            boolean exists = acaPyConnectionRepository.existsByTenantIdAndUserId(tenantId, userId);

            if (exists) {
                log.info("테넌트 ID {} 및 사용자 ID {}에 대한 연결이 이미 존재합니다. 처리를 종료합니다.", tenantId, userId);
                return;
            }

            // 1. 테넌트 지갑 조회
            log.info("테넌트 ID {}에 대한 지갑 조회", tenantId);
              Wallet tenantWallet = walletService.getWalletByAccountIdOrThrow(tenantId);

            // 2. 사용자 지갑 조회
            log.info("사용자 ID {}에 대한 지갑 조회", userId);
            Wallet userWallet = walletService.getWalletByAccountIdOrThrow(userId);

            // 3. 테넌트 AcaPy 에서 초대장 생성
            CreateInvitationResponse invitation = createInvitation(tenantWallet, tenantId, userId);

            // 4. 연결 정보 저장
            log.info("테넌트 ID {} 및 사용자 ID {}에 대한 연결 정보 저장", tenantId, userId);
            acaPyConnectionRepository.save(invitation.toEntity(tenantId, userId));

            // 5. 사용자 AcaPy 에서 초대장 수락
            receiveInvitation(userWallet, invitation, tenantId, userId);

            log.info("테넌트 ID {} 및 사용자 ID {}에 대한 AcaPy 연결 생성 완료", tenantId, userId);
        } catch (BusinessException e) {
            log.error("테넌트 ID {} 및 사용자 ID {}에 대한 AcaPy 연결 생성 중 비즈니스 예외 발생: {}", tenantId, userId,
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("테넌트 ID {} 및 사용자 ID {}에 대한 AcaPy 연결 생성 중 예외 발생: {}", tenantId, userId,
                    e.getMessage(), e);
            throw new BusinessException("AcaPy간 연결 생성에 실패했습니다.", CONNECTION_CREATION_FAILED);
        }
    }

    /**
     * 테넌트 AcaPy에서 초대장을 생성합니다.
     *
     * @param tenantWallet 테넌트 지갑 정보
     * @param tenantId     테넌트 ID
     * @param userId       사용자 ID
     * @return 생성된 초대장 응답
     */
    private CreateInvitationResponse createInvitation(Wallet tenantWallet, Long tenantId,
                                                     Long userId) {
        log.info("테넌트 ID {}에서 사용자 ID {}로의 초대장 생성 요청", tenantId, userId);
        CreateInvitationResponse invitation = remoteTenantAcaPyService.createInvitation(
                tenantWallet.getToken(), CreateInvitationRequest.forUserTenant(userId, tenantId));

        if (invitation == null || invitation.getInvitation() == null) {
            log.error("테넌트 ID {} 및 사용자 ID {}에 대한 초대장 생성 실패", tenantId, userId);
            throw new BusinessException("초대장이 생성에 실패했습니다.", DidErrorCodes.INVITATION_CREATION_FAILED);
        }
        log.debug("초대장 생성 성공: {}", invitation.getInvitation().getId());
        return invitation;
    }

    /**
     * 사용자 AcaPy에서 초대장을 수락합니다.
     *
     * @param userWallet 사용자 지갑 정보
     * @param invitation 초대장 정보
     * @param tenantId   테넌트 ID
     * @param userId     사용자 ID
     */
    private void receiveInvitation(Wallet userWallet, CreateInvitationResponse invitation,
                                   Long tenantId, Long userId) {
        log.info("사용자 ID {}가 테넌트 ID {}의 초대장 수락 요청", userId, tenantId);
        ReceiveInvitationResponse receivedInvitation =
                remoteUserAcaPyService.receiveInvitation(userWallet.getToken(),
                        ReceiveInvitationRequest.fromInvitation(invitation.getInvitation()));

        if (receivedInvitation == null || !"deleted".equals(receivedInvitation.getState())) {
            log.error("테넌트 ID {} 및 사용자 ID {}에 대한 초대장 수락 실패: {}", tenantId, userId,
                    receivedInvitation != null ? receivedInvitation.getState() : "null");
            throw new BusinessException("초대장 수락에 실패했습니다.", INVITATION_RECEIVE_FAILED);
        }
        log.debug("초대장 수락 성공");
    }

    @Override
    public AcaPyConnection getActiveAcaPyConnectionOrThrow(Long tenantId, Long userId) {
        log.debug("테넌트 ID: {} 및 사용자 ID: {}에 대한 활성화된 연결 조회", tenantId, userId);

        // 활성화된 연결 조회
        AcaPyConnection connection = acaPyConnectionRepository.findByTenantIdAndUserIdAndIsActive(tenantId, userId)
                .orElseThrow(() -> createConnectionNotFoundException(tenantId, userId));

        // 연결 ID 유효성 검사
        if (connection.getConnectionId() == null || connection.getConnectionId().isEmpty()) {
            log.warn("테넌트 ID: {} 및 사용자 ID: {}에 대한 연결이 존재하지만 connectionId가 null입니다", tenantId, userId);
            throw createConnectionNotFoundException(tenantId, userId);
        }

        log.debug("테넌트 ID: {} 및 사용자 ID: {}에 대한 활성화된 연결 조회 성공: connectionId={}",
                tenantId, userId, connection.getConnectionId());
        return connection;
    }

    /**
     * 연결을 찾을 수 없을 때 발생시킬 예외를 생성합니다.
     *
     * @param tenantId 테넌트 ID
     * @param userId   사용자 ID
     * @return 생성된 BusinessException
     */
    private BusinessException createConnectionNotFoundException(Long tenantId, Long userId) {
        String errorMessage = String.format("테넌트 ID: %d 사용자 ID: %d 에 대한 활성화된 연결을 찾을 수 없습니다.",
                tenantId, userId);
        return new BusinessException(errorMessage, CONNECTION_NOT_FOUND);
    }
}
