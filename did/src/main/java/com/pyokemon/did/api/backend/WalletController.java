package com.pyokemon.did.api.backend;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest;
import com.pyokemon.did.domain.dto.request.TenantWalletRequest.CreateWalletRequest;
import com.pyokemon.did.service.TenantWalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/backend/wallets")
@RestController
@RequiredArgsConstructor
public class WalletController {
    private final TenantWalletService tenantWalletService;

    @PostMapping("/tenant")
    public ResponseEntity<ResponseDto<Void>> registerTenantWallet(@RequestBody @Valid CreateWalletRequest createWalletRequest) {
        tenantWalletService.registerTenantWallet(createWalletRequest);

        return ResponseEntity.ok(ResponseDto.success("테넌트 지갑 생성 성공"));
    }
}
