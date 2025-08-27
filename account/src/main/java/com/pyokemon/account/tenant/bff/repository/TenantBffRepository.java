package com.pyokemon.account.tenant.bff.repository;

import java.util.List;
import java.util.Optional;

import com.pyokemon.account.tenant.bff.dto.TenantDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.account.tenant.entity.Tenant;

@Mapper
public interface TenantBffRepository {

  Optional<Tenant> findByTenantId(@Param("tenantId") Long tenantId);

  List<TenantDto> findTenantsByIdIn(@Param("ids") List<Long> ids);
}
