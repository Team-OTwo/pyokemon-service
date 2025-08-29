package com.pyokemon.payment.controller;

import java.util.List;
import java.util.Map;

import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.payment.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.PaymentErrorCodes;
import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.repository.PaymentRepository;
import com.pyokemon.payment.service.PaymentService;
import com.pyokemon.payment.service.TossPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  final TossPaymentService tossPaymentService;
  final PaymentService paymentService;
  private final PaymentRepository paymentRepository;

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
    Payment status = paymentRepository.selectByOrderIdStatus(request.getOrderId());
    if (status == null || "CANCELED".equals(status.getStatus().name())
        || "FAILED".equals(status.getStatus().name())
        || "EXPIRED".equals(status.getStatus().name())) {
      throw new BusinessException("결제 정보를 찾을 수 없습니다.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }

    if ("READY".equals(status.getStatus().name()) && request.getPaymentKey() != null) {
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
