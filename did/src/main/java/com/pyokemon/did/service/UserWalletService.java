package com.pyokemon.did.service;

public interface UserWalletService {

  /**
   * 사용자 지갑 생성 1. User ACA-Py에 지갑 생성 요청 2. 응답받은 토큰으로 로컬 DB에 저장
   * 
   * @param userId 사용자 ID
   */
  void createUserWallet(Long userId);

  /**
   * 사용자 지갑을 조회하고 토큰을 검증합니다.
   * 
   * @param userId 사용자 ID
   * @return 사용자 지갑 토큰
   * @throws com.pyokemon.common.exception.BusinessException 지갑이 없거나 토큰이 유효하지 않은 경우
   */
  String getUserWalletToken(Long userId);
}
