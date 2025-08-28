package com.pyokemon.did.api.backend;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.domain.dto.request.WalletRequest;
import com.pyokemon.did.domain.dto.request.WalletRequest.RegisterWalletRequest;
import com.pyokemon.did.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/backend/wallets")
@RestController
@RequiredArgsConstructor
public class WalletController {
  private final WalletService walletService;

  @PostMapping()
  public ResponseEntity<ResponseDto<Void>> registerWallet(
      @RequestBody @Valid RegisterWalletRequest registerWalletRequest) {
    walletService.registerWallet(registerWalletRequest);

    return ResponseEntity.ok(ResponseDto.success("계정 지갑 생성 성공"));
  }
}
