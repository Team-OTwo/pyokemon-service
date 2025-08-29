package com.pyokemon.account.user.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.account.user.entity.User;

@Mapper
public interface UserRepository {

  Optional<User> findByAccountId(@Param("accountId") Long accountId);

  List<User> findAllByAccountIdIn(@Param("ids") List<Long> ids);

  int insert(User user);

  int update(User user);

  int delete(@Param("userId") Long userId);
}
