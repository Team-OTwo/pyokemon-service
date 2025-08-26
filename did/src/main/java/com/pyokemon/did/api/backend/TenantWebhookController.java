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


}
