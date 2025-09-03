package com.pyokemon.did.api.open;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.dto.request.DelegateCredentialRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;
import com.pyokemon.did.service.VerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/verifications")
@RestController
@AllArgsConstructor
@Tag(name = "VP 검증 관리", description = "Credential 검증 관련 API")
public class VerificationController {

  VerificationService verificationService;

  @Operation(summary = "자격 증명 (Verified Credential) 위임 API",
      description = "VC 위임 요청입니다. \n" + "200 OK 시 mediator ACA-Py로부터 위임된 VC를 polling합니다.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "위임 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 오류")})
  @PostMapping("/delegate-credential")
  public ResponseEntity<ResponseDto<Void>> delegateCredential(
      @Parameter(description = "자격 증명 위임 요청 정보") @RequestBody
      @Valid DelegateCredentialRequest delegateCredentialRequest) {
    Long userId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    String deviceId = GatewayRequestHeaderUtils.getUserDeviceOrThrowException();

    verificationService.delegateCredential(delegateCredentialRequest.getBookingId(), userId,
        deviceId);
    return ResponseEntity.ok(ResponseDto.success("자격 증명 위임 성공"));
  }


  @Operation(summary = "VP 검증을 위한 초대장 생성 API", description = "검증자의 QR에 표시될 내용을 요청합니다.")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "URL 생성 성공",
              content = @Content(
                  schema = @Schema(implementation = CreateVerificationResponse.class))),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "401", description = "인증 실패"),
          @ApiResponse(responseCode = "500", description = "서버 오류")})
  @PostMapping
  public ResponseEntity<ResponseDto<CreateVerificationResponse>> issueVerificationUrl(
      @Parameter(description = "검증 URL 생성 요청 정보") @RequestBody
      @Valid CreateVerificationRequest request) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    CreateVerificationResponse response =
        verificationService.createVerificationUrl(request, tenantId);
    return ResponseEntity.ok(ResponseDto.success(response, "검증 URL 생성 성공"));
  }

  @Operation(summary = "VP 검증 결과 응답 API",
      description = "검증자의 QR 코드가 스캔되면 검증 완료된 booking을 polling합니다.")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "검증 처리 완료",
              content = @Content(
                  schema = @Schema(implementation = HandleVerificationResponse.class))),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "401", description = "인증 실패"),
          @ApiResponse(responseCode = "404", description = "검증 정보 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")})
  @GetMapping("/{pres_ex_id}")
  public ResponseEntity<ResponseDto<HandleVerificationResponse>> handleVerification(
      @Parameter(description = "검증 ID") @PathVariable(name = "pres_ex_id") String presExId,
      @PathVariable(name="booking_id") Long bookingId) {
    Long tenantId = GatewayRequestHeaderUtils.getTenantIdOrThrowException();
    HandleVerificationResponse response =
        verificationService.handleVerification(tenantId, presExId, bookingId);
    return ResponseEntity.ok(ResponseDto.success(response, "검증 처리 완료"));
  }
}
