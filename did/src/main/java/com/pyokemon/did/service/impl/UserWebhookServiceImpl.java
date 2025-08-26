package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.ACTIVE;
import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.INVITATION_SENT;
import static com.pyokemon.did.domain.IssuedVc.VcStatus.*;

import java.io.IOException;
import java.util.Optional;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.api.backend.dto.*;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.service.UserWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWebhookServiceImpl implements UserWebhookService {

  private final DeviceConnectionRepository deviceConnectionRepository;
  private final IssuedVcRepository issuedVcRepository;


  @Override
  @Retryable(value = {BusinessException.class, IOException.class}, // 비즈니스 예외 및 네트워크 관련 예외 발생 시 재시도
      maxAttempts = 3, // 최대 3번 시도
      backoff = @Backoff(delay = 2000, multiplier = 2) // 2초, 4초 간격으로 재시도
  )
  public void handleConnectionWebhook(ConnectionWebhookDto webhookDto) {
    log.info("=== User ACA-Py Connections Webhook ===");
    log.info("Payload: {}", webhookDto);

    try {
      String state = webhookDto.getState();
      String connectionId = webhookDto.getConnectionId();
      String alias = webhookDto.getAlias();

      log.info("Connection webhook - state: {}, connection_id: {}, alias: '{}'", state,
          connectionId, alias);

      // alias로 DeviceConnection 찾기
      DeviceConnection deviceConnection = findDeviceConnection(connectionId, alias);

      log.info("✅DeviceConnection found by alias: '{}', id: {}", alias, deviceConnection.getId());

      // state에 따라 처리
      switch (state) {
        case "invitation":
          log.info("Updated status invitation for alias: {}", alias);
          break;

        case "active":
          deviceConnection.setStatus(ACTIVE);
          deviceConnection.setConnectionId(connectionId);
          deviceConnectionRepository.update(deviceConnection);
          log.info("Updated status to ACTIVE for connection_id: {}", connectionId);
          break;

        default:
          log.info("Unhandled state: {}", state);
          break;
      }

    } catch (Exception e) {
      log.error("❌ Error processing connection webhook: {}", e.getMessage(), e);
      throw e; // 재시도를 위해 예외를 다시 던짐
    }
    log.info("================================");
  }

  @Override
  @Retryable(value = {BusinessException.class, IOException.class}, // 비즈니스 예외 및 네트워크 관련 예외 발생 시 재시도
      maxAttempts = 3, // 최대 3번 시도
      backoff = @Backoff(delay = 2000, multiplier = 2) // 2초, 4초 간격으로 재시도
  )
  public void handleOutOfBandWebhook(OutOfBandWebhookDto webhookDto) {
    log.info("=== User ACA-Py Out of Band Webhook ===");
    log.info("Payload: {}", webhookDto);

    try {
      String state = webhookDto.getState();
      String connectionId = webhookDto.getConnectionId();

      log.info("OOB webhook - state: {}, connection_id: {}", state, connectionId);

      // connectionId로 DeviceConnection 찾기
      DeviceConnection deviceConnection =
          deviceConnectionRepository.findByConnectionId(connectionId)
              .orElseThrow(() -> new BusinessException(
                  "DeviceConnection not found for connection_id: " + connectionId,
                  "DEVICE_CONNECTION_NOT_FOUND"));

      log.info("✅DeviceConnection found by connectionId: {}", connectionId);

      // OOB webhook 처리 (state 구분 없이 동일한 로그)
      log.info("OOB webhook processed for connectionId: {}, state: {}", connectionId, state);

    } catch (Exception e) {
      log.error("❌ Error processing OOB webhook: {}", e.getMessage(), e);
      throw e; // 재시도를 위해 예외를 다시 던짐
    }

    log.info("================================");
  }

  @Override
  public void handleBasicMessageWebhook(BasicMessageWebhookDto webhookDto) {
    log.info("=== User ACA-Py Basic Message Webhook ===");
    log.info("Payload: {}", webhookDto);

    try {
      String state = webhookDto.getState();
      String connectionId = webhookDto.getConnectionId();
      String content = webhookDto.getContent();

      log.info("Basic Message webhook - state: {}, connection_id: {}, content: {}", state,
          connectionId, content);

      // connectionId로 DeviceConnection 찾기
      DeviceConnection deviceConnection =
          deviceConnectionRepository.findByConnectionId(connectionId)
              .orElseThrow(() -> new BusinessException(
                  "DeviceConnection not found for connection_id: " + connectionId,
                  "DEVICE_CONNECTION_NOT_FOUND"));

      log.info("✅ DeviceConnection found by connectionId: {}, current publicDid: {}", connectionId,
          deviceConnection.getPublicDid());

      // content를 publicDid에 저장 (임시로 전체 content 사용)
      deviceConnection.setPublicDid(content);
      int updateResult = deviceConnectionRepository.update(deviceConnection);

      log.info("Updated publicDid to '{}', update result: {}", content, updateResult);

      log.info("Basic Message webhook processed for connectionId: {}, state: {}", connectionId,
          state);

    } catch (Exception e) {
      log.error("❌ Error processing Basic Message webhook: {}", e.getMessage(), e);
      throw e;
    }

    log.info("================================");
  }

  /**
   * DeviceConnection을 찾습니다. connectionId가 있으면 우선으로 하고, 없거나 실패 시 alias에서 추출하여 찾습니다.
   */
  private DeviceConnection findDeviceConnection(String connectionId, String alias) {
    // 1. connectionId가 있고, 해당 connectionId로 찾기
    if (connectionId != null && !connectionId.trim().isEmpty()) {
      Optional<DeviceConnection> byConnectionId =
          deviceConnectionRepository.findByConnectionId(connectionId);
      if (byConnectionId.isPresent()) {
        log.info("✅ DeviceConnection found by connectionId: {}", connectionId);
        return byConnectionId.get();
      }
      log.warn("DeviceConnection not found by connectionId: {}, trying alias...", connectionId);
    } else {
      log.info("connectionId is null or empty, trying alias...");
    }

    // 2. connectionId로 찾지 못한 경우, alias에서 추출하여 찾기
    try {
      // credo:user:123#device:abc123 형식 파싱
      String userId = null;
      String deviceId = null;

      log.info("Searching DeviceConnection by alias: '{}'", alias);

      return deviceConnectionRepository.findByAlias(alias).orElseThrow(() -> new BusinessException(
          "❌ DeviceConnection not found for connection_id: " + connectionId + " or alias: " + alias,
          "DEVICE_CONNECTION_NOT_FOUND"));

    } catch (Exception e) {
      log.error("Failed to parse ACA-Py alias: {}", alias, e);
      throw new BusinessException("❌ DeviceConnection not found for connection_id: " + connectionId
          + " and could not extract alias from: " + alias, "DEVICE_CONNECTION_NOT_FOUND");
    }
  }

  @Override
  public void handleIssueCredentialWebhook(IssueCredentialWebhookDto webhookDto) {
    log.info("일반 Webhook 처리 시작: {}", webhookDto);

    try {
      handleGeneralWebhook(webhookDto);

    } catch (Exception e) {
      log.error("일반 Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new RuntimeException("일반 Webhook 처리 실패", e);
    }
  }

  @Override
  public void handleLdProofWebhook(LdProofWebhookDto webhookDto) {
    log.info("LD Proof Webhook 처리 시작: {}", webhookDto);

    try {
      processLdProofWebhook(webhookDto);

    } catch (Exception e) {
      log.error("LD Proof Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new RuntimeException("LD Proof Webhook 처리 실패", e);
    }
  }

  private void processLdProofWebhook(LdProofWebhookDto webhookDto) {
    String credExId = webhookDto.getCredExId();
    String credIdStored = webhookDto.getCredIdStored();

    log.info("LD Proof Webhook 처리 - credExId: {}, credIdStored: {}", credExId, credIdStored);

    if (credExId == null || credExId.isEmpty()) {
      log.warn("credential_exchange_id가 없습니다");
      return;
    }

    // LD Proof webhook은 credential_id를 업데이트하고 상태를 CREDENTIAL_ISSUED로 변경
    updateVcStatus(credExId, credIdStored, CREDENTIAL_ISSUED);
  }

  private void handleGeneralWebhook(IssueCredentialWebhookDto webhookDto) {
    String state = webhookDto.getState();
    String credExId = webhookDto.getCredExId();

    log.info("일반 Webhook 처리 - 상태: {}, credExId: {}", state, credExId);

    if (credExId == null || credExId.isEmpty()) {
      log.warn("credential_exchange_id가 없습니다");
      return;
    }

    // 상태에 따른 처리
    switch (state) {
      case "credential-received":
        // credential-received 상태에서 credential_exchange_id 업데이트
        updateVcCredentialExchangeId(webhookDto);
        updateVcStatus(credExId, null, CREDENTIAL_RECEIVED);
        break;
      case "done":
        updateVcStatus(credExId, null, CREDENTIAL_ISSUED);
        break;
      default:
        log.info("처리하지 않는 상태: {}", state);
    }
  }

  private void updateVcCredentialExchangeId(IssueCredentialWebhookDto webhookDto) {
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
      var issuedVcOpt = issuedVcRepository.findByBookingIdAndStatus(bookingIdLong, CREDENTIAL_SENT);
      if (issuedVcOpt.isEmpty()) {
        log.warn("업데이트할 VC를 찾을 수 없습니다 - bookingId: {}, status: CREDENTIAL_SENT", bookingIdLong);
        return;
      }

      IssuedVc issuedVc = issuedVcOpt.get();

      // credential_exchange_id 업데이트
      issuedVc.setCredentialExchangeId(credExId);

      issuedVcRepository.update(issuedVc);
      log.info("VC credential_exchange_id 업데이트 완료 - bookingId: {}, credExId: {}",
          issuedVc.getBookingId(), credExId);

    } catch (Exception e) {
      log.error("VC credential_exchange_id 업데이트 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  private void updateVcStatus(String credExId, String credentialId, IssuedVc.VcStatus status) {
    log.info("VC 상태 업데이트 - credExId: {}, credentialId: {}, status: {}", credExId, credentialId,
        status);

    // credential_exchange_id로 IssuedVc 찾기
    var issuedVcOpt = issuedVcRepository.findByCredentialExchangeId(credExId);
    if (issuedVcOpt.isEmpty()) {
      log.warn("credential_exchange_id에 해당하는 VC를 찾을 수 없습니다: {}", credExId);
      return;
    }

    IssuedVc issuedVc = issuedVcOpt.get();

    // 상태 업데이트
    issuedVc.setStatus(status);

    // credential_id가 있으면 업데이트
    if (credentialId != null && !credentialId.isEmpty()) {
      issuedVc.setCredentialId(credentialId);
    }

    issuedVcRepository.update(issuedVc);
    log.info("VC 상태 업데이트 완료 - bookingId: {}, status: {}, credentialId: {}", issuedVc.getBookingId(),
        status, credentialId);
  }
}
