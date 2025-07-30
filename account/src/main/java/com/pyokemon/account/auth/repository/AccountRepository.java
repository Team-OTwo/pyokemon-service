package com.pyokemon.account.auth.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;

@Mapper
public interface AccountRepository {

  Optional<Account> findByLoginId(String loginId);

  Optional<Account> findByAccountId(Long accountId);

  int insert(Account account);

  int update(Account account);

  int updateStatus(Long accountId, AccountStatus status);

  int updatePassword(@Param("accountId") Long accountId, @Param("password") String password);

  boolean existsByLoginId(String loginId);
}
