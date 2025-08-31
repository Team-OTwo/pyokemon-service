package com.pyokemon.did.api.open;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.dto.request.DelegateCredentialRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
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

  @PostMapping("/delegate-credential")
  public ResponseEntity<ResponseDto<Void>> delegateCredential(
      @RequestBody @Valid DelegateCredentialRequest delegateCredentialRequest) {
    Long userId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    String deviceId = GatewayRequestHeaderUtils.getUserDeviceOrThrowException();

    verificationService.delegateCredential(delegateCredentialRequest.getBookingId(), userId,
        deviceId);
    return ResponseEntity.ok(ResponseDto.success("자격 증명 위임 성공"));
  }


  @PostMapping
  public ResponseEntity<CreateVerificationResponse> issueVerificationUrl(
      CreateVerificationRequest request) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    CreateVerificationResponse response =
        verificationService.createVerificationUrl(request, tenantId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{pres_ex_id}")
  public ResponseEntity<HandleVerificationResponse> handleVerification(
      @PathVariable(name = "pres_ex_id") String presExId) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    HandleVerificationResponse response =
        verificationService.handleVerification(tenantId, presExId);
    return ResponseEntity.ok(response);
  }

}
