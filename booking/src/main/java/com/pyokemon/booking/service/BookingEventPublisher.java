package com.pyokemon.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "booking-status-updated";

    public void publishBookingStatusUpdate(Booking booking) {
        try {
            BookingEventDto event = BookingEventDto.builder()
                    .bookingId(booking.getBookingId())
                    .eventScheduleId(booking.getEventScheduleId())
                    .accountId(booking.getAccountId())
                    .status(booking.getStatus().name())
                    .build();

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);

            log.info("Published booking status update event: {}", message);
        } catch (Exception e) {
            log.error("Failed to publish booking status update event for booking: {}", booking.getBookingId(), e);
        }
    }
}
