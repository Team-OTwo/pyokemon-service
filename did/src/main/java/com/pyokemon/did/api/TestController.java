package com.pyokemon.did.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.domain.dto.request.WalletRequest.RegisterWalletRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.request.webhook.*;
import com.pyokemon.did.service.WalletService;
import com.pyokemon.did.service.VerificationService;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.UserWebhookService;
import com.pyokemon.did.service.TenantWebhookService;
import com.pyokemon.did.domain.Wallet.AccountRole;
import com.pyokemon.did.domain.dto.request.VerificationRequest.HandleVerificationRequest;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

  private final WalletService walletService;
  private final VerificationService verificationService;
  private final DeviceConnectionService deviceConnectionService;
  private final IssuedVcService issuedVcService;
  private final AcaPyConnectionService acaPyConnectionService;
  private final UserWebhookService userWebhookService;
  private final TenantWebhookService tenantWebhookService;

  // ==================== 월렛 생성 테스트 ====================
  
  /**
   * 테스트용 테넌트 월렛 생성
   */
  @PostMapping("/wallet/tenant")
  public ResponseEntity<ResponseDto<String>> createTenantWallet() {
    try {
      RegisterWalletRequest request = new RegisterWalletRequest(1L, AccountRole.TENANT);
      walletService.registerWallet(request);
      return ResponseEntity.ok(ResponseDto.success("테넌트 월렛 생성 성공"));
    } catch (Exception e) {
      log.error("테넌트 월렛 생성 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto.error("테넌트 월렛 생성 실패: " + e.getMessage(), "WALLET_CREATION_FAILED"));
    }
  }

  /**
   * 테스트용 사용자 월렛 생성
   */
  @PostMapping("/wallet/user")
  public ResponseEntity<ResponseDto<String>> createUserWallet() {
    try {
      RegisterWalletRequest request = new RegisterWalletRequest(1L, AccountRole.USER);
      walletService.registerWallet(request);
      return ResponseEntity.ok(ResponseDto.success("사용자 월렛 생성 성공"));
    } catch (Exception e) {
      log.error("사용자 월렛 생성 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto.error("사용자 월렛 생성 실패: " + e.getMessage(), "WALLET_CREATION_FAILED"));
    }
  }

  // ==================== 초대장 생성 테스트 ====================
  
  /**
   * 초대장 생성 테스트 (사용자 디바이스 연결용)
   */
  @PostMapping("/invitations")
  public ResponseEntity<ResponseDto<String>> testCreateInvitations() {
    try {
      // 실제로는 Gateway 헤더에서 추출하지만, 테스트용으로 하드코딩
      Long userId = 1L;
      String deviceId = "test-device-001";
      
      var response = deviceConnectionService.createInvitations(userId);
      log.info("초대장 생성 성공: {}", response);
      
      return ResponseEntity.ok(ResponseDto.success("초대장 생성 성공: " + response.toString()));
    } catch (Exception e) {
      log.error("초대장 생성 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto.error("초대장 생성 실패: " + e.getMessage(), "INVITATION_CREATION_FAILED"));
    }
  }

  // ==================== 검증 URL 생성 테스트 ====================
  
  /**
   * 검증 URL 생성 테스트
   */
  @PostMapping("/verifications")
  public ResponseEntity<ResponseDto<String>> testCreateVerificationUrl() {
    try {
      CreateVerificationRequest request = new CreateVerificationRequest();
      request.setBookingId(1L);
      request.setJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.jwt");
      
      // Gateway 헤더에서 계정 정보 추출
      Long accountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
      var response = verificationService.createVerificationUrl(request, accountId);
      log.info("검증 URL 생성 성공: {}", response);
      
      return ResponseEntity.ok(ResponseDto.success("검증 URL 생성 성공: " + response.toString()));
    } catch (Exception e) {
      log.error("검증 URL 생성 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto.error("검증 URL 생성 실패: " + e.getMessage(), "VERIFICATION_CREATION_FAILED"));
    }
  }

  // ==================== Webhook 시뮬레이션 테스트 ====================
  
  /**
   * 실제 Credo ACA-Py에서 보내는 Connection Webhook을 받습니다.
   * 이 엔드포인트는 Credo ACA-Py가 직접 호출합니다.
   */
  @PostMapping("/webhook/connection")
  public ResponseEntity<String> receiveConnectionWebhook(@RequestBody ConnectionWebhookRequest webhook) {
    try {
      log.info("실제 Connection Webhook 수신: {}", webhook);
      userWebhookService.handleConnectionWebhook(webhook);
      return ResponseEntity.ok("Connection Webhook 처리 완료");
    } catch (Exception e) {
      log.error("Connection Webhook 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body("Connection Webhook 처리 실패: " + e.getMessage());
    }
  }

  /**
   * 실제 Credo ACA-Py에서 보내는 Issue Credential Webhook을 받습니다.
   * 이 엔드포인트는 Credo ACA-Py가 직접 호출합니다.
   */
  @PostMapping("/webhook/issue-credential")
  public ResponseEntity<String> receiveIssueCredentialWebhook(@RequestBody IssueCredentialWebhookRequest webhook) {
    try {
      log.info("실제 Issue Credential Webhook 수신: {}", webhook);
      userWebhookService.handleIssueCredentialWebhook(webhook);
      return ResponseEntity.ok("Issue Credential Webhook 처리 완료");
    } catch (Exception e) {
      log.error("Issue Credential Webhook 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body("Issue Credential Webhook 처리 실패: " + e.getMessage());
    }
  }

  /**
   * 실제 Credo ACA-Py에서 보내는 Present Proof Webhook을 받습니다.
   * 이 엔드포인트는 Credo ACA-Py가 직접 호출합니다.
   */
  @PostMapping("/webhook/present-proof")
  public ResponseEntity<String> receivePresentProofWebhook(@RequestBody PresentProofWebhookRequest webhook) {
    try {
      log.info("실제 Present Proof Webhook 수신: {}", webhook);
      tenantWebhookService.handleTenantPresentProofWebhook(webhook);
      return ResponseEntity.ok("Present Proof Webhook 처리 완료");
    } catch (Exception e) {
      log.error("Present Proof Webhook 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body("Present Proof Webhook 처리 실패: " + e.getMessage());
    }
  }

  /**
   * 실제 Credo ACA-Py에서 보내는 Basic Message Webhook을 받습니다.
   * 이 엔드포인트는 Credo ACA-Py가 직접 호출합니다.
   */
  @PostMapping("/webhook/basic-message")
  public ResponseEntity<String> receiveBasicMessageWebhook(@RequestBody BasicMessageWebhookRequest webhook) {
    try {
      log.info("실제 Basic Message Webhook 수신: {}", webhook);
      userWebhookService.handleBasicMessageWebhook(webhook);
      return ResponseEntity.ok("Basic Message Webhook 처리 완료");
    } catch (Exception e) {
      log.error("Basic Message Webhook 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body("Basic Message Webhook 처리 실패: " + e.getMessage());
    }
  }

  /**
   * 실제 Credo ACA-Py에서 보내는 Out of Band Webhook을 받습니다.
   * 이 엔드포인트는 Credo ACA-Py가 직접 호출합니다.
   */
  @PostMapping("/webhook/out-of-band")
  public ResponseEntity<String> receiveOutOfBandWebhook(@RequestBody OutOfBandWebhookRequest webhook) {
    try {
      log.info("실제 Out of Band Webhook 수신: {}", webhook);
      userWebhookService.handleOutOfBandWebhook(webhook);
      return ResponseEntity.ok("Out of Band Webhook 처리 완료");
    } catch (Exception e) {
      log.error("Out of Band Webhook 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body("Out of Band Webhook 처리 실패: " + e.getMessage());
    }
  }

  /**
   * 실제 Credo ACA-Py에서 보내는 LD Proof Webhook을 받습니다.
   * 이 엔드포인트는 Credo ACA-Py가 직접 호출합니다.
   */
  @PostMapping("/webhook/ld-proof")
  public ResponseEntity<String> receiveLdProofWebhook(@RequestBody LdProofWebhookRequest webhook) {
    try {
      log.info("실제 LD Proof Webhook 수신: {}", webhook);
      userWebhookService.handleLdProofWebhook(webhook);
      return ResponseEntity.ok("LD Proof Webhook 처리 완료");
    } catch (Exception e) {
      log.error("LD Proof Webhook 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body("LD Proof Webhook 처리 실패: " + e.getMessage());
    }
  }

  // ==================== 검증 처리 테스트 ====================
  
  /**
   * 검증 처리 테스트
   */
  @PostMapping("/verifications/{pres_ex_id}")
  public ResponseEntity<ResponseDto<String>> testHandleVerification(@PathVariable String pres_ex_id) {
    try {
      // Gateway 헤더에서 계정 정보 추출
      Long accountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
      
      // HandleVerificationRequest 생성 (테스트용)
      HandleVerificationRequest request = new HandleVerificationRequest();
      // 필요한 경우 request에 데이터 설정
      
      var response = verificationService.handleVerification(accountId, pres_ex_id, request);
      log.info("검증 처리 성공: {}", response);
      
      return ResponseEntity.ok(ResponseDto.success("검증 처리 성공: " + response.toString()));
    } catch (Exception e) {
      log.error("검증 처리 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto.error("검증 처리 실패: " + e.getMessage(), "VERIFICATION_HANDLING_FAILED"));
    }
  }

  // ==================== 상태 조회 테스트 ====================
  
  /**
   * 테스트 상태 조회
   */
  @GetMapping("/status")
  public ResponseEntity<ResponseDto<Map<String, Object>>> getTestStatus() {
    try {
      Map<String, Object> status = Map.of(
          "service", "DID Service Test Controller",
          "timestamp", System.currentTimeMillis(),
          "availableTests", Map.of(
              "wallet", "월렛 생성 테스트",
              "invitations", "초대장 생성 테스트",
              "verifications", "검증 URL 생성 테스트",
              "webhooks", "Webhook 시뮬레이션 테스트"
          )
      );
      
      return ResponseEntity.ok(ResponseDto.success(status, "테스트 컨트롤러 상태 조회 성공"));
    } catch (Exception e) {
      log.error("상태 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ResponseDto.error("상태 조회 실패: " + e.getMessage(), "STATUS_QUERY_FAILED"));
    }
  }
}
