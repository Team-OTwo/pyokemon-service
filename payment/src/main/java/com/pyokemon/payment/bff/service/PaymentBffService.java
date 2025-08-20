package com.pyokemon.payment.bff.service;

// import org.springframework.security.crypto.password.PasswordEncoder;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.PaymentErrorCodes;
import com.pyokemon.payment.bff.dto.PaymentDto;
import com.pyokemon.payment.bff.repository.PaymentBffRepository;
import com.pyokemon.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentBffService {

  private final PaymentBffRepository paymentBffRepository;

  public PaymentDto getPayment(Long paymentId){
    Optional<Payment> paymentOpt = paymentBffRepository.findByPaymentId(paymentId);

    if(paymentOpt.isEmpty()){
      throw new BusinessException("결제 정보를 조회할 수 없습니다.", "PAYMENT_NOT_FOUND");
    }

    Payment payment = paymentOpt.get();

    return PaymentDto.builder()
            .paymentId(paymentId)
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .updatedAt(payment.getUpdatedAt())
            .build();
  }

  public List<PaymentDto> getPayments(List<Long> paymentIds) {
    if (paymentIds == null || paymentIds.isEmpty()) {
      return List.of();
    }

    // DB 한 번만 호출 (IN 절)
    List<Payment> rows = paymentBffRepository.findByPaymentIds(paymentIds);

    // 결과를 Map으로 정리
    Map<Long, Payment> byId = rows.stream()
            .collect(Collectors.toMap(Payment::getPaymentId, Function.identity()));

    // 누락 체크 → 기존 단건 정책 유지
    List<Long> missing = paymentIds.stream()
            .filter(id -> !byId.containsKey(id))
            .distinct()
            .toList();

    if (!missing.isEmpty()) {
      throw new BusinessException(
              "결제 정보를 조회할 수 없습니다. ids=" + missing,
              "PAYMENT_NOT_FOUND"
      );
    }

    // 요청 순서 / 중복 그대로 복원
    return paymentIds.stream()
            .map(id -> toDto(id, byId.get(id)))
            .toList();
  }

  private static PaymentDto toDto(Long paymentId, Payment payment) {
    return PaymentDto.builder()
            .paymentId(paymentId)
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .updatedAt(payment.getUpdatedAt())
            .build();
  }
}
