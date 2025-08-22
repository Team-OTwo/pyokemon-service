package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.ConnectionMetadata;
import com.pyokemon.did.domain.repository.ConnectionMetadataRepository;
import com.pyokemon.did.remote.common.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.service.ConnectionMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionMetadataServiceImpl implements ConnectionMetadataService {

    private final ConnectionMetadataRepository connectionMetadataRepository;

    /**
     * AcaPy 응답을 기반으로 ConnectionMetadata 객체 생성
     * 
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     * @param tenantId 테넌트 ID
     * @param invitation AcaPy 초대장 응답
     * @return 생성된 ConnectionMetadata 객체
     * @throws BusinessException 필수 입력값이 누락되거나 잘못된 경우
     */
    @Override
    public ConnectionMetadata buildConnectionMetadata(Long userId, String deviceId, Long tenantId, AcaPyCreateInvitationResponse invitation) {
        // 입력값 검증
        if (userId == null) {
            throw new BusinessException("사용자 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new BusinessException("디바이스 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (invitation == null) {
            throw new BusinessException("AcaPy 초대장 응답은 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (invitation.getOobId() == null || invitation.getOobId().trim().isEmpty()) {
            throw new BusinessException("AcaPy 초대장의 oobId는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (invitation.getInviMsgId() == null || invitation.getInviMsgId().trim().isEmpty()) {
            throw new BusinessException("AcaPy 초대장의 inviMsgId는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (tenantId == null) {
            throw new BusinessException("테넌트 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        try {
            return ConnectionMetadata.builder()
                .connId(invitation.getOobId())
                .inviMsgId(invitation.getInviMsgId())
                .userId(userId)
                .tenantId(tenantId)
                .deviceId(deviceId)
                .status(ConnectionMetadata.ConnectionStatus.PENDING)
                .build();
        } catch (Exception e) {
            log.error("ConnectionMetadata 객체 생성 중 오류 발생: userId={}, deviceId={}, error={}", 
                userId, deviceId, e.getMessage(), e);
            throw new BusinessException("ConnectionMetadata 객체 생성에 실패했습니다.", DidErrorCodes.INVITATION_CREATION_FAILED, e);
        }
    }

    /**
     * ConnectionMetadata 저장
     * 
     * @param connectionMetadata 저장할 연결 메타데이터
     * @return 저장된 ConnectionMetadata 객체
     * @throws BusinessException 저장할 데이터가 null이거나 데이터베이스 저장 중 오류가 발생한 경우
     */
    @Override
    public ConnectionMetadata saveConnectionMetadata(ConnectionMetadata connectionMetadata) {
        // 입력값 검증
        if (connectionMetadata == null) {
            throw new BusinessException("저장할 ConnectionMetadata는 null일 수 없습니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (connectionMetadata.getConnId() == null || connectionMetadata.getConnId().trim().isEmpty()) {
            throw new BusinessException("Connection ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        if (connectionMetadata.getUserId() == null) {
            throw new BusinessException("사용자 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        try {
            connectionMetadataRepository.save(connectionMetadata);
            log.info("Connection metadata 저장 완료: connId={}, userId={}", 
                connectionMetadata.getConnId(), connectionMetadata.getUserId());
            return connectionMetadata;
        } catch (Exception e) {
            log.error("ConnectionMetadata 저장 중 오류 발생: connId={}, userId={}, error={}", 
                connectionMetadata.getConnId(), connectionMetadata.getUserId(), e.getMessage(), e);
            throw new BusinessException("ConnectionMetadata 저장에 실패했습니다.", DidErrorCodes.DATABASE_ERROR, e);
        }
    }
} 