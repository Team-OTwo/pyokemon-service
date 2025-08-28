package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.webhook.*;

/**
 * User ACA-Py webhook 처리를 위한 서비스 인터페이스
 */
public interface UserWebhookService {

  /**
   * Connection webhook을 처리합니다.
   *
   * @param webhookDto Connection webhook 데이터
   */
  void handleConnectionWebhook(ConnectionWebhookRequest webhookDto);

  /**
   * Out of Band webhook을 처리합니다.
   *
   * @param webhookDto Out of Band webhook 데이터
   */
  void handleOutOfBandWebhook(OutOfBandWebhookRequest webhookDto);

  /**
   * Basic Message webhook을 처리합니다.
   *
   * @param webhookDto Basic Message webhook 데이터
   */
  void handleBasicMessageWebhook(BasicMessageWebhookRequest webhookDto);


  void handleIssueCredentialWebhook(IssueCredentialWebhookRequest webhookDto);

  void handleLdProofWebhook(LdProofWebhookRequest webhookDto);
}
