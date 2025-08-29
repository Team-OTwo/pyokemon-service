package com.pyokemon.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.pyokemon.event.dto.BookingStatusResponse;

@FeignClient(name = "booking-service", url = "${booking.service.url:http://localhost:8088}")
public interface BookingServiceClient {

  @GetMapping("/booking/api/bookings/{eventScheduleId}")
  BookingStatusResponse getBookingStatusByEventScheduleId(
      @PathVariable("eventScheduleId") Long eventScheduleId);
}
