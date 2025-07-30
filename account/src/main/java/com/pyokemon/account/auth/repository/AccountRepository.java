package com.pyokemon.account.auth.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;

@Mapper
public interface AccountRepository {

  Optional<Account> findByLoginId(String loginId);

  Optional<Account> findByAccountId(Long accountId);

  int insert(Account account);

  int update(Account account);

  int updateStatus(Long accountId, AccountStatus status);

  boolean existsByLoginId(String loginId);
}
