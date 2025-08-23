package com.pyokemon.did.api.backend;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/backend/webhook")
public class WebhookController {

    @PostMapping({"/user/topic/connections", "/user/topic/connections/"})
    public ResponseEntity<Void> handleConnectionWebhook(@RequestBody String payload) {
        log.info("=== User ACA-Py Connections Webhook ===");
        log.info("Payload: {}", payload);
        log.info("================================");
        // 연결 상태 변경 처리
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/user/topic/out_of_band", "/user/topic/out_of_band/"})
    public ResponseEntity<Void> handleOutOfBandWebhook(@RequestBody String payload) {
        log.info("=== User ACA-Py Out of Band Webhook ===");
        log.info("Payload: {}", payload);
        log.info("================================");
        // 초대장 관련 처리
        return ResponseEntity.ok().build();
    }
}
