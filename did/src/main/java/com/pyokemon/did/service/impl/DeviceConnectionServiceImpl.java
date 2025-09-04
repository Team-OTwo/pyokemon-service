package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.*;

import java.util.Optional;

import org.springframework.retry.RetryException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.dto.response.InvitationResponse;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.acapy.service.RemoteMediatorAcaPyService;
import com.pyokemon.did.remote.acapy.service.RemoteUserAcaPyService;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.WalletService;

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
  private final WalletService walletService;

  @Override
  @Transactional
  public InvitationResponse createInvitations(Long userId) {

    // 1. 사용자 지갑에서 userId, Token 조회
    String userToken = walletService.getWalletToken(userId);
    // 2. Gateway 헤더에서 deviceId 추출
    String deviceId = GatewayRequestHeaderUtils.getUserDeviceOrThrowException();
    // 3. tb_device_connection 확인 및 예외처리
    checkAndProcessDeviceConnection(userId, deviceId);
    // 4. ACA-Py 초대장 생성 (User + Mediator) - 필요한 경우에만

    try {

        // User ACA-Py 초대장 생성
        String userAcaPyInvitationUrl = createUserAcaPyInvitationUrl(deviceId, userId, userToken);
        // Mediator ACA-Py 초대장 생성
        String mediatorAcaPyInvitationUrl = createMediatorAcaPyInvitationUrl(deviceId, userId);

        return new InvitationResponse(mediatorAcaPyInvitationUrl, userAcaPyInvitationUrl);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("초대장 생성 중 오류가 발생했습니다", DidErrorCodes.INVITATION_CREATION_FAILED);
    }
  }


  /**
   * 테넌트 AcaPy 에서 초대장을 생성합니다.
   *
   * @param deviceId 테넌트 ID
   * @param userId 사용자 ID
   * @return 생성된 초대장 응답
   */
  private String createUserAcaPyInvitationUrl(String deviceId, Long userId, String userToken) {
    log.info("User ACA-Py 초대장 생성 요청: userId={}", userId);
    CreateInvitationRequest userRequest = CreateInvitationRequest.forUserDevice(userId, deviceId);
    CreateInvitationResponse userAcapyResponse =
        remoteUserAcaPyService.createInvitation(userToken, userRequest);

    if (userAcapyResponse == null || userAcapyResponse.getInvitationUrl() == null) {
      log.error("User ACA-Py 초대장 생성 실패: 응답이 null 이거나 URL이 없음, userId={}", userId);
      throw new BusinessException("User ACA-Py 초대장 생성에 실패했습니다", INVITATION_CREATION_FAILED);
    }
    log.info("User ACA-Py 초대장 생성 완료: userId={}", userId);
    return userAcapyResponse.getInvitationUrl();
  }

  private String createMediatorAcaPyInvitationUrl(String deviceId, Long userId) {
    log.info("Mediator ACA-Py 초대장 생성 요청: userId={}", userId);
    CreateInvitationRequest mediatorRequest =
        CreateInvitationRequest.forUserDevice(userId, deviceId);
    CreateInvitationResponse mediatorAcapyResponse =
        remoteMediatorAcaPyService.createInvitation(mediatorRequest);

    if (mediatorAcapyResponse == null || mediatorAcapyResponse.getInvitationUrl() == null) {
      log.error("Mediator ACA-Py 초대장 생성 실패: 응답이 null 이거나 URL이 없음, userId={}", userId);
      throw new RuntimeException("Mediator ACA-Py 초대장 생성에 실패했습니다");
    }
    log.info("Mediator ACA-Py 초대장 생성 완료: userId={}", userId);
    return mediatorAcapyResponse.getInvitationUrl();
  }

  @Override
  public Long getUserIdByPublicDidOrThrow(String did) {
    DeviceConnection deviceConnection = deviceConnectionRepository.findByPublicDid(did).orElseThrow(
        () -> new BusinessException("public DID: {" + did + "} 에 대한 userId를 찾을 수 없습니다.",
            DID_NOT_FOUND));
    return deviceConnection.getUserId();
  }

  /**
   * DeviceConnection 비즈니스 로직 예외처리
   */
  private void checkAndProcessDeviceConnection(Long userId, String deviceId) {

    String userAlias = String.format("credo:user:%d#device:%s", userId, deviceId);

    Optional<DeviceConnection> existingConnection = deviceConnectionRepository.findByAlias(userAlias);

    if (existingConnection.isEmpty()) {
      createNewDeviceConnection(userId, deviceId, userAlias, "신규 사용자");
      return;
    }

    DeviceConnection existing = existingConnection.get();

    // userId 있음, deviceId 다름 → 기존 연결 REVOKED 처리 후 새 연결 생성
    if (!existing.getDeviceId().equals(deviceId)) {
      existing.setStatus(REVOKED);
      deviceConnectionRepository.update(existing);
      log.info("기존 연결 폐기: id={}, oldDeviceId={}, newDeviceId={}", existing.getId(),
          existing.getDeviceId(), deviceId);

      createNewDeviceConnection(userId, deviceId, userAlias, "deviceId 변경");
    }

    // 동일 디바이스의 기존 연결이 있는 경우 →  새 연결 생성 (재발급)
    if (existing.getDeviceId().equals(deviceId)){
      updateDeviceConnection(existing);
      log.info("앱 초기화로 연결 재생성: id={}, oldDeviceId={}, newDeviceId={}", existing.getId(),
              existing.getDeviceId(), deviceId);
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

  /**
   * 기존 DeviceConnection을 업데이트합니다.
   */
  private void updateDeviceConnection(DeviceConnection deviceConnection) {

    deviceConnection.update();
    deviceConnectionRepository.update(deviceConnection);
  }

  /**
   * deviceId로 deviceConnection을 조회
   */
  @Override
  public DeviceConnection getDeviceConnectionByDeviceIdOrThrow(String deviceId) {
    return deviceConnectionRepository.findByDeviceId(deviceId)
        .orElseThrow(() -> new BusinessException("디바이스 연결이 존재하지 않습니다.", CONNECTION_NOT_FOUND));
  }

  /**
   * deviceConnection을 connectionId 또는 alias로 찾고 public DID를 저장합니다
   */
  @Override
  @Transactional
  public void updatePublicDid(String connectionId, String content) {

    DeviceConnection deviceConnection = getDeviceConnectionByConnectionIdOrThrow(connectionId);
    // content가 did:key로 시작하는 경우에만 publicDid에 저장
    if (content != null && content.startsWith("did:key:")) {
      deviceConnection.setPublicDid(content);
      deviceConnectionRepository.update(deviceConnection);
    }
  }

  /**
   * deviceConnection을 connectionId 또는 alias로 찾고 public DID를 저장합니다
   */
  @Override
  @Transactional
  public void UpdateConnectionId(String connectionId, String alias) throws RetryException {
    try {
      // alias로 찾기
      DeviceConnection deviceConnection = getDeviceConnectionByAliasOrThrow(alias);
      deviceConnection.activate(connectionId);
      deviceConnectionRepository.update(deviceConnection);
    } catch (BusinessException e) {
      if (e.getErrorCode().equals(CONNECTION_INVALID_STATE))
        return;
    } catch (Exception e) {
      throw new RetryException("<UNK> <UNK> <UNK> <UNK> <UNK> <UNK> <UNK> <UNK>.", e);
    }

    // alias로 찾기
    DeviceConnection deviceConnection = deviceConnectionRepository.findByAlias(alias)
        // .orElseThrow(() -> new BusinessException(
        // "DeviceConnection not found for connection_id: " + connectionId + " or alias: " + alias,
        // "DEVICE_CONNECTION_NOT_FOUND"));
        .orElseThrow(() -> new RetryException("retry - device connection not found"));

    // connectionId 저장, active로 상태 바꾸기
    deviceConnection.activate(connectionId);
    deviceConnectionRepository.update(deviceConnection);
  }

  /**
   * deviceConnection을 connectionId 로 찾습니다
   */
  private DeviceConnection getDeviceConnectionByConnectionIdOrThrow(String connectionId) {
    return deviceConnectionRepository.findByConnectionId(connectionId)
        .orElseThrow(() -> new BusinessException("{}에 대한 DeviceConnection 못찾음: " + connectionId,
            "DEVICE_CONNECTION_NOT_FOUND"));
  }

  /**
   * deviceConnection을 alias로 찾습니다.
   */
  private DeviceConnection getDeviceConnectionByAliasOrThrow(String alias) {
    DeviceConnection deviceConnection = deviceConnectionRepository.findByAlias(alias)
        .orElseThrow(() -> new BusinessException("not found", CONNECTION_NOT_FOUND));

    if (DeviceConnection.DeviceConnectionStatus.REVOKED.equals(deviceConnection.getStatus())) {
      throw new BusinessException("invalid connection", CONNECTION_INVALID_STATE);
    }
    return deviceConnection;
  }
}
