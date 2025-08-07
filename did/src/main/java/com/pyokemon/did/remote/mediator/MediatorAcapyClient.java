package com.pyokemon.did.remote.mediator;

import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.mediator.dto.request.CreateMediatorInvitationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "acapy-mediator-client", 
    url = "${acapy.mediator.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class
)
public interface MediatorAcapyClient {

    @PostMapping(value = "/out-of-band/create-invitation")
    public CreateMediatorInvitationResponse createInvitation(@RequestBody CreateMediatorInvitationRequest request);
}
