package com.pyokemon.account.auth.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;

@Mapper
public interface AccountRepository {

  Optional<Account> findByLoginId(String loginId);

  Optional<Account> findByLoginIdAndStatus(String loginId, AccountStatus status);

  Optional<Account> findByAccountId(Long id);

  int insert(Account account);

  int update(Account account);

  int updateStatus(Long id, AccountStatus status);

  int updatePassword(@Param("id") Long id, @Param("password") String password);

  boolean existsByLoginId(String loginId);

  boolean existsByLoginIdAndStatus(String loginId, AccountStatus status);
}
