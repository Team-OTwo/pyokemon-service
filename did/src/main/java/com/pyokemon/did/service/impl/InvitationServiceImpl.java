package com.pyokemon.did.service.impl;

import com.pyokemon.did.remote.mediator.MediatorAcapyClient;
import com.pyokemon.did.remote.mediator.dto.request.CreateMediatorInvitationRequest;
import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.tenant.TenantAcapyClient;
import com.pyokemon.did.remote.tenant.dto.request.CreateTenantInvitationRequest;
import com.pyokemon.did.remote.tenant.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.CreateTenantInvitationResponse;
import com.pyokemon.did.remote.tenant.dto.response.CreateWalletResponse;
import com.pyokemon.did.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 초대장 관리 작업을 위한 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final MediatorAcapyClient mediatorAcapyClient;
    private final TenantAcapyClient tenantAcapyClient;
    
    @Value("${acapy.wallet.key}")
    private String walletKey;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateMediatorInvitationResponse getMediatorInvitation() {
        return mediatorAcapyClient.createInvitation(CreateMediatorInvitationRequest.generate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateTenantInvitationResponse getTenantInvitation() {
        CreateWalletRequest walletRequest = CreateWalletRequest.create(1L, walletKey);
        CreateWalletResponse walletResponse = tenantAcapyClient.createWallet(walletRequest);

        String authorization = "Bearer " + walletResponse.token();
        return tenantAcapyClient.createInvitation(
            authorization,
            CreateTenantInvitationRequest.generate("tenant-2", "Pyokemon Tenant")
        );
    }
}
