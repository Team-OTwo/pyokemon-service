package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.webhook.ConnectionWebhookRequest;
import com.pyokemon.did.domain.dto.request.webhook.OutOfBandWebhookRequest;
import com.pyokemon.did.domain.dto.request.webhook.PresentProofWebhookRequest;

public interface TenantWebhookService {

  /**
   * Connection webhook을 처리합니다.
   *
   * @param connectionWebhookRequest Connection webhook 데이터
   */
  void handleTenantConnectionWebhook(ConnectionWebhookRequest connectionWebhookRequest);

  /**
   * Out of Band webhook을 처리합니다.
   *
   * @param outOfBandWebhookRequest Out of Band webhook 데이터
   */
  void handleTenantOOBWebhook(OutOfBandWebhookRequest outOfBandWebhookRequest);

  /**
   * Present Proof webhook을 처리합니다.
   *
   * @param presentProofWebhookRequest Out of Band webhook 데이터
   */
  void handleTenantPresentProofWebhook(PresentProofWebhookRequest presentProofWebhookRequest);
}
