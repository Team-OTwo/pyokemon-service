package com.pyokemon.booking.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pyokemon.booking.dto.request.ValidBookingRequest;
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
import com.pyokemon.booking.service.BookingService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidBookingController 단위 테스트")
class ValidBookingControllerTest {

  @Mock
  private BookingService bookingService;

  @InjectMocks
  private ValidBookingController validBookingController;

  private ValidBookingRequest testValidBookingRequest;

  @BeforeEach
  void setUp() {
    testValidBookingRequest =
        ValidBookingRequest.builder().userId(300L).bookings(Arrays.asList(1L, 2L, 3L)).build();
  }

  @Test
  @DisplayName("예약 유효성 검사 - 성공")
  void validateBookings_Success() {
    // Given
    List<ValidBookingDetail> validBookings = Arrays.asList(new ValidBookingDetail(1L, 100L, 400L),
        new ValidBookingDetail(2L, 101L, 400L), new ValidBookingDetail(3L, 102L, 400L));
    ValidBookingResponse expectedResponse =
        ValidBookingResponse.builder().bookings(validBookings).build();
    when(bookingService.validateBookings(testValidBookingRequest)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ValidBookingResponse> response =
        validBookingController.validateBookings(testValidBookingRequest);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(3, response.getBody().getBookings().size());
    assertEquals(1L, response.getBody().getBookings().get(0).getBookingId());
    assertEquals(100L, response.getBody().getBookings().get(0).getEventScheduleId());
    assertEquals(400L, response.getBody().getBookings().get(0).getTenantId());
    assertEquals(2L, response.getBody().getBookings().get(1).getBookingId());
    assertEquals(101L, response.getBody().getBookings().get(1).getEventScheduleId());
    assertEquals(400L, response.getBody().getBookings().get(1).getTenantId());
    assertEquals(3L, response.getBody().getBookings().get(2).getBookingId());
    assertEquals(102L, response.getBody().getBookings().get(2).getEventScheduleId());
    assertEquals(400L, response.getBody().getBookings().get(2).getTenantId());

    verify(bookingService).validateBookings(testValidBookingRequest);
  }

  @Test
  @DisplayName("예약 유효성 검사 - 빈 예약 목록")
  void validateBookings_EmptyBookings_Success() {
    // Given
    ValidBookingRequest requestWithEmptyBookings =
        ValidBookingRequest.builder().userId(300L).bookings(Collections.emptyList()).build();
    ValidBookingResponse expectedResponse =
        ValidBookingResponse.builder().bookings(Collections.emptyList()).build();
    when(bookingService.validateBookings(requestWithEmptyBookings)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ValidBookingResponse> response =
        validBookingController.validateBookings(requestWithEmptyBookings);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getBookings().size());

    verify(bookingService).validateBookings(requestWithEmptyBookings);
  }

  @Test
  @DisplayName("예약 유효성 검사 - 단일 예약 ID")
  void validateBookings_SingleBookingId_Success() {
    // Given
    ValidBookingRequest singleBookingRequest =
        ValidBookingRequest.builder().userId(300L).bookings(Arrays.asList(1L)).build();
    List<ValidBookingDetail> validBookings = Arrays.asList(new ValidBookingDetail(1L, 100L, 400L));
    ValidBookingResponse expectedResponse =
        ValidBookingResponse.builder().bookings(validBookings).build();
    when(bookingService.validateBookings(singleBookingRequest)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ValidBookingResponse> response =
        validBookingController.validateBookings(singleBookingRequest);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getBookings().size());
    assertEquals(1L, response.getBody().getBookings().get(0).getBookingId());
    assertEquals(100L, response.getBody().getBookings().get(0).getEventScheduleId());
    assertEquals(400L, response.getBody().getBookings().get(0).getTenantId());

    verify(bookingService).validateBookings(singleBookingRequest);
  }

  @Test
  @DisplayName("예약 유효성 검사 - 대량 예약 ID")
  void validateBookings_MultipleBookingIds_Success() {
    // Given
    List<Long> multipleBookingIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
    ValidBookingRequest multipleBookingRequest =
        ValidBookingRequest.builder().userId(300L).bookings(multipleBookingIds).build();
    List<ValidBookingDetail> validBookings = Arrays.asList(new ValidBookingDetail(1L, 100L, 400L),
        new ValidBookingDetail(2L, 101L, 400L), new ValidBookingDetail(3L, 102L, 400L),
        new ValidBookingDetail(4L, 103L, 400L), new ValidBookingDetail(5L, 104L, 400L));
    ValidBookingResponse expectedResponse =
        ValidBookingResponse.builder().bookings(validBookings).build();
    when(bookingService.validateBookings(multipleBookingRequest)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ValidBookingResponse> response =
        validBookingController.validateBookings(multipleBookingRequest);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(5, response.getBody().getBookings().size());

    verify(bookingService).validateBookings(multipleBookingRequest);
  }

  @Test
  @DisplayName("예약 유효성 검사 - null 사용자 ID")
  void validateBookings_NullUserId_Success() {
    // Given
    ValidBookingRequest requestWithNullUserId =
        ValidBookingRequest.builder().userId(null).bookings(Arrays.asList(1L, 2L)).build();
    ValidBookingResponse expectedResponse =
        ValidBookingResponse.builder().bookings(Collections.emptyList()).build();
    when(bookingService.validateBookings(requestWithNullUserId)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ValidBookingResponse> response =
        validBookingController.validateBookings(requestWithNullUserId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getBookings().size());

    verify(bookingService).validateBookings(requestWithNullUserId);
  }

  @Test
  @DisplayName("예약 유효성 검사 - null 예약 목록")
  void validateBookings_NullBookings_Success() {
    // Given
    ValidBookingRequest requestWithNullBookings =
        ValidBookingRequest.builder().userId(300L).bookings(null).build();
    ValidBookingResponse expectedResponse =
        ValidBookingResponse.builder().bookings(Collections.emptyList()).build();
    when(bookingService.validateBookings(requestWithNullBookings)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ValidBookingResponse> response =
        validBookingController.validateBookings(requestWithNullBookings);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getBookings().size());

    verify(bookingService).validateBookings(requestWithNullBookings);
  }
}
