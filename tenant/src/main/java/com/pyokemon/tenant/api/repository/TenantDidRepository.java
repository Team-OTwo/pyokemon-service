package com.pyokemon.tenant.api.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.tenant.api.entity.TenantDid;

@Mapper
public interface TenantDidRepository {

  List<TenantDid> findAll();

  Optional<TenantDid> findById(@Param("tenantDidId") Long tenantDidId);

  List<TenantDid> findByTenantId(@Param("tenantId") Long tenantId);

  Optional<TenantDid> findByDid(@Param("did") String did);

  List<TenantDid> findValidByTenantId(@Param("tenantId") Long tenantId);

  boolean existsByDid(@Param("did") String did);

  void insert(TenantDid tenantDid);

  void update(TenantDid tenantDid);

  void deleteById(@Param("tenantDidId") Long tenantDidId);

  void updateValidStatus(@Param("tenantDidId") Long tenantDidId, @Param("isValid") Boolean isValid);
}
