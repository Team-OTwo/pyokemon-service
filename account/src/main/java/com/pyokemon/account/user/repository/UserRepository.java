package com.pyokemon.account.user.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.account.user.entity.User;

@Mapper
public interface UserRepository {

  Optional<User> findByUserId(Long userId);

  Optional<User> findByAccountId(Long accountId);

  int insert(User user);

  int update(User user);

  int delete(Long userId);
}
