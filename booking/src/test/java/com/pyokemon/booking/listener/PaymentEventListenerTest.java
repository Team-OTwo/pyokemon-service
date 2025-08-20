package com.pyokemon.booking.listener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.booking.dto.kafka.PaymentKafkaDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

  @Mock
  private BookingService bookingService;

  @InjectMocks
  private PaymentEventListener paymentEventListener;

  @Test
  void handlePaymentStatusUpdate_Done_ShouldUpdateToBooked() {
    // Given
    PaymentKafkaDto paymentEvent = new PaymentKafkaDto(1L, 1L, "DONE");

    // When
    paymentEventListener.processPaymentEvent(paymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.BOOKED), eq(1L));
  }

  @Test
  void handlePaymentStatusUpdate_Canceled_ShouldUpdateToCanceled() {
    // Given
    PaymentKafkaDto paymentEvent = new PaymentKafkaDto(2L, 1L, "CANCELED");

    // When
    paymentEventListener.processPaymentEvent(paymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.CANCELED), eq(2L));
  }

  @Test
  void handlePaymentStatusUpdate_Failed_ShouldUpdateToFailed() {
    // Given
    PaymentKafkaDto paymentEvent = new PaymentKafkaDto(3L, 1L, "FAILED");

    // When
    paymentEventListener.processPaymentEvent(paymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.FAILED), eq(3L));
  }

  @Test
  void handlePaymentStatusUpdate_UnknownStatus_ShouldUpdateToFailed() {
    // Given
    PaymentKafkaDto paymentEvent = new PaymentKafkaDto(5L, 1L, "UNKNOWN");

    // When
    paymentEventListener.processPaymentEvent(paymentEvent);

    // Then
    verify(bookingService).updateBookingStatus(eq(1L), eq(Booking.Booked.FAILED), eq(5L));
  }
}
