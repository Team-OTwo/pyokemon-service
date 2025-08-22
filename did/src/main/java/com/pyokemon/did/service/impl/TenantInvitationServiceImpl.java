package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.ConnectionMetadata;
import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.dto.request.TenantInvitationRequest.CreateTenantInvitationRequest;
import com.pyokemon.did.domain.dto.response.TenantInvitationResponse.CreateTenantInvitationResponse;
import com.pyokemon.did.domain.dto.response.TenantInvitationResponse.TenantInvitation;

import com.pyokemon.did.domain.repository.ConnectionMetadataRepository;
import com.pyokemon.did.service.ConnectionMetadataService;
import com.pyokemon.did.service.WalletMetadataService;
import com.pyokemon.did.remote.booking.RemoteBookingService;
import com.pyokemon.did.remote.common.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.tenantacapy.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.tenantacapy.dto.request.InvitationRequest.AcaPyCreateTenantInvitationRequest;
import com.pyokemon.did.service.TenantInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 초대장 관리 작업을 위한 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInvitationServiceImpl implements TenantInvitationService {

    private final RemoteTenantAcaPyService remoteTenantAcaPyService;
    private final RemoteBookingService remoteBookingService;
    private final ConnectionMetadataRepository connectionMetadataRepository;
    private final ConnectionMetadataService connectionMetadataService;
    private final WalletMetadataService walletMetadataService;

    @Value("${acapy.wallet.key}")
    private String walletKey;


    /**
     * 테넌트 초대장 생성
     * 기존 connection이 없는 tenant에 대해서만 새로운 invitation URL 생성
     * 
     * @param request 초대장 생성 요청 (사용자 ID, 디바이스 ID, 테넌트 ID 목록)
     * @return 새로 생성된 초대장 목록 (기존 connection이 있는 tenant는 제외)
     */
    @Override
    @Transactional
    public CreateTenantInvitationResponse createTenantInvitation(CreateTenantInvitationRequest request) {
        // 입력값 검증
        validateRequest(request);
        
        List<Long> tenantIds = request.getTenantIds();
        Long userId = request.getUserId();
        String deviceId = request.getDeviceId();

        if (tenantIds == null || tenantIds.isEmpty()) {
            log.info("테넌트 ID 목록이 비어있음: userId={}", userId);
            return createEmptyResponse();
        }

        List<TenantInvitation> invitations = new ArrayList<>();
        
        for (Long tenantId : tenantIds) {
            try {
                log.info("테넌트 초대장 생성 처리 시작: userId={}, tenantId={}", userId, tenantId);
                // 기존 connection이 없는 경우에만 새로운 invitation 생성
                if (!hasExistingConnection(userId, tenantId)) {
                    log.info("새로운 초대장 생성 시작: userId={}, tenantId={}", userId, tenantId);
                    TenantInvitation invitation = createNewInvitation(userId, tenantId, deviceId);
                    invitations.add(invitation);
                    log.info("새로운 초대장 생성 완료: userId={}, tenantId={}, invitationUrl={}", 
                        userId, tenantId, invitation.getInvitationUrl());
                } else {
                    log.info("기존 connection이 존재하므로 건너뜀: userId={}, tenantId={}", userId, tenantId);
                }
            } catch (Exception e) {
                log.error("테넌트 초대장 생성 중 오류 발생: userId={}, tenantId={}, error={}", 
                    userId, tenantId, e.getMessage(), e);
                // 개별 tenant 실패 시에도 다른 tenant는 계속 처리
                continue;
            }
        }

        return new CreateTenantInvitationResponse(invitations);
    }

    /**
     * 입력값 검증
     * 
     * @param request 초대장 생성 요청
     */
    private void validateRequest(CreateTenantInvitationRequest request) {
        if (request == null) {
            throw new BusinessException("요청 객체가 null입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (request.getUserId() == null) {
            throw new BusinessException("사용자 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (request.getDeviceId() == null || request.getDeviceId().trim().isEmpty()) {
            throw new BusinessException("디바이스 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
    }

    /**
     * 기존 connection 존재 여부 확인
     * 
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @return 기존 connection이 있으면 true, 없으면 false
     */
    private boolean hasExistingConnection(Long userId, Long tenantId) {
        try {
            log.info("기존 connection 조회 시작: userId={}, tenantId={}", userId, tenantId);
            List<ConnectionMetadata> existingConnections = 
                connectionMetadataRepository.findByUserIdAndTenantId(userId, tenantId);
            log.info("기존 connection 조회 결과: userId={}, tenantId={}, count={}", 
                userId, tenantId, existingConnections.size());
            boolean hasConnection = !existingConnections.isEmpty();
            log.info("기존 connection 존재 여부: userId={}, tenantId={}, hasConnection={}", 
                userId, tenantId, hasConnection);
            return hasConnection;
        } catch (Exception e) {
            log.error("기존 connection 조회 중 오류 발생: userId={}, tenantId={}, error={}", 
                userId, tenantId, e.getMessage(), e);
            throw new BusinessException("기존 연결 정보 조회에 실패했습니다.", DidErrorCodes.DATABASE_ERROR, e);
        }
    }

    /**
     * 새로운 테넌트 초대장 생성
     * AcaPy를 통해 초대장을 생성하고 ConnectionMetadata를 저장
     * 
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param deviceId 디바이스 ID
     * @return 새로 생성된 테넌트 초대장 정보
     */
    private TenantInvitation createNewInvitation(Long userId, Long tenantId, String deviceId) {
        try {
            // 1. WalletMetadata 조회
            WalletMetadata walletMetadata = walletMetadataService.getWalletMetadata(tenantId);
            
            // 2. AcaPy 초대장 생성 (token을 인증 헤더로 사용)
            AcaPyCreateInvitationResponse invitation = createAcaPyInvitation(walletMetadata.getToken(), tenantId);
            
            // 3. ConnectionMetadata 생성 및 저장
            ConnectionMetadata connectionMetadata = connectionMetadataService.buildConnectionMetadata(userId, deviceId, tenantId, invitation);
            connectionMetadataService.saveConnectionMetadata(connectionMetadata);
            
            log.info("새로운 connection metadata 생성: userId={}, tenantId={}, connId={}",
                userId, tenantId, connectionMetadata.getConnId());
            
            return TenantInvitation.builder()
                .tenantId(tenantId)
                .invitationUrl(invitation.getInvitationUrl())
                .build();
                
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("새로운 초대장 생성 중 예상치 못한 오류 발생: userId={}, tenantId={}, error={}", 
                userId, tenantId, e.getMessage(), e);
            throw new BusinessException("초대장 생성에 실패했습니다.", DidErrorCodes.INVITATION_CREATION_FAILED, e);
        }
    }




    /**
     * AcaPy 초대장 생성
     * 외부 AcaPy 서비스를 호출하여 초대장 생성
     * 
     * @param tenantToken 테넌트 토큰 (인증용)
     * @param tenantId 테넌트 ID
     * @return AcaPy 초대장 응답
     */
    private AcaPyCreateInvitationResponse createAcaPyInvitation(String tenantToken, Long tenantId) {
        try {
            // Bearer 토큰 형태로 설정
            String bearerToken = "Bearer " + tenantToken;
            log.info("AcaPy 초대장 생성 요청: tenantId={}, bearerToken={}", tenantId, bearerToken);
            
            AcaPyCreateInvitationResponse response = remoteTenantAcaPyService.createInvitation(bearerToken, AcaPyCreateTenantInvitationRequest.of(tenantId));
            
            log.info("AcaPy 초대장 생성 응답: tenantId={}, response={}", tenantId, response);
            
            return response;
        } catch (Exception e) {
            log.error("AcaPy 초대장 생성 중 오류 발생: tenantId={}, tenantToken={}, error={}", 
                tenantId, tenantToken, e.getMessage(), e);
            throw new BusinessException("AcaPy 초대장 생성에 실패했습니다.", DidErrorCodes.ACAPY_SERVICE_ERROR, e);
        }
    }


    /**
     * 빈 초대장 응답 생성
     * 테넌트 ID 목록이 비어있을 때 사용
     * 
     * @return 빈 초대장 목록이 포함된 응답
     */
    private CreateTenantInvitationResponse createEmptyResponse() {
        return new CreateTenantInvitationResponse(new ArrayList<TenantInvitation>());
    }

}


