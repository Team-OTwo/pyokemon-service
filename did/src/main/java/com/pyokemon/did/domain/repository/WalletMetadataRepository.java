package com.pyokemon.did.domain.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.did.domain.WalletMetadata;

@Mapper
public interface WalletMetadataRepository {

  int save(WalletMetadata walletMetadata);

  Optional<WalletMetadata> findById(Long id);

  Optional<WalletMetadata> findByTenantId(Long tenantId);

  boolean existsByTenantId(Long tenantId);
}
