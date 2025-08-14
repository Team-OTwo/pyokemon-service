package com.pyokemon.account.user.bff.repository;

import com.pyokemon.account.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserBffRepository {

  Optional<User> findByAccountId(@Param("accountId") Long accountId);

}
