package com.pyokemon.did.remote.acapy.service;

import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.CreatePublicDidRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.ReceiveInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.CreatePublicDidResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateWalletResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.ReceiveInvitationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 사용자 ACA-Py 서비스와의 원격 통신을 위한 인터페이스
 */
@FeignClient(name = "remote-acapy-user-service", url = "${acapy.user.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class)
public interface RemoteUserAcaPyService {

    @PostMapping(value = "/multitenancy/wallet")
    CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request);

    @PostMapping(value = "/wallet/did/create")
    CreatePublicDidResponse createPublicDid(
        @RequestHeader("Authorization") String authorization,
        @RequestBody CreatePublicDidRequest request);

    @PostMapping(value = "/out-of-band/create-invitation")
    CreateInvitationResponse createInvitation(
        @RequestHeader("Authorization") String authorization,
        @RequestBody CreateInvitationRequest request);

    @PostMapping(value = "/out-of-band/receive-invitation")
    ReceiveInvitationResponse receiveInvitation(
        @RequestHeader("Authorization") String authorization,
        @RequestBody ReceiveInvitationRequest request);
}
