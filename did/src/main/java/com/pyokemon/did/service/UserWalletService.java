package com.pyokemon.did.service;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.domain.UserWallet;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserWalletService {
    
    /**
     * 사용자 지갑 생성
     * 1. User ACA-Py에 지갑 생성 요청
     * 2. 응답받은 토큰으로 로컬 DB에 저장
     * 
     * @param userId 사용자 ID
     * @return ResponseEntity (성공 시 userId, 실패 시 에러 응답)
     */
    ResponseEntity<ResponseDto<Map<String, String>>> createUserWallet(Long userId);
    
    /**
     * 사용자 지갑 조회
     * 
     * @param userId 사용자 ID
     * @return UserWallet 엔티티 (없으면 null)
     */
    UserWallet getUserWallet(Long userId);
    
    /**
     * 사용자 지갑 존재 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByUserId(Long userId);

    public List<UserWallet> getAllUserWallets();

    public UserWallet getUserWalletByToken(String token);
}
