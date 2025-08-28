package com.pyokemon.did.api.open;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.dto.request.VerificationRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.service.VerificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/verifications")
@RestController
@AllArgsConstructor
public class VerificationController {

  VerificationService verificationService;

  @PostMapping
  public ResponseEntity<CreateVerificationResponse> CreateVerificationUrl(
      CreateVerificationRequest request) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    CreateVerificationResponse response =
        verificationService.createVerificationUrl(request, tenantId);
    return ResponseEntity.ok(response);
  }

}
