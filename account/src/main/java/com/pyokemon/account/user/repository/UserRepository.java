package com.pyokemon.account.user.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.account.user.entity.User;

@Mapper
public interface UserRepository {

  Optional<User> findByUserId(@Param("id") Long id);

  Optional<User> findByAccountId(@Param("accountId") Long accountId);

  int insert(User user);

  int update(User user);

  int delete(@Param("id") Long id);
}
