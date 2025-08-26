package com.pyokemon.did.api.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.did.api.backend.dto.IssueCredentialWebhookDto;
import com.pyokemon.did.api.backend.dto.LdProofWebhookDto;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/backend/webhook/user")
@RequiredArgsConstructor
public class TenantWebhookController {

  private final TenantWebhookService tenantWebhookService;

  @PostMapping({"/topic/issue_credential_v2_0", "/topic/issue_credential_v2_0/"})
  public ResponseEntity<Void> handleIssueCredentialWebhook(
      @RequestBody IssueCredentialWebhookDto webhookDto) {
    log.info("일반 Webhook 수신: {}", webhookDto);

    try {
      tenantWebhookService.handleIssueCredentialWebhook(webhookDto);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("일반 Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping({"/topic/issue_credential_v2_0_ld_proof", "/topic/issue_credential_v2_0_ld_proof/"})
  public ResponseEntity<Void> handleIssueCredentialLdProofWebhook(
      @RequestBody LdProofWebhookDto webhookDto) {
    log.info("LD Proof Webhook 수신: {}", webhookDto);

    try {
      tenantWebhookService.handleLdProofWebhook(webhookDto);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("LD Proof Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
