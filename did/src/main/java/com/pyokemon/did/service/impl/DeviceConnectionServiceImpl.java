package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.INVITATION_SENT;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.dto.response.InvitationResponse.CreateInvitationResponse;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.mediatorAcaPy.RemoteMediatorAcaPyService;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.DeviceConnectionService;

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
  private final UserWalletRepository userWalletRepository;

  @Override
  @Transactional
  public CreateInvitationResponse createInvitations(Long userId) {

    // 1. userId로 UserWallet에서 토큰 조회
    Optional<UserWallet> userWalletOpt = userWalletRepository.findByUserId(userId);
    if (userWalletOpt.isEmpty()) {
      throw new BusinessException("사용자 지갑을 찾을 수 없습니다. userId: " + userId,
          DidErrorCodes.WALLET_NOT_FOUND);
    }

    UserWallet userWallet = userWalletOpt.get();
    String userToken = userWallet.getToken();

    if (userToken == null || userToken.isEmpty()) {
      throw new BusinessException("사용자 지갑 토큰이 없습니다. userId: " + userId,
          DidErrorCodes.WALLET_NOT_FOUND);
    }

    log.info("사용자 지갑 토큰 조회 완료: userId={}, token={}", userId, userToken);

    // 2. Gateway 헤더에서 deviceId 추출
    String deviceId = GatewayRequestHeaderUtils.getClientDevice();
    log.info("Gateway 헤더에서 deviceId 추출: deviceId={}", deviceId);

    // 3. Authorization 헤더 생성
    String authorization = "Bearer " + userToken;
    log.debug("Authorization 헤더 설정: {}", authorization);

    // 4. DeviceConnection 먼저 저장 (alias 포함)
    String userAlias = String.format("%d#%s", userId, deviceId);

    // 동일한 alias가 이미 존재하는지 확인
    Optional<DeviceConnection> existingConnection =
        deviceConnectionRepository.findByAlias(userAlias);

    DeviceConnection deviceConnection;
    if (existingConnection.isPresent()) {
      // 이미 존재하는 경우 기존 레코드 사용
      deviceConnection = existingConnection.get();
      log.info("기존 DeviceConnection 사용: alias={}, id={}", userAlias, deviceConnection.getId());
    } else {
      // 새로운 레코드 생성
      deviceConnection = DeviceConnection.builder().connectionId(null)
          .deviceId(deviceId)
          .userId(userId)
          .alias(userAlias) // Tracking ID로 alias 저장
          .status(INVITATION_SENT).build();

      deviceConnectionRepository.save(deviceConnection);
      log.info("새로운 DeviceConnection 생성: alias={}, id={}", userAlias, deviceConnection.getId());
    }

    // 5. User ACA-Py 초대장 생성 (동일한 alias 사용)
    AcaPyCreateInvitationRequest userRequest =
        AcaPyCreateInvitationRequest.of("Invitation to Credo from User ACA-Py", userId, deviceId);
    AcaPyCreateInvitationResponse userAcapyResponse =
        remoteUserAcaPyService.acaPyCreateInvitation(authorization, userRequest);
    String userInvitationUrl = userAcapyResponse.getInvitationUrl();

    // 7. Mediator ACA-Py 초대장 생성 (Tracking ID 포함)
    AcaPyCreateInvitationRequest mediatorRequest = AcaPyCreateInvitationRequest
        .of("Invitation to Credo from Mediator ACA-Py", userId, deviceId);

    AcaPyCreateInvitationResponse mediatorAcapyResponse =
        remoteMediatorAcaPyService.acaPyCreateInvitation(mediatorRequest);

    // 8. 응답 생성
    return new CreateInvitationResponse(userInvitationUrl,
        mediatorAcapyResponse.getInvitationUrl());

  }

}
