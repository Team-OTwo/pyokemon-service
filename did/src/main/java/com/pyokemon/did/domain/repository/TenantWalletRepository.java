package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.TenantWallet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TenantWalletRepository {

    /**
     * TenantWallet을 저장합니다.
     *
     * @param tenantWallet 저장할 TenantWallet
     * @return 저장된 TenantWallet의 ID
     */
    Long save(TenantWallet tenantWallet);

    /**
     * ID로 TenantWallet을 조회합니다.
     *
     * @param id 조회할 TenantWallet의 ID
     * @return TenantWallet (Optional)
     */
    Optional<TenantWallet> findById(Long id);

    /**
     * tenant_id로 TenantWallet을 조회합니다.
     *
     * @param tenantId 조회할 tenant_id
     * @return TenantWallet (Optional)
     */
    Optional<TenantWallet> findByTenantId(Long tenantId);

    /**
     * token으로 TenantWallet을 조회합니다.
     *
     * @param token 조회할 token
     * @return TenantWallet (Optional)
     */
    Optional<TenantWallet> findByToken(String token);

    /**
     * public_did로 TenantWallet을 조회합니다.
     *
     * @param publicDid 조회할 public_did
     * @return TenantWallet (Optional)
     */
    Optional<TenantWallet> findByPublicDid(String publicDid);

    /**
     * public_verkey로 TenantWallet을 조회합니다.
     *
     * @param publicVerkey 조회할 public_verkey
     * @return TenantWallet (Optional)
     */
    Optional<TenantWallet> findByPublicVerkey(String publicVerkey);

    /**
     * 모든 TenantWallet 목록을 조회합니다.
     *
     * @return TenantWallet 목록
     */
    List<TenantWallet> findAll();

    /**
     * TenantWallet을 업데이트합니다.
     *
     * @param tenantWallet 업데이트할 TenantWallet
     * @return 업데이트된 행 수
     */
    int update(TenantWallet tenantWallet);

    /**
     * ID로 TenantWallet을 삭제합니다.
     *
     * @param id 삭제할 TenantWallet의 ID
     * @return 삭제된 행 수
     */
    int deleteById(Long id);

    /**
     * tenant_id로 TenantWallet을 삭제합니다.
     *
     * @param tenantId 삭제할 tenant_id
     * @return 삭제된 행 수
     */
    int deleteByTenantId(Long tenantId);

    /**
     * token으로 TenantWallet을 삭제합니다.
     *
     * @param token 삭제할 token
     * @return 삭제된 행 수
     */
    int deleteByToken(String token);
}
