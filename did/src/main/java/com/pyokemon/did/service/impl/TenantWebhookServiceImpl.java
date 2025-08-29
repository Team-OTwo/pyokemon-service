package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.CONNECTION_CREATION_FAILED;

import java.io.IOException;

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

  private static final String CONNECTION_STATUS_ACTIVE = "active";

  @Override
  @Transactional
  @Retryable(retryFor = {RetryException.class, DataAccessException.class, IOException.class},
      backoff = @Backoff(delay = 2000, multiplier = 2) // 2초, 4초 간격으로 재시도
  )
  public void handleTenantConnectionWebhook(ConnectionWebhookRequest connectionWebhookRequest) {
    // 1. active 상태인 연결만 처리
    if (!CONNECTION_STATUS_ACTIVE.equals(connectionWebhookRequest.getState())) {
      return;
    }

    // 2. AcaPyConnection 조회
    AcaPyConnection connection =
        acaPyConnectionRepository.findByInviMsgId(connectionWebhookRequest.getInvitationMsgId()).orElseThrow(() -> {
          log.error("[Webhook] 연결 찾기 실패: invitationMsgId={}", connectionWebhookRequest.getInvitationMsgId());
          return new RetryException("AcaPy Connection NotFound");
        });

    // 3. 상태 변경 및 업데이트
    connection.activate(connectionWebhookRequest.getConnectionId());
    acaPyConnectionRepository.update(connection);
  }


  @Override
  public void handleTenantOOBWebhook(OutOfBandWebhookRequest handleTenantOOBRequest) {
    log.info("oob_id: {}, state: {}", handleTenantOOBRequest.getOobId(),
        handleTenantOOBRequest.getState());
  }
}
