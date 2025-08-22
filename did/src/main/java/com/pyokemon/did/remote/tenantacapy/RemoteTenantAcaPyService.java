package com.pyokemon.did.remote.tenantacapy;

import com.pyokemon.did.remote.tenantacapy.dto.request.InvitationRequest;
import com.pyokemon.did.remote.tenantacapy.dto.request.InvitationRequest.AcaPyCreateTenantInvitationRequest;
import com.pyokemon.did.remote.tenantacapy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.common.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.tenantacapy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "remote-AcaPy-tenant-service",
    url = "${acapy.tenant.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class
)
public interface RemoteTenantAcaPyService {

    @PostMapping(value="/multitenancy/wallet")
    public AcaPyCreateWalletResponse acaPyCreateWallet(@RequestBody AcaPyCreateWalletRequest request);

    @DeleteMapping(value="/multitenancy/wallet/{walletId}")
    public void acaPyDeleteWallet(@PathVariable("walletId") String walletId);

    @PostMapping(value="/out-of-band/create-invitation?auto_accept=true&multi_use=true")
    public AcaPyCreateInvitationResponse createInvitation(
        @RequestHeader("Authorization") String authorization,
        @RequestBody AcaPyCreateTenantInvitationRequest request
    );
}
