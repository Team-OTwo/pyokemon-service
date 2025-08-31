package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.DeviceConnection.isDeviceConnectionAliasValid;

import com.pyokemon.did.domain.IssuedCredentialWebhookResult;
import com.pyokemon.did.domain.repository.IssuedCredentialWebhookResultRepository;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.common.annotation.WebhookRetryable;
import com.pyokemon.did.domain.dto.request.webhook.*;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.UserWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWebhookServiceImpl implements UserWebhookService {

  private final IssuedVcService issuedVcService;
  private final IssuedCredentialWebhookResultRepository issuedCredentialWebhookResultRepository;
  private final DeviceConnectionService deviceConnectionService;

  private static final String ISSUE_CREDENTIAL_STATUS_DONE = "done";
  private static final String ISSUE_CREDENTIAL_ROLE_HOLDER = "holder";

  /**
   * Connection Webhook 왔을때 ConnectionId 저장하는 메소드
   */
  @Override
  @WebhookRetryable
  @Transactional
  public void handleConnectionWebhook(ConnectionWebhookRequest webhookDto) {
    if (webhookDto == null) {
      throw new BusinessException("Connection webhook 요청이 null입니다",
          DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    String state = webhookDto.getState();
    String connectionId = webhookDto.getConnectionId();
    String alias = webhookDto.getAlias();

    if (connectionId == null || connectionId.trim().isEmpty()) {
      throw new BusinessException("Connection ID가 null이거나 비어있습니다",
          DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    if (!isDeviceConnectionAliasValid(alias))
      return; // alias 형식에 맞는지 보고
    if (!"active".equals(state))
      return; // state=active 아닌지 보고

    log.info("Connection webhook - state: '{}', alias: '{}', connection_id: '{}'", state, alias,
        connectionId);

    deviceConnectionService.findAndUpdateConnectionId(connectionId, alias);
  }

  /**
   * handleConnectionWebhook 재시도 실패 시 복구 메소드
   */
  @Recover
  public void recoverConnectionWebhook(Exception e, ConnectionWebhookRequest webhookDto) {
    log.error(
        "Connection Webhook 처리 실패 - 최대 재시도 횟수 초과. state: '{}', alias: '{}', connection_id: '{}', error: {}",
        webhookDto.getState(), webhookDto.getAlias(), webhookDto.getConnectionId(), e.getMessage(),
        e);
  }

  public void handleOutOfBandWebhook(OutOfBandWebhookRequest webhookDto) {
    if (webhookDto == null) {
      log.error("OutOfBand webhook request is null");
      throw new BusinessException("OutOfBand webhook 요청이 null입니다",
          DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    log.info("OOB Webhook from User ACA-py - state: {}, oob_id: {}, role: {}, connection_id: {}",
        webhookDto.getState(), webhookDto.getOobId(), webhookDto.getRole(),
        webhookDto.getConnectionId());
  }

  @Override
  @Transactional
  public void handleBasicMessageWebhook(BasicMessageWebhookRequest webhookDto) {
    if (webhookDto == null) {
      log.error("Basic Message webhook request is null");
      throw new BusinessException("Basic Message webhook 요청이 null입니다",
          DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    String state = webhookDto.getState();
    String connectionId = webhookDto.getConnectionId();
    String content = webhookDto.getContent();
    String messageId = webhookDto.getMessageId();

    log.info("Basic Message webhook - state: {}, content: {}, connection_id: {}, message_id: {}",
        state, content, connectionId, messageId);

    deviceConnectionService.updatePublicDid(connectionId, content);
  }

  @Override
  @Transactional
  public void handleIssueCredentialWebhook(
      IssueCredentialWebhookRequest issueCredentialWebhookRequest) {

    // holder webhook 만 처리
    if (!ISSUE_CREDENTIAL_ROLE_HOLDER.equals(issueCredentialWebhookRequest.getRole()))
      return;

    // 완료된 issue credential webhook 만 처리
    if (!ISSUE_CREDENTIAL_STATUS_DONE.equals(issueCredentialWebhookRequest.getState()))
      return;

    String credExId = issueCredentialWebhookRequest.getCredExId();
    if (credExId == null || credExId.trim().isEmpty()) {
      throw new BusinessException("Credential exchange ID가 null이거나 비어있습니다",
          DidErrorCodes.WEBHOOK_INVALID_PAYLOAD);
    }

    log.info("Issue credential Webhook from User ACA-Py - cred_ex_id: {}, role: {}, state: {}",
        credExId, issueCredentialWebhookRequest.getRole(),
        issueCredentialWebhookRequest.getState());

    // 1. issueCredentialWebhookRequest 에서 bookingId 추출
    Long bookingId = issueCredentialWebhookRequest.extractBookingId();
    // 2. IssuedVc 조회 및 credExId 업데이트
    Optional<IssuedCredentialWebhookResult> webhookResult = issuedCredentialWebhookResultRepository.findByCredExId(credExId);

    if (webhookResult.isPresent()) return;

    issuedCredentialWebhookResultRepository.save(IssuedCredentialWebhookResult.of(credExId,bookingId));

  }


  @Override
  @Transactional
  @WebhookRetryable
  public void handleLdProofWebhook(LdProofWebhookRequest ldProofWebhookRequest) {

    IssuedCredentialWebhookResult webhookResult = issuedCredentialWebhookResultRepository.findByCredExId(ldProofWebhookRequest.getCredExId())
            .orElseThrow(() -> new RetryException("없당께"));

    Long bookingId = webhookResult.getBookingId();

    String credIdStored = ldProofWebhookRequest.getCredIdStored();
    issuedVcService.updateCredIdStored(bookingId, credIdStored);

    log.info(
        "LD Proof Webhook from User ACA-Py - cred_ex_id: {}, cred_id_stored: {}, cred_ex_ld_proof_id: {}",
        ldProofWebhookRequest.getCredExId(), credIdStored,
        ldProofWebhookRequest.getCredExLdProofId());
  }

  /**
   * handleLdProofWebhook 재시도 실패 시 복구 메소드
   */
  @Recover
  public void recoverLdProofWebhook(Exception e,
                                    LdProofWebhookRequest ldProofWebhookRequest) {
    log.error(
            "Issue Credential Webhook 처리 실패 - 최대 재시도 횟수 초과. cred_ex_id: {}, cred_ex_ld_proof_id: {}, cred_id_stored: {}, error: {}",
            ldProofWebhookRequest.getCredExId(), ldProofWebhookRequest.getCredExLdProofId(),
            ldProofWebhookRequest.getCredIdStored(), e.getMessage(), e);
  }
}
