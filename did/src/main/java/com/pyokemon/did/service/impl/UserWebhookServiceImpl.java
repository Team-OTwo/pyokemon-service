package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.ACTIVE;
import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.INVITATION_SENT;

import java.io.IOException;
import java.util.Optional;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.api.backend.dto.BasicMessageWebhookDto;
import com.pyokemon.did.api.backend.dto.ConnectionWebhookDto;
import com.pyokemon.did.api.backend.dto.OutOfBandWebhookDto;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.service.UserWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWebhookServiceImpl implements UserWebhookService {

  private final DeviceConnectionRepository deviceConnectionRepository;

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

      log.info("✅ DeviceConnection found by connectionId: {}", connectionId);

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
      
      return deviceConnectionRepository.findByAlias(alias).orElseThrow(
          () -> new BusinessException("❌ DeviceConnection not found for connection_id: "
              + connectionId + " or alias: " + alias, "DEVICE_CONNECTION_NOT_FOUND"));

    } catch (Exception e) {
      log.error("Failed to parse ACA-Py alias: {}", alias, e);
      throw new BusinessException("❌ DeviceConnection not found for connection_id: " + connectionId
          + " and could not extract alias from: " + alias, "DEVICE_CONNECTION_NOT_FOUND");
    }
  }
}
