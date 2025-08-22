package com.pyokemon.account.tenant.bff.repository;

import com.pyokemon.account.tenant.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface TenantBffRepository {

    Optional<Tenant> findByTenantId(@Param("tenantId") Long tenantId);

}
