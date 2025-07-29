package com.pyokemon.admin.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.admin.entity.Admin;

@Mapper
public interface AdminRepository {
  Optional<Admin> findByUsername(String username);

  Optional<Admin> findByAdminId(String adminId);

  /**
   * 관리자 계정을 저장합니다.
   * 
   * @param admin
   * @return
   */
  int save(Admin admin);
}
