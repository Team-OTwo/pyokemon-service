package com.pyokemon.did.service.impl;

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
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.INVITATION_SENT;

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
            throw new BusinessException("사용자 지갑을 찾을 수 없습니다. userId: " + userId, DidErrorCodes.WALLET_NOT_FOUND);
        }
        
        UserWallet userWallet = userWalletOpt.get();
        String userToken = userWallet.getToken();
        
        if (userToken == null || userToken.isEmpty()) {
            throw new BusinessException("사용자 지갑 토큰이 없습니다. userId: " + userId, DidErrorCodes.WALLET_NOT_FOUND);
        }
        
        log.info("사용자 지갑 토큰 조회 완료: userId={}, token={}", userId, userToken);

        // 2. Gateway 헤더에서 deviceId 추출
        String deviceId = GatewayRequestHeaderUtils.getClientDevice();
        log.info("Gateway 헤더에서 deviceId 추출: deviceId={}", deviceId);

        // 2. Authorization 헤더 생성
        String authorization = "Bearer " + userToken;
        log.debug("Authorization 헤더 설정: {}", authorization);

        // 3. User ACA-Py 초대장 생성 (Authorization 헤더와 함께)
        AcaPyCreateInvitationRequest userRequest = AcaPyCreateInvitationRequest.of(
                "connection between CREDO <-> USER ACA-Py",
                "Invitation to Credo from User ACA-Py"
        );
        AcaPyCreateInvitationResponse userAcapyResponse = remoteUserAcaPyService.acaPyCreateInvitation(authorization, userRequest);

        // 4. Mediator ACA-Py 초대장 생성 (MediatorFeignClientConfiguration이 자동으로 X-API-Key 헤더 추가)
        AcaPyCreateInvitationRequest mediatorRequest = AcaPyCreateInvitationRequest.of(
                "connection between CREDO <-> MEDIATOR ACA-Py",
                "Invitation to Credo from Mediator ACA-Py"
        );

        AcaPyCreateInvitationResponse mediatorAcapyResponse = remoteMediatorAcaPyService.acaPyCreateInvitation(mediatorRequest);

        // 5. DeviceConnection 저장 (connection_id는 null로 설정, webhook에서 나중에 업데이트)
        DeviceConnection deviceConnection = DeviceConnection.builder()
                .connectionId(null)  // webhook에서 실제 connection_id로 업데이트 예정
                .inviMsgId(userAcapyResponse.getInviMsgId())
                .deviceId(deviceId)  // Gateway 헤더에서 받은 device_id
                .userId(userId)      // userId도 함께 저장
                .status(INVITATION_SENT)
                .build();

        deviceConnectionRepository.save(deviceConnection);

        // 6. 응답 생성
        return new CreateInvitationResponse(
                userAcapyResponse.getInvitationUrl(),
                mediatorAcapyResponse.getInvitationUrl()
        );

    }


}
