package com.pyokemon.account.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.account.tenant.entity.Tenant;

@Mapper
public interface TenantRepository {

  Optional<Tenant> findByTenantId(Long tenantId);

  Optional<Tenant> findByAccountId(Long accountId);

  List<Tenant> findAll();

  int insert(Tenant tenant);

  int update(Tenant tenant);

  int delete(Long tenantId);
}
