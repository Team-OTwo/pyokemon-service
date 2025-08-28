package com.pyokemon.did.api.backend;

import com.pyokemon.did.remote.acapy.common.dto.request.CreateWalletRequest;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.common.dto.ResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

  @Autowired
  private RemoteTenantAcaPyService remoteTenantAcaPyService;

  @GetMapping
  public ResponseDto<String> health() {
    return ResponseDto.success("DID Service is running");
  }

  @GetMapping("/acapy")
  public ResponseDto<String> acapyHealth() {
    try {
      // AcaPy 서비스에 간단한 요청을 보내서 연결 상태 확인
      CreateWalletRequest request =
          CreateWalletRequest.builder().walletName("health-check-wallet")
              .walletKey("health-check-key").label("health-check").walletType("askar")
              .walletDispatchType("default").keyManagementMode("managed").build();

      remoteTenantAcaPyService.createWallet(request);
      return ResponseDto.success("AcaPy Tenant Service is connected");
    } catch (Exception e) {
      log.error("AcaPy 서비스 연결 실패: {}", e.getMessage(), e);
      return ResponseDto.error("AcaPy Tenant Service connection failed: " + e.getMessage(),
          "ACAPY_CONNECTION_ERROR");
    }
  }
}
