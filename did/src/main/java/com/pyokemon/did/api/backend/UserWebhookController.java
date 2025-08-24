package com.pyokemon.did.api.backend;

import com.pyokemon.did.service.UserWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.did.api.backend.dto.ConnectionWebhookDto;
import com.pyokemon.did.api.backend.dto.OutOfBandWebhookDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/backend/webhook")
public class UserWebhookController {

  private final UserWebhookService userWebhookService;

  @PostMapping({"/user/topic/connections", "/user/topic/connections/"})
  public ResponseEntity<Void> handleConnectionWebhook(
      @RequestBody ConnectionWebhookDto webhookDto) {
    userWebhookService.handleConnectionWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/user/topic/out_of_band", "/user/topic/out_of_band/"})
  public ResponseEntity<Void> handleOutOfBandWebhook(@RequestBody OutOfBandWebhookDto webhookDto) {
    userWebhookService.handleOutOfBandWebhook(webhookDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping({"/user/topic/basicmessages", "/user/topic/basicmessages/"})
  public ResponseEntity<Void> handleBasicMessage(@RequestBody String payload) {
    log.info("=== User ACA-Py BasicMessages Webhook ===");
    log.info("Payload: {}", payload);

    return ResponseEntity.ok().build();
  }
}
