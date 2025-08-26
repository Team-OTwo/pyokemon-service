package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;

import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyReceiveInvitationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.TenantWallet;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyReceiveInvitationResponse;
import com.pyokemon.did.remote.tenantAcaPy.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.TenantWalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcaPyConnectionServiceImpl implements AcaPyConnectionService {
  private final RemoteTenantAcaPyService remoteTenantAcaPyService;
  private final RemoteUserAcaPyService remoteUserAcaPyService;
  private final AcaPyConnectionRepository acaPyConnectionRepository;
  private final TenantWalletService tenantWalletService;
  // TODO: userWalletRepository.findByUserId -> userWalletService.getWalletByUserId 수정
  private final UserWalletRepository userWalletRepository;

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
      TenantWallet tenantWallet = tenantWalletService.getWalletByTenantId(tenantId)
          .orElseThrow(() -> new BusinessException("테넌트 지갑이 존재하지 않습니다.", WALLET_NOT_FOUND));

      // 2. 사용자 지갑 조회
      log.info("사용자 ID {}에 대한 지갑 조회", userId);
      UserWallet userWallet = userWalletRepository.findByUserId(userId)
          .orElseThrow(() -> new BusinessException("사용자 지갑이 존재하지 않습니다.", WALLET_NOT_FOUND));

      // 3. 테넌트 AcaPy 에서 초대장 생성
      AcaPyCreateInvitationResponse invitation = createInvitation(tenantWallet, tenantId, userId);

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
   * @param tenantId 테넌트 ID
   * @param userId 사용자 ID
   * @return 생성된 초대장 응답
   */
  private AcaPyCreateInvitationResponse createInvitation(TenantWallet tenantWallet, Long tenantId,
      Long userId) {
    log.info("테넌트 ID {}에서 사용자 ID {}로의 초대장 생성 요청", tenantId, userId);
    AcaPyCreateInvitationResponse invitation = remoteTenantAcaPyService.acaPyCreateInvitation(
        tenantWallet.getToken(), AcaPyCreateInvitationRequest.of(userId, tenantId));

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
   * @param tenantId 테넌트 ID
   * @param userId 사용자 ID
   */
  private void receiveInvitation(UserWallet userWallet, AcaPyCreateInvitationResponse invitation,
      Long tenantId, Long userId) {
    log.info("사용자 ID {}가 테넌트 ID {}의 초대장 수락 요청", userId, tenantId);
    AcaPyReceiveInvitationResponse receivedInvitation =
        remoteUserAcaPyService.acaPyReceiveInvitation(
                userWallet.getToken(),
                AcaPyReceiveInvitationRequest.of(invitation.getInvitation())
        );

    if (receivedInvitation == null || !"deleted".equals(receivedInvitation.getState())) {
      log.error("테넌트 ID {} 및 사용자 ID {}에 대한 초대장 수락 실패: {}", tenantId, userId,
          receivedInvitation != null ? receivedInvitation.getState() : "null");
      throw new BusinessException("초대장 수락에 실패했습니다.", INVITATION_RECEIVE_FAILED);
    }
    log.debug("초대장 수락 성공");
  }

}
