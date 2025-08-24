package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.TenantWallet;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 테넌트 지갑 데이터에 접근하기 위한 Repository 인터페이스
 */
@Mapper
public interface TenantWalletRepository {

    /**
     * TenantWallet 객체를 저장합니다.
     *
     * @param tenantWallet 저장할 TenantWallet 객체
     * @return 저장된 TenantWallet의 ID
     */
    Long save(TenantWallet tenantWallet);


    /**
     * 테넌트 ID로 TenantWallet을 조회합니다.
     *
     * @param tenantId 조회할 테넌트 ID
     * @return 조회된 TenantWallet (Optional)
     */
    Optional<TenantWallet> findByTenantId(Long tenantId);

    /**
     * 테넌트 ID로 지갑 존재 여부를 확인합니다.
     *
     * @param tenantId 확인할 테넌트 ID
     * @return 지갑 존재 여부 (존재하면 true, 없으면 false)
     */
    boolean existsByTenantId(Long tenantId);


    /**
     * TenantWallet 객체를 업데이트합니다.
     *
     * @param tenantWallet 업데이트할 TenantWallet 객체
     * @return 업데이트된 행 수
     */
    int update(TenantWallet tenantWallet);

    /**
     * 테넌트 ID로 TenantWallet을 삭제합니다.
     *
     * @param tenantId 삭제할 테넌트 ID
     * @return 삭제된 행 수
     */
    int deleteByTenantId(Long tenantId);

}