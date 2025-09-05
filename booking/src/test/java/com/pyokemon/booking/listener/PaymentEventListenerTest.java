package com.pyokemon.booking.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.booking.dto.kafka.PaymentKafkaDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventListener 단위 테스트")
class PaymentEventListenerTest {

  @Mock
  private BookingService bookingService;

  @InjectMocks
  private PaymentEventListener paymentEventListener;

  private PaymentKafkaDto testPaymentEvent;

  @BeforeEach
  void setUp() {
    testPaymentEvent = new PaymentKafkaDto();
    testPaymentEvent.setBookingId(1L);
    testPaymentEvent.setPaymentId(500L);
    testPaymentEvent.setStatus("DONE");
  }

  @Test
  @DisplayName("결제 상태 업데이트 이벤트 처리 - DONE 상태")
  void handlePaymentStatusUpdate_DoneStatus_Success() {
    // When
    paymentEventListener.handlePaymentStatusUpdate(testPaymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.BOOKED), eq(500L));
  }

  @Test
  @DisplayName("결제 상태 업데이트 이벤트 처리 - CANCELED 상태")
  void handlePaymentStatusUpdate_CanceledStatus_Success() {
    // Given
    PaymentKafkaDto canceledEvent = new PaymentKafkaDto();
    canceledEvent.setBookingId(1L);
    canceledEvent.setPaymentId(500L);
    canceledEvent.setStatus("CANCELED");

    // When
    paymentEventListener.handlePaymentStatusUpdate(canceledEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.CANCELED), eq(500L));
  }

  @Test
  @DisplayName("결제 상태 업데이트 이벤트 처리 - FAILED 상태")
  void handlePaymentStatusUpdate_FailedStatus_Success() {
    // Given
    PaymentKafkaDto failedEvent = new PaymentKafkaDto();
    failedEvent.setBookingId(1L);
    failedEvent.setPaymentId(500L);
    failedEvent.setStatus("FAILED");

    // When
    paymentEventListener.handlePaymentStatusUpdate(failedEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.FAILED), eq(500L));
  }

  @Test
  @DisplayName("결제 상태 업데이트 이벤트 처리 - 알 수 없는 상태")
  void handlePaymentStatusUpdate_UnknownStatus_DefaultsToFailed() {
    // Given
    PaymentKafkaDto unknownEvent = new PaymentKafkaDto();
    unknownEvent.setBookingId(1L);
    unknownEvent.setPaymentId(500L);
    unknownEvent.setStatus("UNKNOWN");

    // When
    paymentEventListener.handlePaymentStatusUpdate(unknownEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.FAILED), eq(500L));
  }

  @Test
  @DisplayName("결제 상태 업데이트 이벤트 처리 - 소문자 상태")
  void handlePaymentStatusUpdate_LowercaseStatus_Success() {
    // Given
    PaymentKafkaDto lowercaseEvent = new PaymentKafkaDto();
    lowercaseEvent.setBookingId(1L);
    lowercaseEvent.setPaymentId(500L);
    lowercaseEvent.setStatus("done");

    // When
    paymentEventListener.handlePaymentStatusUpdate(lowercaseEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.BOOKED), eq(500L));
  }

  @Test
  @DisplayName("결제 상태 업데이트 이벤트 처리 - 예외 발생 시 로그 기록")
  void handlePaymentStatusUpdate_Exception_LogsError() {
    // Given
    doThrow(new RuntimeException("Service error")).when(bookingService)
        .updateBookingStatus(anyLong(), any(), anyLong());

    // When
    paymentEventListener.handlePaymentStatusUpdate(testPaymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.BOOKED), eq(500L));
    // 예외가 발생해도 메서드가 정상적으로 완료되어야 함
  }

  @Test
  @DisplayName("결제 이벤트 처리 - DONE 상태")
  void processPaymentEvent_DoneStatus_Success() {
    // When
    paymentEventListener.processPaymentEvent(testPaymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.BOOKED), eq(500L));
  }

  @Test
  @DisplayName("결제 이벤트 처리 - CANCELED 상태")
  void processPaymentEvent_CanceledStatus_Success() {
    // Given
    PaymentKafkaDto canceledEvent = new PaymentKafkaDto();
    canceledEvent.setBookingId(2L);
    canceledEvent.setPaymentId(501L);
    canceledEvent.setStatus("CANCELED");

    // When
    paymentEventListener.processPaymentEvent(canceledEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(2L), eq(Booking.Booked.CANCELED), eq(501L));
  }

  @Test
  @DisplayName("결제 이벤트 처리 - FAILED 상태")
  void processPaymentEvent_FailedStatus_Success() {
    // Given
    PaymentKafkaDto failedEvent = new PaymentKafkaDto();
    failedEvent.setBookingId(3L);
    failedEvent.setPaymentId(502L);
    failedEvent.setStatus("FAILED");

    // When
    paymentEventListener.processPaymentEvent(failedEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(3L), eq(Booking.Booked.FAILED), eq(502L));
  }

  @Test
  @DisplayName("결제 이벤트 처리 - 알 수 없는 상태")
  void processPaymentEvent_UnknownStatus_DefaultsToFailed() {
    // Given
    PaymentKafkaDto unknownEvent = new PaymentKafkaDto();
    unknownEvent.setBookingId(4L);
    unknownEvent.setPaymentId(503L);
    unknownEvent.setStatus("UNKNOWN_STATUS");

    // When
    paymentEventListener.processPaymentEvent(unknownEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(4L), eq(Booking.Booked.FAILED), eq(503L));
  }
}
