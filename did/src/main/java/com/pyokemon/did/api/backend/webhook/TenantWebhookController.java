package com.pyokemon.did.api.backend.webhook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.did.domain.dto.request.webhook.*;
import com.pyokemon.did.service.TenantWebhookService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/backend/webhook/tenant/topic")
public class TenantWebhookController {
  private final TenantWebhookService tenantWebhookService;

  @PostMapping({"/connections/"})
  public ResponseEntity<Void> handleTenantConnectionWebhook(
      @RequestBody ConnectionWebhookRequest connectionWebhookRequest) {
    tenantWebhookService.handleTenantConnectionWebhook(connectionWebhookRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/out_of_band/"})
  public ResponseEntity<Void> handleTenantOutOfBandWebhook(
      @RequestBody OutOfBandWebhookRequest outOfBandWebhookRequest) {
    tenantWebhookService.handleTenantOOBWebhook(outOfBandWebhookRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/present_proof_v2_0/")
  public ResponseEntity<Void> handlePresentProofWebhook(
      @RequestBody PresentProofWebhookRequest presentProofWebhookRequest) {
    tenantWebhookService.handleTenantPresentProofWebhook(presentProofWebhookRequest);
    return ResponseEntity.ok().build();
  }
}
