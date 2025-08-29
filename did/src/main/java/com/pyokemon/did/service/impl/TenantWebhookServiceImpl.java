package com.pyokemon.did.service.impl;

import com.pyokemon.did.service.AcaPyConnectionService;
import org.springframework.retry.annotation.Recover;

import com.pyokemon.did.common.annotation.WebhookRetryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.did.domain.dto.request.webhook.ConnectionWebhookRequest;
import com.pyokemon.did.domain.dto.request.webhook.OutOfBandWebhookRequest;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TenantWebhookServiceImpl implements TenantWebhookService {
  private final AcaPyConnectionService acaPyConnectionService;

  private static final String CONNECTION_STATUS_ACTIVE = "active";

  @Override
  @Transactional
  @WebhookRetryable
  public void handleTenantConnectionWebhook(ConnectionWebhookRequest connectionWebhookRequest) {
    // 1. active 상태인 연결만 처리
    if (!CONNECTION_STATUS_ACTIVE.equals(connectionWebhookRequest.getState())) return;

    log.info("Connection Webhook from Tenant ACA-py - state: {}, invitation_msg_id: {}, connection_id: {}",
            connectionWebhookRequest.getState(),
            connectionWebhookRequest.getInvitationMsgId(),
            connectionWebhookRequest.getConnectionId());

    // 2. AcaPyConnection 조회 및 connectionId, status 업데이트
    acaPyConnectionService.updateConnectionId(
            connectionWebhookRequest.getInvitationMsgId(),
            connectionWebhookRequest.getConnectionId());
  }

  /**
   * handleTenantConnectionWebhook 재시도 실패 시 복구 메소드
   */
  @Recover
  public void recoverTenantConnectionWebhook(Exception e, ConnectionWebhookRequest connectionWebhookRequest) {
    log.error("Tenant Connection Webhook 처리 실패 - 최대 재시도 횟수 초과. state: {}, invitation_msg_id: {}, error: {}", 
        connectionWebhookRequest.getState(), 
        connectionWebhookRequest.getInvitationMsgId(), 
        e.getMessage(), e);
  }


  @Override
  public void handleTenantOOBWebhook(OutOfBandWebhookRequest handleTenantOOBRequest) {
    log.info("oob_id: {}, state: {}", handleTenantOOBRequest.getOobId(),
        handleTenantOOBRequest.getState());
  }
}
