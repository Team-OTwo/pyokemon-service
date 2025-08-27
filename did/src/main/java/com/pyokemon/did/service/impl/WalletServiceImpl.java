package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;

import java.util.Optional;

import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.Wallet.AccountRole;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.dto.request.WalletRequest.CreateWalletRequest;
import com.pyokemon.did.domain.repository.WalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreatePublicDidRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreatePublicDidResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.tenantAcaPy.RemoteTenantAcaPyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
  private final RemoteTenantAcaPyService remoteTenantAcaPyService;
  private final RemoteUserAcaPyService remoteUserAcaPyService;
  private final WalletRepository walletRepository;

  @Override
  @Transactional
  public void registerWallet(CreateWalletRequest createWalletRequest)
      throws BusinessException {
    Long accountId = createWalletRequest.getAccountId();
    AccountRole accountRole = createWalletRequest.getAccountRole();

    try {
      // 1. 기존 지갑 존재 여부 확인
      boolean exists = walletRepository.existsByAccountId(accountId);
      if (exists) {
        log.error("{} ID: {}에 대한 지갑이 이미 존재합니다.", accountRole, accountId);
        throw new BusinessException("계정 지갑이 이미 존재합니다.", WALLET_ALREADY_EXISTS);
      }
      
      // 2. 적절한 AcaPy 서비스 선택
      RemoteAcaPyService remoteService = getRemoteServiceByRole(accountRole);
      
      // 3. 지갑 생성
      log.info("{} ID: {}에 대한 지갑 생성 요청", accountRole, accountId);
      AcaPyCreateWalletResponse walletResponse = remoteService.acaPyCreateWallet(
              AcaPyCreateWalletRequest.of(accountRole)
      );
      if (walletResponse == null || walletResponse.getToken() == null) {
        throw new BusinessException("지갑 생성에 실패했습니다.", WALLET_CREATION_FAILED);
      }
      
      // 4. 공개 DID 생성
      log.info("{} ID: {}에 대한 공개 DID 생성 요청", accountRole, accountId);
      AcaPyCreatePublicDidResponse publicDidResponse = remoteService.acaPyCreatePublicDid(
              "Bearer " + walletResponse.getToken(),AcaPyCreatePublicDidRequest.of("key")
      );
      if (publicDidResponse == null || publicDidResponse.getResult() == null
              || publicDidResponse.getResult().getDid() == null) {
        throw new BusinessException("공개 DID 생성에 실패했습니다.", DID_CREATION_FAILED);
      }
      
      // 5. 지갑 정보 저장
      walletRepository.save(publicDidResponse.toEntity(accountId, accountRole, walletResponse.getToken()));
      log.info("{} ID: {}에 대한 지갑 생성 및 저장 완료", accountRole, accountId);

    } catch (BusinessException e) {
      // 비즈니스 예외는 그대로 전파
      throw e;
    } catch (Exception e) {
      // 외부 API 호출 중 발생한 예외 처리
      log.error("{} ID: {}에 대한 외부 API 호출 중 오류 발생: {}", accountRole, accountId, e.getMessage(), e);
      throw new BusinessException("외부 시스템 연동 중 오류가 발생했습니다", WALLET_CREATION_FAILED);
    }
  }
  
  /**
   * 계정 역할에 따라 적절한 원격 서비스를 반환
   */
  private interface RemoteAcaPyService {
    AcaPyCreateWalletResponse acaPyCreateWallet(AcaPyCreateWalletRequest request);
    AcaPyCreatePublicDidResponse acaPyCreatePublicDid(String authToken, AcaPyCreatePublicDidRequest request);
  }
  
  /**
   * 계정 역할에 따라 적절한 원격 서비스를 반환
   */
  private RemoteAcaPyService getRemoteServiceByRole(AccountRole accountRole) {
    if (accountRole == AccountRole.TENANT) {
      return new RemoteAcaPyService() {
        @Override
        public AcaPyCreateWalletResponse acaPyCreateWallet(AcaPyCreateWalletRequest request) {
          return remoteTenantAcaPyService.acaPyCreateWallet(request);
        }
        
        @Override
        public AcaPyCreatePublicDidResponse acaPyCreatePublicDid(String authToken, AcaPyCreatePublicDidRequest request) {
          return remoteTenantAcaPyService.acaPyCreatePublicDid(authToken, request);
        }
      };
    } else if (accountRole == AccountRole.USER) {
      return new RemoteAcaPyService() {
        @Override
        public AcaPyCreateWalletResponse acaPyCreateWallet(AcaPyCreateWalletRequest request) {
          return remoteUserAcaPyService.acaPyCreateWallet(request);
        }
        
        @Override
        public AcaPyCreatePublicDidResponse acaPyCreatePublicDid(String authToken, AcaPyCreatePublicDidRequest request) {
          return remoteUserAcaPyService.acaPyCreatePublicDid(authToken, request);
        }
      };
    } else {
      throw new BusinessException("유효하지 않은 계정입니다.", WALLET_CREATION_FAILED);
    }
  }


  @Override
  public Optional<Wallet> getWalletByAccountId(Long accountId) {
    return walletRepository.findByAccountId(accountId);

  }
}
