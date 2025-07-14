package com.pyokemon.user.repository;

import com.pyokemon.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRepository {
    
    List<User> findAll();
    
    Optional<User> findById(@Param("id") Long id);
    
    Optional<User> findByUsername(@Param("username") String username);
    
    Optional<User> findByEmail(@Param("email") String email);
    
    boolean existsByUsername(@Param("username") String username);
    
    boolean existsByEmail(@Param("email") String email);
    
    void insert(User user);
    
    void update(User user);
    
    void deleteById(@Param("id") Long id);
    
    boolean existsById(@Param("id") Long id);
} 