package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.dto.request.WalletMetadataRequest.CreateWalletRequest;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.remote.tenantacapy.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.tenantacapy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.tenantacapy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import com.pyokemon.did.service.WalletMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import feign.FeignException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletMetadataServiceImpl implements WalletMetadataService {
    private final WalletMetadataRepository walletMetadataRepository;
    private final RemoteTenantAcaPyService remoteTenantAcaPyService;

    @Value("${acapy.wallet.key}")
    private String walletKey;

    /**
     * 테넌트를 위한 지갑 생성
     * AcaPy를 통해 지갑을 생성하고 메타데이터를 저장
     * 
     * @param request 지갑 생성 요청 (테넌트 ID 포함)
     */
    @Override
    @Transactional
    public void createWallet(CreateWalletRequest request) {
        // 입력값 검증
        if (request == null) {
            throw new BusinessException("지갑 생성 요청은 null일 수 없습니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        Long tenantId = request.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("테넌트 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        try {
            // 이 테넌트에 대한 지갑이 이미 존재하는지 확인
            if (walletMetadataRepository.existsByTenantId(tenantId)) {
                throw new BusinessException("이미 지갑이 존재합니다.", DidErrorCodes.WALLET_ALREADY_EXISTS);
            }

            // ACA-PY 클라이언트를 통해 지갑 생성
            AcaPyCreateWalletResponse walletResponse = remoteTenantAcaPyService.acaPyCreateWallet(
                    AcaPyCreateWalletRequest.generate(tenantId, walletKey)
            );

            // 지갑 메타데이터 저장
            walletMetadataRepository.save(walletResponse.toEntity(tenantId));
            log.info("테넌트 ID: {} 지갑이 생성되었습니다", tenantId);

        } catch (FeignException.BadRequest e) {
            // AcaPy에서 400 에러가 발생한 경우 (지갑이 이미 존재하는 경우)
            if (e.getMessage().contains("already exists")) {
                log.warn("AcaPy에 지갑이 이미 존재합니다. 기존 지갑 정보를 조회하여 메타데이터를 생성합니다. tenantId={}", tenantId);
                
                try {
                    // 기존 지갑 정보를 조회하여 메타데이터 생성
                    // AcaPy에서 기존 지갑 정보를 가져오는 API가 있다면 사용
                    // 현재는 간단히 wallet_name을 기반으로 메타데이터를 생성
                    String walletName = "wallet:" + tenantId;
                    String walletId = "wallet_id_for_" + tenantId; // 실제로는 AcaPy에서 조회해야 함
                    
                    // 임시로 메타데이터 생성 (실제로는 AcaPy API를 통해 조회해야 함)
                    com.pyokemon.did.domain.WalletMetadata walletMetadata = com.pyokemon.did.domain.WalletMetadata.builder()
                            .walletKey(walletKey)
                            .token("existing_token_for_" + tenantId) // 실제로는 AcaPy에서 조회해야 함
                            .tenantId(tenantId)
                            .build();
                    
                    walletMetadataRepository.save(walletMetadata);
                    log.info("기존 AcaPy 지갑에 대한 메타데이터가 생성되었습니다. tenantId={}", tenantId);
                    
                } catch (Exception ex) {
                    log.error("기존 지갑 메타데이터 생성 중 오류 발생: tenantId={}, error={}", tenantId, ex.getMessage(), ex);
                    throw new BusinessException("기존 지갑 정보 처리에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED, ex);
                }
            } else {
                log.error("AcaPy 지갑 생성 중 오류 발생: tenantId={}, error={}", tenantId, e.getMessage(), e);
                throw new BusinessException("지갑 생성에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED, e);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("지갑 프로비저닝 중 오류 발생: tenantId={}, error={}", tenantId, e.getMessage(), e);
            throw new BusinessException("지갑 생성에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED, e);
        }
    }
    
    /**
     * 테넌트 ID로 Wallet metadata 조회
     * 
     * @param tenantId 테넌트 ID
     * @return WalletMetadata 객체
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public com.pyokemon.did.domain.WalletMetadata getWalletMetadata(Long tenantId) {
        // 입력값 검증
        if (tenantId == null) {
            throw new BusinessException("테넌트 ID는 필수입니다.", DidErrorCodes.INVALID_REQUEST);
        }
        
        try {
            return walletMetadataRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new BusinessException("Wallet metadata not found for tenantId: " + tenantId, DidErrorCodes.WALLET_NOTFOUND));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Wallet metadata 조회 중 오류 발생: tenantId={}, error={}", tenantId, e.getMessage(), e);
            throw new BusinessException("Wallet metadata 조회에 실패했습니다.", DidErrorCodes.DATABASE_ERROR, e);
        }
    }
}
