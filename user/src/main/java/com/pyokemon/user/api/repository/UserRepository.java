package com.pyokemon.user.api.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.user.api.entity.User;

@Mapper
public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    boolean existsById(@Param("id") Long id);

    void insert(User user);

    void update(User user);

    void deleteById(@Param("id") Long id);
}
