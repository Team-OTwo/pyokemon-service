package com.pyokemon.did.service;

import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.dto.request.CreateWalletRequestDto;

import com.pyokemon.did.remote.tenant.TenantAcapyClient;
import com.pyokemon.did.remote.tenant.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.CreateWalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletMetadataService {
    private final WalletMetadataRepository walletMetadataRepository;
    private final TenantAcapyClient tenantAcapyClient;

    @Value("${acapy.wallet.key}")
    private String walletKey;


    public void createWallet(CreateWalletRequestDto requestDto) {

        CreateWalletResponse wallet = tenantAcapyClient.createWallet(CreateWalletRequest.generate(requestDto.getTenantId(), walletKey));

        log.info("wallet: {}", wallet);
    }
}

