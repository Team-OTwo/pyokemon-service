package com.pyokemon.did.remote.acapy.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.did.remote.acapy.common.dto.request.*;
import com.pyokemon.did.remote.acapy.common.dto.response.*;

/**
 * 사용자 ACA-Py 서비스와의 원격 통신을 위한 인터페이스
 */
@FeignClient(name = "remote-acapy-user-service", url = "${acapy.user.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class)
public interface RemoteUserAcaPyService {

  @PostMapping(value = "/multitenancy/wallet")
  CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request);

  @PostMapping(value = "/wallet/did/create")
  CreatePublicDidResponse createPublicDid(@RequestHeader("Authorization") String authorization,
      @RequestBody CreatePublicDidRequest request);

  @PostMapping(value = "/out-of-band/create-invitation")
  CreateInvitationResponse createInvitation(@RequestHeader("Authorization") String authorization,
      @RequestBody CreateInvitationRequest request);

  @PostMapping(value = "/out-of-band/receive-invitation")
  ReceiveInvitationResponse receiveInvitation(@RequestHeader("Authorization") String authorization,
      @RequestBody ReceiveInvitationRequest request);

  @GetMapping(value = "/vc/credentials/{credential_id}")
  GetCredentialResponse getCredential(@RequestHeader("Authorization") String authorization,
      @PathVariable(name = "credential_id") String credentialId);

  @PostMapping(value = "/issue-credential-2.0/send")
  IssueCredentialResponse issueCredential(@RequestHeader("Authorization") String authorization,
      @RequestBody IssueCredentialRequest request);
}
