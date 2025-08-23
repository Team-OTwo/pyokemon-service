package com.pyokemon.did.remote.userAcaPy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyReceiveInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;

@FeignClient(name = "remote-AcaPy-user-service", url = "${acapy.user.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class)
public interface RemoteUserAcaPyService {

  @PostMapping(value = "/multitenancy/wallet")
  public AcaPyCreateWalletResponse acaPyCreateWallet(@RequestBody AcaPyCreateWalletRequest request);

  @PostMapping(value = "/out-of-band/receive-invitation")
  public AcaPyReceiveInvitationResponse acaPyReceiveInvitation();

}
