package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.CONNECTION_CREATION_FAILED;

import java.io.IOException;

import com.pyokemon.did.domain.dto.request.webhook.PresentProofWebhookRequest;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.WalletService;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.dto.request.webhook.ConnectionWebhookRequest;
import com.pyokemon.did.domain.dto.request.webhook.OutOfBandWebhookRequest;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TenantWebhookServiceImpl implements TenantWebhookService {

  private final AcaPyConnectionRepository acaPyConnectionRepository;
  private final RemoteTenantAcaPyService remoteTenantAcaPyService;
  private final WalletService walletService;
  private static final String CONNECTION_STATUS_ACTIVE = "active";

  @Override
  @Transactional
  @Retryable(retryFor = {RetryException.class, DataAccessException.class, IOException.class},
      backoff = @Backoff(delay = 2000, multiplier = 2) // 2초, 4초 간격으로 재시도
  )
  public void handleTenantConnectionWebhook(ConnectionWebhookRequest request) {
    // 1. active 상태인 연결만 처리
    if (!CONNECTION_STATUS_ACTIVE.equals(request.getState())) {
      log.debug("[Webhook] 비활성 상태 무시: {}", request.getState());
      return;
    }

    // 2. AcaPyConnection 조회
    AcaPyConnection connection =
        acaPyConnectionRepository.findByInviMsgId(request.getInvitationMsgId()).orElseThrow(() -> {
          log.error("[Webhook] 연결 찾기 실패: invitationMsgId={}", request.getInvitationMsgId());
          return new RetryException("AcaPy Connection NotFound");
        });

    // 3. 상태 변경 및 업데이트
    connection.activate(request.getConnectionId());
    acaPyConnectionRepository.update(connection);
  }

  /**
   * 예외 발생 시 복구 처리 - 연결 비활성화 후 예외 발생
   */
  @Recover
  public void recoverTenantConnectionWebhook(Exception e, ConnectionWebhookRequest request)
      throws BusinessException {
    log.error("[Webhook] 재시도 실패 (Exception): msgId={}, error={}", request.getInvitationMsgId(),
        e.getMessage());

    try {
      AcaPyConnection connection =
          acaPyConnectionRepository.findByInviMsgId(request.getInvitationMsgId()).orElse(null);

      if (connection != null) {
        log.info("[Connection] 실패로 인한 비활성화: id={}", connection.getId());
        connection.deactivate();
        acaPyConnectionRepository.update(connection);
      } else {
        log.warn("[Connection] 비활성화 실패: 연결 찾을 수 없음");
      }
    } catch (Exception ex) {
      log.error("[Connection] 비활성화 중 오류 발생: {}", ex.getMessage());
    }

    throw new BusinessException("연결 생성 실패: " + e.getMessage(), CONNECTION_CREATION_FAILED);
  }

  @Override
  public void handleTenantOOBWebhook(OutOfBandWebhookRequest request) {
    log.info("OOB Webhook from Tenant ACA-py - state: {}, oob_id: {}, role: {}, connection_id: {}", 
        request.getState(), request.getOobId(), request.getRole(), request.getConnectionId());
  }

  @Override
  public void handleTenantPresentProofWebhook(PresentProofWebhookRequest request) {
    String state = request.getState();
    String challenge = request.getChallenge();
    String presExId = request.getPresExId();

    String authorization = walletService.getWalletToken()

    //state="presentation_received"일때, pres_ex_id로 issuedProof 뒤져서 challenge 찾기, challenge 비교하기
    //pres_ex_id로 issuedVc가서 status=issued인지 확인
    //POST /present-proof-2.0/records/{pres_ex_id}/verify-presentation 날리기
    //state가 done으로 나오면

    log.info("Present proof Webhook - state: {}, pres_ex_id: {}, challenge: {}", state, presExId, challenge);

  }
}
