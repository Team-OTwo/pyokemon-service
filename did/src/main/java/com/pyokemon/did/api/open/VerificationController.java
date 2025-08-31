package com.pyokemon.did.api.open;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.HandleVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;
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
  public ResponseEntity<ResponseDto<CreateVerificationResponse>> issueVerificationUrl(
      CreateVerificationRequest request) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    CreateVerificationResponse response =
        verificationService.createVerificationUrl(request, tenantId);
    return ResponseEntity.ok(ResponseDto.success(response, "검증 URL 생성 성공"));
  }

  @PostMapping("/{pres_ex_id}")
  public ResponseEntity<ResponseDto<HandleVerificationResponse>> handleVerification(
      @PathVariable(name = "pres_ex_id") String presExId, HandleVerificationRequest request) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    HandleVerificationResponse response =
        verificationService.handleVerification(tenantId, presExId, request);
    return ResponseEntity.ok(ResponseDto.success(response, "검증 처리 완료"));
  }
}
