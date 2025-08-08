package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.WalletMetadata;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface WalletMetadataRepository {

    int save(WalletMetadata walletMetadata);

    Optional<WalletMetadata> findById(Long id);

    Optional<WalletMetadata> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);
}
