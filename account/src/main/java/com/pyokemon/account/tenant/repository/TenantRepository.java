package com.pyokemon.account.tenant.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.account.tenant.dto.response.TenantInfoDto;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.account.tenant.entity.Tenant;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TenantRepository {

  Optional<Tenant> findByTenantId(Long tenantId);

  Optional<Tenant> findByAccountId(Long accountId);

  Optional<Tenant> findByCorpId(String corpId);

  Optional<Tenant> findAccountsByTenantId(@Param("tenantId") Long tenantId);

  List<TenantInfoDto> findTenantsByIdIn(@Param("ids") List<Long> ids);

  List<Tenant> findAll();

  int insert(Tenant tenant);

  int update(Tenant tenant);

  int delete(Long tenantId);
}
