package com.pyokemon.payment.controller;

import java.util.List;
import java.util.Map;

import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.payment.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.payment.service.PaymentService;
import com.pyokemon.payment.service.TossPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  final TossPaymentService tossPaymentService;
  final PaymentService paymentService;

  // 결제 임시저장
  @PostMapping("/initiate")
  public ResponseEntity<Map<String, Object>> reserve(
      @RequestBody PaymentInitiateRequestDto request) {
    paymentService.reserve(request);
    return ResponseEntity
        .ok(Map.of("orderId", request.getOrderId(), "amount", request.getAmount()));
  }

  // 결제 confirm, 성공/실패 처리 (Done/Failed)
  @PostMapping("/confirm-save")
  public ResponseEntity<PaymentConfirmResponseDto> confirm(
      @RequestBody PaymentConfirmRequestDto request) {
    if (request.getPaymentKey() != null) {
      PaymentConfirmResponseDto response = tossPaymentService.confirm(request);
      return ResponseEntity.ok(response);
    } else {
      tossPaymentService.fail(request);
      return ResponseEntity.badRequest().build();

    }
  }

  @GetMapping("/{paymentId}")
  public PaymentInfoDto getPayment(@PathVariable Long paymentId) {
    return paymentService.getPayment(paymentId);
  }

  @PostMapping("/_batch")
  public List<PaymentInfoDto> getPayments(@RequestBody IdsRequest request) {
    return paymentService.getPayments(request.getIds());
  }

  @GetMapping("/summary/revenue")
  public ResponseEntity<TotalRevenueResponseDto> getTotalRevenue(
          @RequestParam("scheduleIds") List<Long> scheduleIds) {

    TotalRevenueResponseDto responseDto = paymentService.getTotalRevenue(scheduleIds);
    return ResponseEntity.ok(responseDto);
  }

}
