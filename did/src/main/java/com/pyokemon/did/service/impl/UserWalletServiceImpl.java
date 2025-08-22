package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.UserWalletService;
import com.pyokemon.common.dto.ResponseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWalletServiceImpl implements UserWalletService {

    private final UserWalletRepository userWalletRepository;
    private final RemoteUserAcaPyService remoteUserAcaPyService;


    @Override
    @Transactional
    public ResponseEntity<ResponseDto<Map<String, String>>> createUserWallet(String userId) {
        // 사전 검증
        validateWalletCreation(userId);
        
        // 로컬 DB에 이미 존재하는지 먼저 확인
        if (existsByUserId(userId)) {
            log.info("로컬 DB에 이미 지갑이 존재: userId={}", userId);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseDto.error("이미 지갑이 존재하는 사용자입니다.", DidErrorCodes.WALLET_ALREADY_EXISTS));
        }
        
        try {
            // ACA-Py에 지갑 생성 요청
            String token = createWalletInAcaPy(userId);
            
            // 로컬 DB에 저장
            saveWalletToDatabase(userId, token);
            
            return ResponseEntity.ok(ResponseDto.success(Map.of("userId", userId), "사용자 지갑 생성 완료"));
            
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.error(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("지갑 생성 중 예상치 못한 오류: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ResponseDto.error("지갑 생성에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED));
        }
    }
    

    private void validateWalletCreation(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BusinessException("사용자 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
    }
    
        private String createWalletInAcaPy(String userId) {
        try {
            AcaPyCreateWalletRequest request = AcaPyCreateWalletRequest.generate(userId);
            log.info("=== ACA-Py 지갑 생성 요청 ===");
            log.info("userId: {}", userId);
            log.info("request: {}", request);
            
            AcaPyCreateWalletResponse response = remoteUserAcaPyService.acaPyCreateWallet(request);
            log.info("=== ACA-Py 지갑 생성 응답 ===");
            log.info("userId: {}", userId);
            log.info("walletId: {}", response.getWalletId());
            log.info("token: {}", response.getToken());
            log.info("createdAt: {}", response.getCreatedAt());
            log.info("updatedAt: {}", response.getUpdatedAt());
            log.info("keyManagementMode: {}", response.getKeyManagementMode());
            
            return response.getToken();
            
        } catch (FeignException.BadRequest e) {
            log.error("ACA-Py BadRequest 오류: userId={}, status={}, message={}", userId, e.status(), e.getMessage());
            throw new BusinessException("ACA-Py 지갑 생성 요청이 잘못되었습니다.", DidErrorCodes.INVALID_REQUEST, e);
            
        } catch (FeignException.NotFound e) {
            log.error("ACA-Py NotFound 오류: userId={}, status={}, message={}", userId, e.status(), e.getMessage());
            throw new BusinessException("ACA-Py 서비스를 찾을 수 없습니다.", DidErrorCodes.ACAPY_SERVICE_ERROR, e);
            
        } catch (FeignException.ServiceUnavailable e) {
            log.error("ACA-Py ServiceUnavailable 오류: userId={}, status={}, message={}", userId, e.status(), e.getMessage());
            throw new BusinessException("ACA-Py 서비스가 일시적으로 사용할 수 없습니다.", DidErrorCodes.ACAPY_SERVICE_ERROR, e);
            
        } catch (FeignException e) {
            log.error("ACA-Py FeignException 오류: userId={}, status={}, message={}", userId, e.status(), e.getMessage());
            throw new BusinessException("ACA-Py 서비스 통신 중 오류가 발생했습니다.", DidErrorCodes.ACAPY_SERVICE_ERROR, e);
            
        } catch (Exception e) {
            log.error("ACA-Py 예상치 못한 오류: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException("ACA-Py 지갑 생성 중 예상치 못한 오류가 발생했습니다.", DidErrorCodes.WALLET_CREATION_FAILED, e);
        }
    }
    
    private UserWallet saveWalletToDatabase(String userId, String token) {
        UserWallet userWallet = UserWallet.builder()
                .userId(userId)
                .token(token)
                .build();
        
        // INSERT 실행 후 생성된 ID를 가져옴
        int affectedRows = userWalletRepository.saveAndReturn(userWallet);
        
        if (affectedRows == 0) {
            throw new BusinessException("지갑 저장에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED);
        }
        
        // 생성된 ID로 완전한 데이터를 조회하여 반환
        return userWalletRepository.findById(userWallet.getId())
                .orElseThrow(() -> new BusinessException("지갑 저장 후 조회에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED));
    }
    


    @Override
    public UserWallet getUserWallet(String userId) {
        return userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("해당 userId의 지갑을 찾을 수 없습니다: " + userId, DidErrorCodes.WALLET_NOTFOUND));
    }

    @Override
    public boolean existsByUserId(String userId) {
        return userWalletRepository.findByUserId(userId).isPresent();
    }

    @Override
    // 모든 사용자 지갑 조회
    public List<UserWallet> getAllUserWallets() {
        return userWalletRepository.findAll();
    }

    @Override
    public UserWallet getUserWalletByToken(String token) {
        return userWalletRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("토큰에 해당하는 지갑을 찾을 수 없습니다", DidErrorCodes.WALLET_NOTFOUND));
    }
}
