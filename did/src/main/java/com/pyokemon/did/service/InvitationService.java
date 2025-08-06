package com.pyokemon.did.service;

import com.pyokemon.did.remote.mediator.MediatorAcapyClient;
import com.pyokemon.did.remote.mediator.dto.request.CreateMediatorInvitationRequest;
import com.pyokemon.did.remote.tenant.TenantAcapyClient;
import com.pyokemon.did.remote.tenant.dto.request.CreateTenantInvitationRequest;
import com.pyokemon.did.remote.tenant.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.CreateTenantInvitationResponse;
import com.pyokemon.did.remote.tenant.dto.response.CreateWalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final MediatorAcapyClient mediatorAcapyClient;
    private final TenantAcapyClient tenantAcapyClient;
    
    @Value("${acapy.wallet.key}")
    private String walletKey;

    public CreateMediatorInvitationResponse getMediatorInvitation() {

        return mediatorAcapyClient.createInvitation(CreateMediatorInvitationRequest.generate());
    }

    public CreateTenantInvitationResponse getTenantInvitation(){

        CreateWalletRequest walletRequest = CreateWalletRequest.generate("tenant-2", walletKey);
        CreateWalletResponse walletResponse = tenantAcapyClient.createWallet(walletRequest);

        String authorization = "Bearer " + walletResponse.token();
        return tenantAcapyClient.createInvitation(
            authorization,
            CreateTenantInvitationRequest.generate("tenant-2", "Pyokemon Tenant")
        );
    }
}
