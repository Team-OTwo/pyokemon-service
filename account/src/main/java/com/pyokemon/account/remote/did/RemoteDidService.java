package com.pyokemon.account.remote.did;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.common.dto.ResponseDto;

@FeignClient(name = "remote-did-service", url = "${services.did.base-url}",
    configuration = com.pyokemon.account.remote.config.FeignConfig.class)
public interface RemoteDidService {
  @PostMapping("/backend/wallets")
  ResponseEntity<ResponseDto<Void>> registerWallet(
      @RequestBody @Valid RegisterWalletRequest registerWalletRequest);

}
