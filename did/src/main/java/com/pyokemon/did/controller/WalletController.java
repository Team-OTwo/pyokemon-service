package com.pyokemon.did.controller;

import com.pyokemon.did.dto.request.CreateWalletRequestDto;
import com.pyokemon.did.service.WalletService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/multitenancy/wallet")
@AllArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping()
    public ResponseEntity<String> createWallet(
     @RequestBody @Valid CreateWalletRequestDto requestDto
    ) {
        walletService.createWallet(requestDto);
        return ResponseEntity.ok().build();
    }
}
