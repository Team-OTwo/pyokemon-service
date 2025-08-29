package com.pyokemon.payment.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.payment.dto.PaymentInfoDto;
import com.pyokemon.payment.dto.TotalRevenueResponseDto;
import com.pyokemon.payment.entity.Payment;
import org.springframework.stereotype.Service;

import com.pyokemon.payment.dto.PaymentDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
  final PaymentRepository paymentRepository;

  public void reserve(PaymentInitiateRequestDto request) {
    PaymentDto dto = PaymentDto.builder().bookingId(request.getBookingId())
        .orderId(request.getOrderId()).amount(request.getAmount()).method(request.getMethod())
        .status("READY").accountId(request.getAccountId()).build();

    paymentRepository.insertInitiatePayment(dto);
  }

  @Transactional(readOnly = true)
  public PaymentInfoDto getPayment(Long paymentId) {
    Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);

    if (paymentOpt.isEmpty()) {
      throw new BusinessException("결제 정보를 조회할 수 없습니다.", "PAYMENT_NOT_FOUND");
    }

    Payment payment = paymentOpt.get();

    return PaymentInfoDto.builder().paymentId(paymentId).amount(payment.getAmount())
            .method(payment.getMethod()).status(payment.getStatus()).updatedAt(payment.getUpdatedAt())
            .build();
  }

  @Transactional(readOnly = true)
  public List<PaymentInfoDto> getPayments(List<Long> paymentIds) {
    if (paymentIds == null || paymentIds.isEmpty()) {
      return List.of();
    }

    // DB 한 번만 호출 (IN 절)
    List<Payment> rows = paymentRepository.findByPaymentIds(paymentIds);

    // 결과를 Map으로 정리
    Map<Long, Payment> byId =
            rows.stream().collect(Collectors.toMap(Payment::getPaymentId, Function.identity()));

    // 누락 체크 → 기존 단건 정책 유지
    List<Long> missing =
            paymentIds.stream().filter(id -> !byId.containsKey(id)).distinct().toList();

    if (!missing.isEmpty()) {
      throw new BusinessException("결제 정보를 조회할 수 없습니다. ids=" + missing, "PAYMENT_NOT_FOUND");
    }

    // 요청 순서 / 중복 그대로 복원
    return paymentIds.stream().map(id -> toDto(id, byId.get(id))).toList();
  }

  private static PaymentInfoDto toDto(Long paymentId, Payment payment) {
    return PaymentInfoDto.builder().paymentId(paymentId).amount(payment.getAmount())
            .method(payment.getMethod()).status(payment.getStatus()).updatedAt(payment.getUpdatedAt())
            .build();
  }

  @Transactional(readOnly = true)
  public TotalRevenueResponseDto getTotalRevenue(List<Long> scheduleIds) {
    Long totalRevenue = paymentRepository.sumTotalRevenueByScheduleIds(scheduleIds);
    return new TotalRevenueResponseDto(totalRevenue);
  }
}
