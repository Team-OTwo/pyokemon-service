package com.pyokemon.did.remote.userAcaPy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyReceiveInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyReceiveInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreatePublicDidResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse.AcaPyCreateWalletResponse;

@FeignClient(name = "remote-AcaPy-user-service", url = "${acapy.user.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class)
public interface RemoteUserAcaPyService {

  @PostMapping(value = "/multitenancy/wallet")
  public AcaPyCreateWalletResponse acaPyCreateWallet(@RequestBody AcaPyCreateWalletRequest request);

  @PostMapping(value = "/wallet/did/create")
  public AcaPyCreatePublicDidResponse acaPyCreatePublicDid(
      @RequestHeader("Authorization") String authorization,
      @RequestBody WalletRequest.AcaPyCreatePublicDidRequest request);

  @PostMapping(value = "/out-of-band/create-invitation")
  public AcaPyCreateInvitationResponse acaPyCreateInvitation(
      @RequestHeader("Authorization") String authorization,
      @RequestBody AcaPyCreateInvitationRequest request);

  @PostMapping(value = "/out-of-band/receive-invitation")
  public AcaPyReceiveInvitationResponse acaPyReceiveInvitation(
      @RequestHeader("Authorization") String authorization,
      @RequestBody AcaPyReceiveInvitationRequest request);

}
