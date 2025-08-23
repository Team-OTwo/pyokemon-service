package com.pyokemon.did.api.backend;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest.CreateWalletRequest;
import com.pyokemon.did.domain.dto.request.UserWalletRequest.CreateUserWalletRequest;
import com.pyokemon.did.service.TenantWalletService;
import com.pyokemon.did.service.UserWalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/backend/wallets")
@RestController
@RequiredArgsConstructor
public class WalletController {
  private final TenantWalletService tenantWalletService;
  private final UserWalletService userWalletService;

  @PostMapping("/tenant")
  public ResponseEntity<ResponseDto<Void>> registerTenantWallet(
      @RequestBody @Valid CreateWalletRequest createWalletRequest) {
    tenantWalletService.registerTenantWallet(createWalletRequest);

    return ResponseEntity.ok(ResponseDto.success("테넌트 지갑 생성 성공"));
  }

  @PostMapping(value = "/user")
  public ResponseEntity<ResponseDto<Map<String, String>>> createUserWallet(
      @Valid @RequestBody CreateUserWalletRequest request) {
    log.info("사용자 지갑 생성 요청: userId={}", request.getUserId());

    userWalletService.createUserWallet(request.getUserId());
    log.info("사용자 지갑 생성 완료: userId={}", request.getUserId());
    return ResponseEntity.ok(
        ResponseDto.success(Map.of("userId", String.valueOf(request.getUserId())), "사용자 지갑 생성 완료"));
  }
}
