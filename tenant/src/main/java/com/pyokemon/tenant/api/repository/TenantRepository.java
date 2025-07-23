package com.pyokemon.tenant.api.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.tenant.api.entity.Tenant;

@Mapper
public interface TenantRepository {

    List<Tenant> findAll();

    Optional<Tenant> findById(@Param("id") Long id);

    Optional<Tenant> findByEmail(@Param("email") String email);

    Optional<Tenant> findByCorpId(@Param("corpId") String corpId);

    boolean existsByEmail(@Param("email") String email);

    boolean existsByCorpId(@Param("corpId") String corpId);

    void insert(Tenant tenant);

    void update(Tenant tenant);

    void deleteById(@Param("id") Long id);

    boolean existsById(@Param("id") Long id);
}
