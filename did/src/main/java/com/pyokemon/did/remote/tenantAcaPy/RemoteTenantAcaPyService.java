package com.pyokemon.did.remote.tenantAcaPy;

import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreatePublicDidResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "remote-AcaPy-tenant-service",
    url = "${acapy.tenant.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class
)
public interface RemoteTenantAcaPyService {

    @PostMapping(value="/multitenancy/wallet")
    public AcaPyCreateWalletResponse acaPyCreateWallet(@RequestBody AcaPyCreateWalletRequest request);

    @PostMapping(value = "/wallet/did/create")
    public AcaPyCreatePublicDidResponse acaPyCreatePublicDid(@RequestBody AcaPyCreatePublicDidResponse request);

    @PostMapping(value="/out-of-band/create-invitation?auto_accept=true&multi_use=true")
    public AcaPyCreateInvitationResponse createInvitation(
        @RequestHeader("Authorization") String authorization,
        @RequestBody AcaPyCreateInvitationRequest request
    );
}
