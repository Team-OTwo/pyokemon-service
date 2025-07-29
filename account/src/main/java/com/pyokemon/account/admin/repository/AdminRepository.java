package com.pyokemon.account.admin.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.account.admin.entity.Admin;

@Mapper
public interface AdminRepository {

  Optional<Admin> findByAdminId(Long adminId);

  Optional<Admin> findByAccountId(Long accountId);

  int insert(Admin admin);

  int update(Admin admin);

  int delete(Long adminId);
}
