package com.pyokemon.did.remote.tenant;

import com.pyokemon.did.remote.tenant.dto.request.CreateTenantInvitationRequest;
import com.pyokemon.did.remote.tenant.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.CreateTenantInvitationResponse;
import com.pyokemon.did.remote.tenant.dto.response.CreateWalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "acapy-tenant-client", 
    url = "${acapy.tenant.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class
)
public interface TenantAcapyClient {

    @PostMapping(value="/multitenancy/wallet")
    public CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request);

    @PostMapping(value="/out-of-band/create-invitation?auto_accept=true&create_unique_did=true&multi_use=false")
    public CreateTenantInvitationResponse createInvitation(
        @RequestHeader("Authorization") String authorization,
        @RequestBody CreateTenantInvitationRequest request
    );
}
