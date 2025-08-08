package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.dto.request.ProvisionWalletRequest;
import com.pyokemon.did.remote.tenant.TenantAcapyClient;
import com.pyokemon.did.remote.tenant.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.CreateWalletResponse;
import com.pyokemon.did.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지갑 관리 작업을 위한 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletMetadataRepository walletMetadataRepository;
    private final TenantAcapyClient tenantAcapyClient;

    @Value("${acapy.wallet.key}")
    private String walletKey;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void provisionWallet(ProvisionWalletRequest request) {
        Long tenantId = request.getTenantId();
        
        // 이 테넌트에 대한 지갑이 이미 존재하는지 확인
        if (walletMetadataRepository.existsByTenantId(tenantId)) {
            throw new BusinessException("이미 지갑이 존재합니다.", DidErrorCodes.WALLET_ALREADY_EXISTS);
        }

        try {
            // ACA-PY 클라이언트를 통해 지갑 생성
            CreateWalletResponse walletResponse = tenantAcapyClient.createWallet(
                CreateWalletRequest.create(tenantId, walletKey)
            );

            // 지갑 메타데이터 저장
            WalletMetadata walletMetadata = walletResponse.toEntity(tenantId);
            log.info("테넌트 ID: {}, 지갑 ID: {}로 지갑이 생성되었습니다", tenantId, walletMetadata.getKey());

            walletMetadataRepository.save(walletMetadata);
        } catch (Exception e) {
            log.error("지갑 프로비저닝 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException("지갑 생성에 실패했습니다.", DidErrorCodes.WALLET_CREATION_FAILED, e);
        }
    }
}
