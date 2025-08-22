package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWalletServiceImpl implements UserWalletService {

    private final UserWalletRepository userWalletRepository;
    private final RemoteUserAcaPyService remoteUserAcaPyService;


    @Override
    @Transactional
    public void createUserWallet(Long userId) {
        // 기존 지갑 존재 여부 확인
        Optional<UserWallet> existingWallet = checkExistingUserWallet(userId);
        if (existingWallet.isPresent()) {
            log.error("사용자 ID {}에 대한 지갑이 이미 존재합니다.", userId);
            throw new BusinessException("사용자 지갑이 이미 존재합니다.", DidErrorCodes.WALLET_ALREADY_EXISTS);
        }

        try {
            // 1. 지갑 생성 요청
            log.info("사용자 ID {}에 대한 지갑 생성 요청", userId);
            AcaPyCreateWalletResponse walletResponse = remoteUserAcaPyService.acaPyCreateWallet(
                AcaPyCreateWalletRequest.generate(userId)
            );
            
            if (walletResponse == null || walletResponse.getToken() == null) {
                log.error("사용자 ID {}에 대한 지갑 생성 실패: 응답이 null이거나 토큰이 없음", userId);
                throw new RuntimeException("지갑 생성에 실패했습니다.");
            }
            
            // 2. 생성된 지갑 정보 저장
            log.info("사용자 ID {}에 대한 지갑 정보 저장", userId);
            UserWallet userWallet = UserWallet.builder()
                .userId(userId)
                .token(walletResponse.getToken())
                .build();
                
            userWalletRepository.saveAndReturn(userWallet);
            log.info("사용자 ID {}에 대한 지갑 생성 및 저장 완료", userId);
            
        } catch (Exception e) {
            log.error("사용자 ID {}에 대한 지갑 생성 중 오류 발생: {}", userId, e.getMessage(), e);
            throw new BusinessException("지갑 생성 중 오류가 발생했습니다", DidErrorCodes.WALLET_CREATION_FAILED);
        }
    }

    private Optional<UserWallet> checkExistingUserWallet(Long userId) {
        try {
            return userWalletRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("사용자 ID {}에 대한 지갑 조회 중 오류 발생: {}", userId, e.getMessage(), e);
            throw new RuntimeException("지갑 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    


    



}
