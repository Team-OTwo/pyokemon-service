package com.pyokemon.account.tenant.bff.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.account.tenant.entity.Tenant;

@Mapper
public interface TenantBffRepository {

  Optional<Tenant> findByTenantId(@Param("tenantId") Long tenantId);

}
