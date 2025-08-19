package com.pyokemon.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.booking.dto.request.ValidBookingRequest;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
import com.pyokemon.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/backend")
public class ValidBookingController {

  private final BookingService bookingService;

  @PostMapping("/validbookings")
  public ResponseEntity<ValidBookingResponse> validateBookings(
      @RequestBody ValidBookingRequest request) {
    ValidBookingResponse response = bookingService.validateBookings(request);
    return ResponseEntity.ok(response);
  }
}
