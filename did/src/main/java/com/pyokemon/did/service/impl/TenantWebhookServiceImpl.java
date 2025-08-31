package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static com.pyokemon.did.domain.Verification.VpStatus.*;

import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.request.webhook.ConnectionWebhookRequest;
import com.pyokemon.did.domain.dto.request.webhook.OutOfBandWebhookRequest;
import com.pyokemon.did.domain.dto.request.webhook.PresentProofWebhookRequest;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;
import com.pyokemon.did.remote.acapy.common.dto.response.VerifyPresentationResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.*;
import com.pyokemon.did.common.annotation.WebhookRetryable;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TenantWebhookServiceImpl implements TenantWebhookService {

  private final IssuedProofService issuedProofService;
  private final IssuedVcService issuedVcService;
  private final WalletService walletService;
  private final RemoteTenantAcaPyService remoteTenantAcaPyService;
  private final AcaPyConnectionRepository acaPyConnectionRepository;
  private final AcaPyConnectionService acaPyConnectionService;


  private static final String CONNECTION_STATUS_ACTIVE = "active";
  private final VerificationService verificationService;


  @Override
  @Transactional
  @WebhookRetryable
  public void handleTenantConnectionWebhook(ConnectionWebhookRequest connectionWebhookRequest) {
    // 1. active 상태인 연결만 처리
    if (!CONNECTION_STATUS_ACTIVE.equals(connectionWebhookRequest.getState()))
      return;

    log.info(
        "Connection Webhook from Tenant ACA-py - state: {}, invitation_msg_id: {}, connection_id: {}",
        connectionWebhookRequest.getState(), connectionWebhookRequest.getInvitationMsgId(),
        connectionWebhookRequest.getConnectionId());

    // 2. AcaPyConnection 조회 및 connectionId, status 업데이트
    acaPyConnectionService.updateConnectionId(connectionWebhookRequest.getInvitationMsgId(),
        connectionWebhookRequest.getConnectionId());
  }

  @Override
  public void handleTenantOOBWebhook(OutOfBandWebhookRequest request) {
    log.info("OOB Webhook from Tenant ACA-py - state: {}, oob_id: {}, role: {}, connection_id: {}",
        request.getState(), request.getOobId(), request.getRole(), request.getConnectionId());
  }

  /**
   * handleTenantConnectionWebhook 재시도 실패 시 복구 메소드
   */
  @Recover
  public void recoverTenantConnectionWebhook(Exception e,
      ConnectionWebhookRequest connectionWebhookRequest) {
    log.error(
        "Tenant Connection Webhook 처리 실패 - 최대 재시도 횟수 초과. state: {}, invitation_msg_id: {}, error: {}",
        connectionWebhookRequest.getState(), connectionWebhookRequest.getInvitationMsgId(),
        e.getMessage(), e);
  }


  @Override
  public void handleTenantPresentProofWebhook(PresentProofWebhookRequest request) {

    VpStatus finalStatus = FAIL;
    String state = request.getState();
    String presExId = request.getPresExId();
    String challenge = request.getChallenge();

    log.info("Present proof Webhook received - state: {}, pres_ex_id: {}, challenge: {}", state,
        presExId, challenge);

    // state 확인
    if (!request.isPresentationReceived())
      return;

    try {
      // 1. 챌린지 일치 검증
      verifyChallenge(request);

      // 2. IssuedVC 상태 검증 및 조회
      IssuedVc issuedVc = validateAndGetIssuedVc(presExId);

      // 3. VP 검증 처리
      finalStatus = triggerRemoteVerification(presExId, issuedVc) ? SUCCESS : FAIL;

    } catch (BusinessException e) {
      if (e.getErrorCode() == VC_CONSUMED_OR_REVOKED) {
        finalStatus = INVALID_VC;
      } else {
        finalStatus = FAIL; // 그 외 모든 BusinessException은 FAIL
      }

    } finally {
      verificationService.saveVerification(presExId, finalStatus);
    }
  }

  /**
   * 요청으로 들어온 Challenge와 저장된 Challenge가 일치하는지 검증합니다. 불일치 시 예외를 발생시킵니다.
   *
   * @param request Webhook 요청
   * @throws BusinessException 챌린지가 일치하지 않는 경우
   */
  private void verifyChallenge(PresentProofWebhookRequest request) {
    String presExId = request.getPresExId();
    String receivedChallenge = request.getChallenge();
    String expectedChallenge = issuedProofService.getChallenge(presExId);

    if (!expectedChallenge.equals(receivedChallenge)) {
      throw new BusinessException("챌린지가 일치하지 않습니다", VP_CHALLENGE_MISMATCH);
    }
  }

  /**
   * IssuedVc의 상태를 검증하고 유효한 경우 객체를 반환합니다.
   * 
   * @param presExId 증명 제시 ID
   * @return 유효한 IssuedVc 객체
   * @throws BusinessException IssuedVc가 없거나 상태가 'ISSUED'가 아닌 경우
   */
  private IssuedVc validateAndGetIssuedVc(String presExId) {
    IssuedVc issuedVc = issuedVcService.getIssuedVcByPresExIdOrThrow(presExId);
    if (!issuedVc.isIssued()) {
      // issuedProof 삭제 후 예외 발생
      issuedProofService.revokeIssuedProof(presExId);
      throw new BusinessException("VC 상태가 'ISSUED'가 아닙니다", VC_CONSUMED_OR_REVOKED);
    }
    return issuedVc;
  }

  /**
   * VP 검증이 완료된 후, 사용된 VP를 삭제하고 원격으로 검증을 요청합니다.
   *
   * @param presExId 증명 제시 ID
   * @return 검증 성공 여부
   * @throws BusinessException 검증 실패 시
   */
  private boolean triggerRemoteVerification(String presExId, IssuedVc issuedVc) {

    // 1. 사용된 VP 삭제
    issuedProofService.revokeIssuedProof(presExId);

    // 2. 원격 검증 요청 - 이미 조회된 issuedVc 사용
    Long tenantId = issuedVc.getTenantId();
    String authorization = walletService.getWalletToken(tenantId);

    try {
      VerifyPresentationResponse response =
          remoteTenantAcaPyService.verifyPresentation(authorization, presExId);

      if (!response.verify()) {
        throw new BusinessException("ACA-Py VP 검증에 실패했습니다", VP_VERIFICATION_FAILED);
      }
      issuedVc.verified(); // issuedVC CONSUMED 상태로 변경
      return true;

    } catch (RestClientException e) {
      throw new BusinessException("원격 검증 API 호출에 실패했습니다", ACAPY_SERVICE_ERROR, e);
    }
  }
}
