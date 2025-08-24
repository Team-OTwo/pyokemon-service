package com.pyokemon.did.service.impl;

import static com.pyokemon.common.exception.code.DidErrorCodes.WALLET_ALREADY_EXISTS;
import static com.pyokemon.common.exception.code.DidErrorCodes.WALLET_CREATION_FAILED;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.TenantWallet;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest.CreateWalletRequest;
import com.pyokemon.did.domain.repository.TenantWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreatePublicDidRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreatePublicDidResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.remote.tenantAcaPy.RemoteTenantAcaPyService;
import com.pyokemon.did.service.TenantWalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TenantWalletServiceImpl implements TenantWalletService {

  private final RemoteTenantAcaPyService remoteTenantAcaPyService;
  private final TenantWalletRepository tenantWalletRepository;

  @Override
  @Transactional
  public void registerTenantWallet(CreateWalletRequest createWalletRequest) {
    Long tenantId = createWalletRequest.getTenantId();

    // 기존 지갑 존재 여부 확인
    Optional<TenantWallet> existingWallet = checkExistingTenantWallet(tenantId);
    if (existingWallet.isPresent()) {
      log.error("테넌트 ID {}에 대한 지갑이 이미 존재합니다.", tenantId);
      throw new BusinessException("테넌트 지갑이 이미 존재합니다.", WALLET_ALREADY_EXISTS);
    }

    try {
      // 1. 지갑 생성 요청
      log.info("테넌트 ID {}에 대한 지갑 생성 요청", tenantId);
      AcaPyCreateWalletResponse walletResponse =
          remoteTenantAcaPyService.acaPyCreateWallet(AcaPyCreateWalletRequest.of());

      if (walletResponse == null || walletResponse.getToken() == null) {
        log.error("테넌트 ID {}에 대한 지갑 생성 실패: 응답이 null이거나 토큰이 없음", tenantId);
        throw new RuntimeException("지갑 생성에 실패했습니다.");
      }

      // 2. 공개 DID 생성 요청
      log.info("테넌트 ID {}에 대한 공개 DID 생성 요청", tenantId);
      AcaPyCreatePublicDidResponse publicDidResponse =
          remoteTenantAcaPyService.acaPyCreatePublicDid("Bearer " + walletResponse.getToken(),
              AcaPyCreatePublicDidRequest.of("key"));

      if (publicDidResponse == null || publicDidResponse.getResult().getDid() == null) {
        log.error("테넌트 ID {}에 대한 공개 DID 생성 실패: 응답이 null이거나 DID가 없음", tenantId);
        throw new RuntimeException("공개 DID 생성에 실패했습니다.");
      }

      // 3. 생성된 지갑 정보 저장
      log.info("테넌트 ID {}에 대한 지갑 정보 저장", tenantId);
      TenantWallet tenantWallet = TenantWallet.builder().tenantId(tenantId)
          .token(walletResponse.getToken()).publicDid(publicDidResponse.getResult().getDid())
          .publicVerkey(publicDidResponse.getResult().getVerkey()).build();

      tenantWalletRepository.save(tenantWallet);
      log.info("테넌트 ID {}에 대한 지갑 생성 및 저장 완료", tenantId);

    } catch (Exception e) {
      log.error("테넌트 ID {}에 대한 지갑 생성 중 오류 발생: {}", tenantId, e.getMessage(), e);
      throw new BusinessException("지갑 생성 중 오류가 발생했습니다", WALLET_CREATION_FAILED);
    }
  }

  @Override
  public Optional<TenantWallet> checkExistingTenantWallet(Long tenantId) {
    try {
      return tenantWalletRepository.findByTenantId(tenantId);
    } catch (Exception e) {
      log.error("테넌트 ID {}에 대한 지갑 조회 중 오류 발생: {}", tenantId, e.getMessage(), e);
      throw new RuntimeException("지갑 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
    }
  }
}
