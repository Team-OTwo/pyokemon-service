package com.pyokemon.booking.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;

@ExtendWith(MockitoExtension.class)
class BookingEventPublisherTest {

  @Mock
  private KafkaTemplate<String, String> kafkaTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private BookingEventPublisher bookingEventPublisher;

  private Booking testBooking;

  @BeforeEach
  void setUp() {
    testBooking = Booking.builder().bookingId(1L).eventScheduleId(100L).accountId(200L)
        .paymentId(300L).status(Booking.Booked.BOOKED).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

  @Test
  void publishBookingStatusUpdate_Booked_ShouldPublishEvent() throws Exception {
    String expectedMessage =
        "{\"booking_id\":1,\"event_schedule_id\":100,\"account_id\":200,\"status\":\"BOOKED\"}";
    when(objectMapper.writeValueAsString(any(BookingEventDto.class))).thenReturn(expectedMessage);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate).send(eq("booking-status-updated"), eq(expectedMessage));
  }

  @Test
  void publishBookingStatusUpdate_Canceled_ShouldPublishEvent() throws Exception {
    testBooking.setStatus(Booking.Booked.CANCELED);
    String expectedMessage =
        "{\"booking_id\":1,\"event_schedule_id\":100,\"account_id\":200,\"status\":\"CANCELED\"}";
    when(objectMapper.writeValueAsString(any(BookingEventDto.class))).thenReturn(expectedMessage);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate).send(eq("booking-status-updated"), eq(expectedMessage));
  }

  @Test
  void publishBookingStatusUpdate_Failed_ShouldPublishEvent() throws Exception {
    testBooking.setStatus(Booking.Booked.FAILED);
    String expectedMessage =
        "{\"booking_id\":1,\"event_schedule_id\":100,\"account_id\":200,\"status\":\"FAILED\"}";
    when(objectMapper.writeValueAsString(any(BookingEventDto.class))).thenReturn(expectedMessage);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate).send(eq("booking-status-updated"), eq(expectedMessage));
  }

  @Test
  void publishBookingStatusUpdate_Pending_ShouldNotPublishEvent() throws Exception {
    testBooking.setStatus(Booking.Booked.PENDING);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate, never()).send(any(), any());
  }
}
