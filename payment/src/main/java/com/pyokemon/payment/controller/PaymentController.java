package com.pyokemon.payment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;

  // 결제 임시저장
  @PostMapping("/initiate/{eventScheduleId}")
  public ResponseEntity<Map<String, Object>> reserve(@PathVariable Long eventScheduleId,
      @RequestBody PaymentInitiateRequestDto request) {
    paymentService.reserve(request, eventScheduleId);
    return ResponseEntity
        .ok(Map.of("orderId", request.getOrderId(), "amount", request.getAmount()));
  }

  // 결제 confirm, 성공/실패 처리 (Done/Failed)
  @PostMapping("/confirm-save")
  public ResponseEntity<PaymentConfirmResponseDto> confirm(
      @RequestBody PaymentConfirmRequestDto request) {
    PaymentConfirmResponseDto response = paymentService.processPaymentConfirm(request);
    return ResponseEntity.ok(response);
  }

}
