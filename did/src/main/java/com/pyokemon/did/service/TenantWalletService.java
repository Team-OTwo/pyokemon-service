package com.pyokemon.did.service;

import com.pyokemon.did.domain.TenantWallet;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest.CreateWalletRequest;

import java.util.Optional;

public interface TenantWalletService {
    /**
     * 테넌트 지갑을 등록합니다.
     *
     * @param createWalletRequest 등록할 테넌트 id
     */
    void registerTenantWallet(CreateWalletRequest createWalletRequest);
    
    /**
     * 테넌트 ID로 테넌트 지갑 존재 여부를 확인합니다.
     * 지갑 생성 전 기존 지갑이 있는지 검증하는 용도로 사용됩니다.
     *
     * @param tenantId 확인할 테넌트 ID
     * @return 테넌트 지갑 정보 (Optional)
     */
    Optional<TenantWallet> checkExistingTenantWallet(Long tenantId);
}
