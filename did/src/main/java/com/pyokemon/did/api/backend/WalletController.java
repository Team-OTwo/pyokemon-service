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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/backend/wallets")
@RestController
@RequiredArgsConstructor
@Tag(name = "지갑 관리", description = "DID 지갑 관련 API")
public class WalletController {
  private final WalletService walletService;

  @Operation(summary = "ACA-Py 지갑 생성 API", description = "새로운 사용자 등록 시 새로운 ACA-Py 지갑을 생성합니다.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "지갑 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 오류")})
  @PostMapping()
  public ResponseEntity<ResponseDto<Void>> registerWallet(@Parameter(description = "지갑 등록 요청 정보")
  @RequestBody @Valid RegisterWalletRequest registerWalletRequest) {
    walletService.registerWallet(registerWalletRequest);

    return ResponseEntity.ok(ResponseDto.success("계정 지갑 생성 성공"));
  }
}
