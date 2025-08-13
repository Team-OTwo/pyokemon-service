package com.pyokemon.did.remote.mediatoracapy;

import com.pyokemon.did.remote.mediatoracapy.dto.response.InvitationResponse.AcaPyCreateMediatorInvitationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.pyokemon.did.remote.mediatoracapy.dto.request.InvitationRequest.*;

@FeignClient(
    name = "acapy-mediator-client", 
    url = "${acapy.mediator.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class
)
public interface RemoteMediatorAcaPyService {

    @PostMapping(value = "/out-of-band/create-invitation")
    public AcaPyCreateMediatorInvitationResponse createInvitation(@RequestBody AcaPyCreateMediatorInvitationRequest request);
}
