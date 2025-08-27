package com.pyokemon.did.api.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.did.api.backend.dto.IssueCredentialWebhookDto;
import com.pyokemon.did.api.backend.dto.LdProofWebhookDto;
import com.pyokemon.did.domain.dto.request.TenantWebhookRequest.HandleTenantConnectionsRequest;
import com.pyokemon.did.domain.dto.request.TenantWebhookRequest.HandleTenantOOBRequest;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/backend/webhook/tenant/topic")
public class TenantWebhookController {
  private final TenantWebhookService tenantWebhookService;

  @PostMapping({"/connections", "/connections/"})
  public ResponseEntity<Void> handleTenantConnectionWebhook(
      @RequestBody HandleTenantConnectionsRequest handleTenantConnectionsRequest) {
    tenantWebhookService.handleTenantConnectionWebhook(handleTenantConnectionsRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/out_of_band", "/out_of_band/"})
  public ResponseEntity<Void> handleTenantOutOfBandWebhook(
      @RequestBody HandleTenantOOBRequest handleTenantOOBRequest) {
    tenantWebhookService.handleTenantOOBWebhook(handleTenantOOBRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/issue_credential_v2_0/"})
  public ResponseEntity<Void> handleIssueCredentialWebhook(
      @RequestBody IssueCredentialWebhookDto webhookDto) {
    log.info(webhookDto.toString());
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/issue_credential_v2_0_ld_proof/"})
  public ResponseEntity<Void> handleIssueCredentialLdProofWebhook(
      @RequestBody LdProofWebhookDto webhookDto) {
    log.info(webhookDto.toString());
    return ResponseEntity.ok().build();
  }
}
