package com.pyokemon.did.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.common.dto.ResponseDto;

@RestController
public class HealthController {

  @GetMapping("/health")
  public ResponseDto<String> health() {
    return ResponseDto.success("DID Service is running");
  }
}
