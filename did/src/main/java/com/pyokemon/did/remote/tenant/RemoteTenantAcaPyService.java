package com.pyokemon.did.remote.tenant;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.pyokemon.did.remote.tenant.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.tenant.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.tenant.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.tenant.dto.response.WalletResponse.AcaPyCreateWalletResponse;

@FeignClient(name = "remote-AcaPy-tenant-service", url = "${acapy.tenant.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class)
public interface RemoteTenantAcaPyService {

  @PostMapping(value = "/multitenancy/wallet")
  public AcaPyCreateWalletResponse acaPyCreateWallet(@RequestBody AcaPyCreateWalletRequest request);

  @PostMapping(
      value = "/out-of-band/create-invitation?auto_accept=true&create_unique_did=true&multi_use=true")
  public AcaPyCreateInvitationResponse createInvitation(
      @RequestHeader("Authorization") String authorization,
      @RequestBody AcaPyCreateInvitationRequest request);
}
