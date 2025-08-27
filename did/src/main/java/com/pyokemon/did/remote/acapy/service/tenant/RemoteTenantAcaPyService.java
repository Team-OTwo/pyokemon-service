package com.pyokemon.did.remote.acapy.service.tenant;

import com.pyokemon.did.remote.acapy.common.dto.request.*;
import com.pyokemon.did.remote.acapy.common.dto.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 테넌트 ACA-Py 서비스와의 원격 통신을 위한 인터페이스
 */
@FeignClient(name = "remote-acapy-tenant-service", url = "${acapy.tenant.base-url}",
    configuration = com.pyokemon.did.remote.config.FeignConfig.class)
public interface RemoteTenantAcaPyService {

    @PostMapping(value = "/multitenancy/wallet")
    CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request);

    @PostMapping(value = "/wallet/did/create")
    CreatePublicDidResponse createPublicDid(
        @RequestHeader("Authorization") String authorization,
        @RequestBody CreatePublicDidRequest request);

    @PostMapping(value = "/out-of-band/create-invitation?auto_accept=true&multi_use=false")
    CreateInvitationResponse createInvitation(
        @RequestHeader("Authorization") String authorization,
        @RequestBody CreateInvitationRequest request);

    @PostMapping(value = "/issue-credential-2.0/send")
    IssueCredentialResponse issueCredential(
        @RequestHeader("Authorization") String authorization,
        @RequestBody IssueCredentialRequest request);
        
    @PostMapping(value = "/present-proof-2.0/send-request")
    PresentProofResponse presentProof(
        @RequestHeader("Authorization") String authorization,
        @RequestBody PresentProofRequest request);
}