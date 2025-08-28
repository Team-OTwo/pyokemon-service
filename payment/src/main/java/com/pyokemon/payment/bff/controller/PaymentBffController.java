package com.pyokemon.payment.bff.controller;

import java.util.List;

import com.pyokemon.payment.bff.dto.TotalRevenueResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.payment.bff.dto.PaymentDto;
import com.pyokemon.payment.bff.service.PaymentBffService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentBffController {

  private final PaymentBffService paymentBffService;

  @GetMapping("/{paymentId}")
  public PaymentDto getPayment(@PathVariable Long paymentId) {
    return paymentBffService.getPayment(paymentId);
  }

  @PostMapping("/_batch")
  public List<PaymentDto> getPayments(@RequestBody IdsRequest request) {
    return paymentBffService.getPayments(request.getIds());
  }

  @GetMapping("/summary/revenue")
  public ResponseEntity<TotalRevenueResponseDto> getTotalRevenue(
          @RequestParam("scheduleIds") List<Long> scheduleIds) {

    TotalRevenueResponseDto responseDto = paymentBffService.getTotalRevenue(scheduleIds);
    return ResponseEntity.ok(responseDto);
  }

}
