package com.pyokemon.did.service;

import java.util.Optional;

import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.dto.request.WalletRequest.CreateWalletRequest;

public interface WalletService {
  /**
   * 계정 지갑을 등록합니다.
   *
   * @param createWalletRequest 등록할 계정 id
   */
  void registerWallet(CreateWalletRequest createWalletRequest);

  /**
   * 계정 ID로 지갑을 조회합니다.
   *
   * @param accountId 조회할 계정 ID
   * @return 지갑 정보 (Optional)
   */
  Optional<Wallet> getWalletByAccountId(Long accountId);

}
