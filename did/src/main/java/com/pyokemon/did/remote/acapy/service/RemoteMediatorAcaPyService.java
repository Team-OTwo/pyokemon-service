package com.pyokemon.did.remote.acapy.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.config.MediatorFeignClientConfiguration;

/**
 * 중재자 ACA-Py 서비스와의 원격 통신을 위한 인터페이스
 */
@FeignClient(name = "acapy-mediator-client", url = "${acapy.mediator.base-url}",
    configuration = MediatorFeignClientConfiguration.class)
public interface RemoteMediatorAcaPyService {

  @PostMapping(value = "/out-of-band/create-invitation")
  CreateInvitationResponse createInvitation(@RequestBody CreateInvitationRequest request);
}
