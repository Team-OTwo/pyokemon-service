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
     * 테넌트 ID로 테넌트 지갑을 조회합니다.
     *
     * @param tenantId 조회할 테넌트 ID
     * @return 테넌트 지갑 정보 (Optional)
     */
    Optional<TenantWallet> getWalletByTenantId(Long tenantId);
}
