package com.pyokemon.did.service;

import com.pyokemon.did.domain.ConnectionMetadata;
import com.pyokemon.did.remote.common.InvitationResponse.AcaPyCreateInvitationResponse;

/**
 * 연결 메타데이터 관리 작업을 위한 서비스 인터페이스
 */
public interface ConnectionMetadataService {
    
    /**
     * AcaPy 응답을 기반으로 ConnectionMetadata 객체 생성
     *
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     * @param tenantId 테넌트 ID
     * @param invitation AcaPy 초대장 응답
     * @return 생성된 ConnectionMetadata 객체
     */
    ConnectionMetadata buildConnectionMetadata(Long userId, String deviceId, Long tenantId, AcaPyCreateInvitationResponse invitation);
    
    /**
     * ConnectionMetadata 저장
     *
     * @param connectionMetadata 저장할 연결 메타데이터
     * @return 저장된 ConnectionMetadata 객체
     */
    ConnectionMetadata saveConnectionMetadata(ConnectionMetadata connectionMetadata);
} 