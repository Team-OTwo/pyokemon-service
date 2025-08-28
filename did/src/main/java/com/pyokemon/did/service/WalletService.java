package com.pyokemon.did.service;

import java.util.Optional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.dto.request.WalletRequest.RegisterWalletRequest;

public interface WalletService {
  /**
   * 계정 지갑을 등록합니다.
   *
   * @param registerWalletRequest 등록할 계정 id
   */
  void registerWallet(RegisterWalletRequest registerWalletRequest);

  /**
   * 계정 ID로 지갑을 조회합니다.
   *
   * @param accountId 조회할 계정 ID
   * @return 지갑 정보 (Optional)
   */
  Optional<Wallet> getWalletByAccountId(Long accountId);


  /**
   * 계정 ID로 지갑을 조회하거나 찾지 못할 경우 예외를 던집니다.
   *
   * @param accountId 조회할 계정 ID
   * @return 지갑 정보
   * @throws BusinessException 지갑을 찾을 수 없는 경우
   */
  Wallet getWalletByAccountIdOrThrow(Long accountId);

  String getWalletToken(Long accountId);
}
