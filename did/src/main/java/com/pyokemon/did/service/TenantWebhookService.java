package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.TenantWebhookRequest.HandleTenantConnectionsRequest;
import com.pyokemon.did.domain.dto.request.TenantWebhookRequest.HandleTenantOOBRequest;

public interface TenantWebhookService {

  /**
   * Connection webhook을 처리합니다.
   *
   * @param handleTenantConnectionsRequest Connection webhook 데이터
   */
  void handleTenantConnectionWebhook(HandleTenantConnectionsRequest handleTenantConnectionsRequest);

  /**
   * Out of Band webhook을 처리합니다.
   *
   * @param handleTenantOOBRequest Out of Band webhook 데이터
   */
  void handleTenantOOBWebhook(HandleTenantOOBRequest handleTenantOOBRequest);
}
