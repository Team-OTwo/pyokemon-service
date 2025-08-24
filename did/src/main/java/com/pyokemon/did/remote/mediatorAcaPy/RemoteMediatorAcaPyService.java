package com.pyokemon.did.remote.mediatorAcaPy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.config.MediatorFeignClientConfiguration;

@FeignClient(name = "acapy-mediator-client", url = "${acapy.mediator.base-url}",
    configuration = MediatorFeignClientConfiguration.class)
public interface RemoteMediatorAcaPyService {

  @PostMapping(value = "/out-of-band/create-invitation")
  public AcaPyCreateInvitationResponse acaPyCreateInvitation(
      @RequestBody AcaPyCreateInvitationRequest request);
}
