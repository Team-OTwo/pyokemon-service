package com.pyokemon.tenant.api.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.tenant.api.entity.Tenant;

@Mapper
public interface TenantRepository {

  Optional<Tenant> findByCorpId(@Param("corpId") String corpId);

  Optional<Tenant> findByLoginId(@Param("loginId") String loginId); // login()에서 사용

  void insert(Tenant tenant);

  List<Tenant> findAll(); // getAllTenants()에서 사용

  Optional<Tenant> findById(Long id); // getTenantById()에서 사용

  boolean existsByCorpId(String corpId); // createTenant()에서 사용 (사업자번호 중복)

  boolean existsByLoginId(@Param("loginId") String loginId); // createTenant()에서 사용 (로그인ID 중복)

  boolean existsById(@Param("id") Long id); // deleteTenant()에서 사용 (존재 여부 확인)

  void update(Tenant tenant); // 나중에 updateProfile()에서 사용

  void deleteById(Long id); // 나중에 deleteTenant()에서 사용
}
