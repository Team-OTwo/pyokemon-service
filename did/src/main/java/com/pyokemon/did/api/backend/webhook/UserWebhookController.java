package com.pyokemon.did.api.backend.webhook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.did.domain.dto.request.webhook.*;
import com.pyokemon.did.service.UserWebhookService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/backend/webhook/user/topic")
public class UserWebhookController {

  private final UserWebhookService userWebhookService;

  @PostMapping({"/connections/"})
  public ResponseEntity<Void> handleConnectionWebhook(
      @RequestBody ConnectionWebhookRequest webhookDto) {
    userWebhookService.handleConnectionWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/out_of_band/"})
  public ResponseEntity<Void> handleOutOfBandWebhook(
      @RequestBody OutOfBandWebhookRequest webhookDto) {
    userWebhookService.handleOutOfBandWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/basicmessages/"})
  public ResponseEntity<Void> handleBasicMessage(
      @RequestBody BasicMessageWebhookRequest webhookDto) {
    userWebhookService.handleBasicMessageWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/issue_credential_v2_0/"})
  public ResponseEntity<Void> handleIssueCredentialWebhook(
      @RequestBody IssueCredentialWebhookRequest webhookDto) {
    userWebhookService.handleIssueCredentialWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/issue_credential_v2_0_ld_proof/"})
  public ResponseEntity<Void> handleIssueCredentialLdProofWebhook(
      @RequestBody LdProofWebhookRequest webhookDto) {
    userWebhookService.handleLdProofWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }
}
