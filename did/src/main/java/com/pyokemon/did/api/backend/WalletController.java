package com.pyokemon.did.api.backend;

import com.pyokemon.did.dto.request.CreateWalletRequestDto;
import com.pyokemon.did.service.WalletMetadataService;
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
@RequestMapping("/backend/wallet")
@AllArgsConstructor
public class WalletMetadataController {

    private final WalletMetadataService walletService;

    @PostMapping
    public ResponseEntity<String> createWallet(
     @RequestBody @Valid CreateWalletRequestDto requestDto
    ) {
        walletService.createWallet(requestDto);
        return ResponseEntity.ok().build();
    }
}
