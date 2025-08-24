package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.ACTIVE;
import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.INVITATION_SENT;
import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.REVOKED;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.dto.response.InvitationResponse.CreateInvitationResponse;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.mediatorAcaPy.RemoteMediatorAcaPyService;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.UserWalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeviceConnectionServiceImpl implements DeviceConnectionService {

  private final RemoteUserAcaPyService remoteUserAcaPyService;
  private final RemoteMediatorAcaPyService remoteMediatorAcaPyService;
  private final DeviceConnectionRepository deviceConnectionRepository;
  private final UserWalletService userWalletService;

  @Override
  @Transactional
  public CreateInvitationResponse createInvitations(Long userId) {
    try {
      // 1. 사용자 지갑에서 userId, Token 조회
      String userToken = userWalletService.getUserWalletToken(userId);
      log.info("사용자 지갑 토큰 조회 완료: userId={}, token={}", userId, userToken);

      // 2. Gateway 헤더에서 deviceId 추출
      String deviceId = GatewayRequestHeaderUtils.getClientDevice();
      log.info("Gateway 헤더에서 deviceId 추출: deviceId={}", deviceId);

      // 3. tb_device_connection 확인 및 예외처리
      boolean shouldCreateInvitation = processDeviceConnectionBusinessLogic(userId, deviceId);

      // 4. 사용자 지갑 토큰으로 Authorization 헤더 생성
      String authorization = "Bearer " + userToken;
      log.debug("Authorization 헤더 설정: {}", authorization);

      // 5. ACA-Py 초대장 생성 (User + Mediator) - 필요한 경우에만
      AcaPyCreateInvitationResponse userAcapyResponse;
      AcaPyCreateInvitationResponse mediatorAcapyResponse;

      if (shouldCreateInvitation) {
        // User ACA-Py 초대장 생성
        log.info("User ACA-Py 초대장 생성 요청: userId={}", userId);
        AcaPyCreateInvitationRequest userRequest = AcaPyCreateInvitationRequest
            .of(userId, deviceId);
        userAcapyResponse =
            remoteUserAcaPyService.acaPyCreateInvitation(authorization, userRequest);

        if (userAcapyResponse == null || userAcapyResponse.getInvitationUrl() == null) {
          log.error("User ACA-Py 초대장 생성 실패: 응답이 null이거나 URL이 없음, userId={}", userId);
          throw new RuntimeException("User ACA-Py 초대장 생성에 실패했습니다");
        }
        log.info("User ACA-Py 초대장 생성 완료: userId={}", userId);

        // Mediator ACA-Py 초대장 생성
        log.info("Mediator ACA-Py 초대장 생성 요청: userId={}", userId);
        AcaPyCreateInvitationRequest mediatorRequest = AcaPyCreateInvitationRequest
            .of(userId, deviceId);
        mediatorAcapyResponse = remoteMediatorAcaPyService.acaPyCreateInvitation(mediatorRequest);

        if (mediatorAcapyResponse == null || mediatorAcapyResponse.getInvitationUrl() == null) {
          log.error("Mediator ACA-Py 초대장 생성 실패: 응답이 null이거나 URL이 없음, userId={}", userId);
          throw new RuntimeException("Mediator ACA-Py 초대장 생성에 실패했습니다");
        }
        log.info("Mediator ACA-Py 초대장 생성 완료: userId={}", userId);

      } else {
        log.info("새로운 초대장 생성이 필요하지 않습니다: userId={}, deviceId={}", userId, deviceId);
        throw new BusinessException("초대장 생성이 필요하지 않은 상태입니다", DidErrorCodes.INVALID_REQUEST);
      }

      // 7. 응답 생성 및 반환
      log.info("초대장 생성 완료: userId={}, deviceId={}", userId, deviceId);
      return new CreateInvitationResponse(userAcapyResponse.getInvitationUrl(),
          mediatorAcapyResponse.getInvitationUrl());

    } catch (BusinessException e) {
      log.error("초대장 생성 중 비즈니스 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("초대장 생성 중 예상치 못한 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
      throw new BusinessException("초대장 생성 중 오류가 발생했습니다", DidErrorCodes.INVITATION_CREATION_FAILED);
    }
  }

  /**
   * DeviceConnection 비즈니스 로직 예외처리
   */
  private boolean processDeviceConnectionBusinessLogic(Long userId, String deviceId) {
    String userAlias = String.format("credo:user:%d#device:%s", userId, deviceId);

    // 1. userId 없음 → 새 연결 생성
    Optional<DeviceConnection> existingConnection =
        deviceConnectionRepository.findByAlias(userAlias);
    if (existingConnection.isEmpty()) {
      createNewDeviceConnection(userId, deviceId, userAlias, "신규 사용자");
      return true;
    }

    DeviceConnection existing = existingConnection.get();

    // 2.userId 있음, deviceId 다름 → 기존 연결 REVOKED 처리 후 새 연결 생성
    if (!existing.getDeviceId().equals(deviceId)) {
      existing.setStatus(REVOKED);
      deviceConnectionRepository.update(existing);
      log.info("기존 연결 폐기: id={}, oldDeviceId={}, newDeviceId={}", existing.getId(),
          existing.getDeviceId(), deviceId);

      createNewDeviceConnection(userId, deviceId, userAlias, "deviceId 변경");
      return true;
    }

    // 3. userId 있음, deviceId 같음 → 상태별 처리
    return handleExistingConnectionByStatus(existing, userId, deviceId, userAlias);
  }

  /**
   * 기존 연결의 상태에 따라 처리합니다.
   */
  private boolean handleExistingConnectionByStatus(DeviceConnection existing, Long userId,
      String deviceId, String userAlias) {
    switch (existing.getStatus()) {
      case ACTIVE, DID_RECEIVED:
        log.error("이미 연결된 상태: userId={}, deviceId={}, alias={}", userId, deviceId, userAlias);
        throw new BusinessException("이미 연결되어 있는 connection이 active 상태입니다",
            DidErrorCodes.CONNECTION_ALREADY_ACTIVE);

      case INVITATION_SENT:
        log.error("이미 pending 중인 invitation: userId={}, deviceId={}, alias={}", userId, deviceId,
            userAlias);
        throw new BusinessException("이미 pending 중인 invitation이 존재합니다",
            DidErrorCodes.INVITATION_ALREADY_SENT);

      case REVOKED:
      default:
        log.info("기존 연결 재생성: status={}, alias={}, id={}", existing.getStatus(), userAlias,
            existing.getId());
        createNewDeviceConnection(userId, deviceId, userAlias, "기존 상태: " + existing.getStatus());
        return true;
    }
  }

  /**
   * 새로운 DeviceConnection을 생성합니다.
   */
  private void createNewDeviceConnection(Long userId, String deviceId, String userAlias,
      String reason) {
    DeviceConnection newConnection = DeviceConnection.builder().connectionId(null)
        .deviceId(deviceId).userId(userId).alias(userAlias).status(INVITATION_SENT).build();

    deviceConnectionRepository.save(newConnection);
    log.info("{} 생성: alias={}, id={}", reason, userAlias, newConnection.getId());
  }
}
