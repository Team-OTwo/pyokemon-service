package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.WalletMetadataRequest.CreateWalletRequest;

/**
 * 지갑 관리 작업을 위한 서비스 인터페이스
 */
public interface WalletMetadataService {
    
    /**
     * 지정된 테넌트를 위한 지갑 프로비저닝 및 메타데이터 등록
     *
     * @param request 테넌트 정보가 포함된 지갑 프로비저닝 요청
     * @throws com.pyokemon.common.exception.BusinessException 지갑이 이미 존재하거나 생성에 실패한 경우
     */
    void createWallet(CreateWalletRequest request);
    
    /**
     * 테넌트 ID로 Wallet metadata 조회
     *
     * @param tenantId 테넌트 ID
     * @return WalletMetadata 객체
     * @throws com.pyokemon.common.exception.BusinessException Wallet metadata를 찾을 수 없는 경우
     */
    com.pyokemon.did.domain.WalletMetadata getWalletMetadata(Long tenantId);
}