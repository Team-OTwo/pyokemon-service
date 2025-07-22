package com.pyokemon.admin.repository;

import com.pyokemon.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AdminRepository {
    List<Admin> findAll();
    Optional<Admin> findById(Long id);
    Optional<Admin> findByUsername(String username);
    Admin save(Admin admin);
    void deleteById(Long id);
} 