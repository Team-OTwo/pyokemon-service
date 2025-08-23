package com.pyokemon.did.service;

public interface UserWalletService {

  /**
   * 사용자 지갑 생성 1. User ACA-Py에 지갑 생성 요청 2. 응답받은 토큰으로 로컬 DB에 저장
   * 
   * @param userId 사용자 ID
   */
  void createUserWallet(Long userId);
}
