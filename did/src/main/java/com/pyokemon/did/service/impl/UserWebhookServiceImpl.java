package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.ACTIVE;
import static com.pyokemon.did.domain.IssuedVc.VcStatus.*;

import java.io.IOException;
import java.util.Optional;

import com.pyokemon.did.service.IssuedVcService;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.dto.request.webhook.*;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.service.UserWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWebhookServiceImpl implements UserWebhookService {

  private final DeviceConnectionRepository deviceConnectionRepository;
  private final IssuedVcRepository issuedVcRepository;
  private final IssuedVcService issuedVcService;

  private static final String ISSUE_CREDENTIAL_STATUS_DONE = "done";
  private static final String ISSUE_CREDENTIAL_ROLE_HOLDER = "holder";

  @Override
  @Retryable(value = {BusinessException.class, IOException.class}, maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2))
  public void handleConnectionWebhook(ConnectionWebhookRequest webhookDto) {
    String state = webhookDto.getState();
    String connectionId = webhookDto.getConnectionId();
    String alias = webhookDto.getAlias();

    if (!isDeviceConnectionAlias(alias)) {
      return;
    }

    DeviceConnection deviceConnection = findDeviceConnection(connectionId, alias);
    log.info("Connection webhook - state: '{}', alias: '{}', connection_id: '{}'", state, alias,
        connectionId);

    if ("active".equals(state)) {
      deviceConnection.setStatus(ACTIVE);
      deviceConnection.setConnectionId(connectionId);
      deviceConnectionRepository.update(deviceConnection);
    }
  }

  public void handleOutOfBandWebhook(OutOfBandWebhookRequest webhookDto) {
    log.info("Oob webhook - state: '{}', role: '{}', connection_id: '{}', inviMsgId: '{}'",
        webhookDto.getState(), webhookDto.getRole(), webhookDto.getConnectionId(),
        webhookDto.getInviMsgId());
  }

  @Override
  public void handleBasicMessageWebhook(BasicMessageWebhookRequest webhookDto) {
    try {
      String state = webhookDto.getState();
      String connectionId = webhookDto.getConnectionId();
      String content = webhookDto.getContent();

      log.info("Basic Message webhook - state: '{}', connection_id: '{}', content: '{}'", state,
          connectionId, content);

      // connectionId로 DeviceConnection 찾기
      DeviceConnection deviceConnection =
          deviceConnectionRepository.findByConnectionId(connectionId).orElseThrow(
              () -> new BusinessException("{}에 대한 DeviceConnection 못찾음: " + connectionId,
                  "DEVICE_CONNECTION_NOT_FOUND"));

      // content가 did:key로 시작하는 경우에만 publicDid에 저장
      if (content != null && content.startsWith("did:key:")) {
        deviceConnection.setPublicDid(content);
        deviceConnectionRepository.update(deviceConnection);
        log.info("'{}'에 대한 publicDid = '{}' 추가 완료", connectionId, content);
      } else {
        log.debug("content가 did:key로 시작하지 않아 publicDid 업데이트 건너뜀 - content: {}", content);
      }
    } catch (Exception e) {
      log.error("Basic Message webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  @Retryable(retryFor = {RetryException.class},
          maxAttempts = 4,
          backoff = @Backoff(delay = 2000, multiplier = 2) // 2초, 4초, 6초  간격으로 재시도
  )
  public void handleIssueCredentialWebhook(IssueCredentialWebhookRequest issueCredentialWebhookRequest) {
    // holder webhook 만 처리
    if (!ISSUE_CREDENTIAL_ROLE_HOLDER.equals(issueCredentialWebhookRequest.getRole())) return;

    // 완료된 issue credential webhook 만 처리
    if (!ISSUE_CREDENTIAL_STATUS_DONE.equals(issueCredentialWebhookRequest.getState())) return;

    // 1. issueCredentialWebhookRequest 에서 bookingId 추출
    Long bookingId = issueCredentialWebhookRequest.extractBookingId();

    // 2. IssuedVc 조회 및 credExId 업데이트
    issuedVcService.updateCredExId(bookingId, issueCredentialWebhookRequest.getCredExId());
  }

  @Override
  public void handleLdProofWebhook(LdProofWebhookRequest ldProofWebhookRequest) {
    log.info(
            "LD Proof Webhook from User ACA-Py - cred_ex_id: {}, cred_id_stored: {}, cred_ex_ld_proof_id: {}"
            , ldProofWebhookRequest.getCredExId(), ldProofWebhookRequest.getCredIdStored(), ldProofWebhookRequest.getCredExLdProofId()
    );
  }

  /**
   * connectionId가 있으면 우선으로 하고, 없으면 alias에서 추출하여 찾습니다.
   */
  private DeviceConnection findDeviceConnection(String connectionId, String alias) {

    // 1. connectionId가 유효한 경우, connectionId로 먼저 조회
    if (connectionId != null && !connectionId.trim().isEmpty()) {
      Optional<DeviceConnection> deviceOpt =
          deviceConnectionRepository.findByConnectionId(connectionId);
      if (deviceOpt.isPresent()) {
        return deviceOpt.get();
      }
    }

    // 2. connectionId로 찾지 못했거나 connectionId가 없는 경우, alias로 조회
    return deviceConnectionRepository.findByAlias(alias)
        .orElseThrow(() -> new BusinessException(
            "DeviceConnection not found for connection_id: " + connectionId + " or alias: " + alias,
            "DEVICE_CONNECTION_NOT_FOUND"));
  }


  private void updateVcCredentialExchangeId(IssueCredentialWebhookRequest webhookDto) {
    String credExId = webhookDto.getCredExId();
    log.info("VC credential_exchange_id 업데이트 - credExId: {}", credExId);

    try {
      // webhook에서 booking_id 추출
      String bookingIdStr = webhookDto.getByFormat().getCredOffer().getLdProof().getCredential()
          .getCredentialSubject().getBookingId();
      if (bookingIdStr == null || bookingIdStr.isEmpty()) {
        log.warn("webhook에서 booking_id를 추출할 수 없습니다");
        return;
      }

      // "urn:booking:1" 형태에서 "1" 추출
      String bookingId = bookingIdStr.replace("urn:booking:", "");
      Long bookingIdLong = Long.parseLong(bookingId);

      // booking_id로 CREDENTIAL_SENT 상태인 VC 찾기
      var issuedVcOpt = issuedVcRepository.findByBookingIdAndStatus(bookingIdLong, ISSUED);
      if (issuedVcOpt.isEmpty()) {
        log.warn("업데이트할 VC를 찾을 수 없습니다 - bookingId: {}, status: CREDENTIAL_SENT", bookingIdLong);
        return;
      }

      IssuedVc issuedVc = issuedVcOpt.get();

      // credential_exchange_id 업데이트
      // issuedVc.setCredentialExchangeId(credExId);

      issuedVcRepository.update(issuedVc);
      log.info("VC credential_exchange_id 업데이트 완료 - bookingId: {}, credExId: {}",
          issuedVc.getBookingId(), credExId);

    } catch (Exception e) {
      log.error("VC credential_exchange_id 업데이트 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  private void updateVcStatus(String credExId, String credentialId, IssuedVc.VcStatus status) {
    // credential_exchange_id로 IssuedVc 찾기
//    var issuedVcOpt = issuedVcRepository.findByCredentialExchangeId(credExId);
//    if (issuedVcOpt.isEmpty()) {
//      log.warn("credential_exchange_id에 해당하는 VC를 찾을 수 없습니다: {}", credExId);
//      return;
//    }
//    IssuedVc issuedVc = issuedVcOpt.get();
//    issuedVc.setStatus(status);

    // credential_id가 있으면 업데이트
    // if (credentialId != null && !credentialId.isEmpty()) {
    // issuedVc.setCredentialId(credentialId);
    // }
//    issuedVcRepository.update(issuedVc);
  }

  /**
   * DeviceConnection alias 패턴인지 확인합니다. DeviceConnection은 "credo:user:{userId}#device:{deviceId}"
   * 패턴을 사용합니다.
   */
  private boolean isDeviceConnectionAlias(String alias) {
    if (alias == null || alias.trim().isEmpty()) {
      return false;
    }
    // DeviceConnection alias 패턴: "credo:user:{userId}#device:{deviceId}"
    return alias.startsWith("credo:user:") && alias.contains("#device:");
  }
}
