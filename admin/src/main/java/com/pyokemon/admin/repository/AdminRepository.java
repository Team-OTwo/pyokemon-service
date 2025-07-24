package com.pyokemon.admin.repository;

import com.pyokemon.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AdminRepository {
    // List<Admin> findAll();
    // Optional<Admin> findById(Long id);
    Optional<Admin> findByUsername(String username);
    
    /**
     * 관리자 계정을 저장합니다.
     * 
     * @param admin 저장할 관리자 정보
     * @return 저장된 행 수
     */
    int save(Admin admin);
} 