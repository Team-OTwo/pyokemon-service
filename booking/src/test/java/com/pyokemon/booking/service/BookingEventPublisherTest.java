package com.pyokemon.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        testBooking = Booking.builder()
                .bookingId(1L)
                .eventScheduleId(100L)
                .accountId(200L)
                .paymentId(300L)
                .status(Booking.Booked.BOOKED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void publishBookingStatusUpdate_Booked_ShouldPublishEvent() throws Exception {
        // Given
        String expectedMessage = "{\"booking_id\":1,\"event_schedule_id\":100,\"account_id\":200,\"payment_id\":300,\"status\":\"BOOKED\",\"message\":\"예매가 완료되었습니다\"}";
        when(objectMapper.writeValueAsString(any(BookingEventDto.class))).thenReturn(expectedMessage);

        // When
        bookingEventPublisher.publishBookingStatusUpdate(testBooking);

        // Then
        verify(kafkaTemplate).send(eq("booking-status-updated"), eq(expectedMessage));
    }

    @Test
    void publishBookingStatusUpdate_Canceled_ShouldPublishEvent() throws Exception {
        // Given
        testBooking.setStatus(Booking.Booked.CANCELED);
        String expectedMessage = "{\"booking_id\":1,\"event_schedule_id\":100,\"account_id\":200,\"payment_id\":300,\"status\":\"CANCELED\",\"message\":\"예매가 취소되었습니다\"}";
        when(objectMapper.writeValueAsString(any(BookingEventDto.class))).thenReturn(expectedMessage);

        // When
        bookingEventPublisher.publishBookingStatusUpdate(testBooking);

        // Then
        verify(kafkaTemplate).send(eq("booking-status-updated"), eq(expectedMessage));
    }

    @Test
    void publishBookingStatusUpdate_Failed_ShouldPublishEvent() throws Exception {
        // Given
        testBooking.setStatus(Booking.Booked.FAILED);
        String expectedMessage = "{\"booking_id\":1,\"event_schedule_id\":100,\"account_id\":200,\"payment_id\":300,\"status\":\"FAILED\",\"message\":\"예매에 실패했습니다\"}";
        when(objectMapper.writeValueAsString(any(BookingEventDto.class))).thenReturn(expectedMessage);

        // When
        bookingEventPublisher.publishBookingStatusUpdate(testBooking);

        // Then
        verify(kafkaTemplate).send(eq("booking-status-updated"), eq(expectedMessage));
    }

    @Test
    void publishBookingStatusUpdate_Pending_ShouldNotPublishEvent() throws Exception {
        // Given
        testBooking.setStatus(Booking.Booked.PENDING);

        // When
        bookingEventPublisher.publishBookingStatusUpdate(testBooking);

        // Then
        verify(kafkaTemplate, never()).send(any(), any());
    }
}
