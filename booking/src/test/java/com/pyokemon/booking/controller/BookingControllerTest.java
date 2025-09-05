package com.pyokemon.booking.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.dto.response.SeatStatusInfo;
import com.pyokemon.booking.service.BookingService;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingController 단위 테스트")
class BookingControllerTest {

  @Mock
  private BookingService bookingService;

  @InjectMocks
  private BookingController bookingController;

  private BookingRequest testBookingRequest;

  @BeforeEach
  void setUp() {
    testBookingRequest = new BookingRequest();
    testBookingRequest.setEventScheduleId(100L);
    testBookingRequest.setSeatId(200L);
    testBookingRequest.setTenantId(400L);
  }

  @Test
  @DisplayName("이벤트 스케줄 ID로 좌석 상태 조회 - 성공")
  void getSeatIdsByEventScheduleId_Success() {
    // Given
    Long eventScheduleId = 100L;
    List<SeatStatusInfo> seatStatusInfos =
        Arrays.asList(new SeatStatusInfo(200L, "PENDING"), new SeatStatusInfo(201L, "BOOKED"));
    EventScheduleIdResponse expectedResponse = new EventScheduleIdResponse(seatStatusInfos);
    when(bookingService.getSeatIdsByEventScheduleId(eventScheduleId)).thenReturn(expectedResponse);

    // When
    ResponseEntity<EventScheduleIdResponse> response =
        bookingController.getSeatIdsByEventScheduleId(eventScheduleId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getSeatStatusInfos().size());
    assertEquals(200L, response.getBody().getSeatStatusInfos().get(0).getSeatId());
    assertEquals("PENDING", response.getBody().getSeatStatusInfos().get(0).getStatus());
    assertEquals(201L, response.getBody().getSeatStatusInfos().get(1).getSeatId());
    assertEquals("BOOKED", response.getBody().getSeatStatusInfos().get(1).getStatus());

    verify(bookingService).getSeatIdsByEventScheduleId(eventScheduleId);
  }

  @Test
  @DisplayName("계정 ID로 예약 조회 - 성공")
  void getBookingsByAccountId_Success() {
    // Given
    Long accountId = 300L;
    List<com.pyokemon.booking.dto.response.BookingInfo> bookingInfos =
        Arrays.asList(new com.pyokemon.booking.dto.response.BookingInfo(100L, 500L,
            com.pyokemon.booking.entity.Booking.Booked.BOOKED, 200L, LocalDateTime.now()));
    AccountIdResponse expectedResponse = new AccountIdResponse(accountId, bookingInfos);
    when(bookingService.getBookingsByAccountId(accountId)).thenReturn(expectedResponse);

    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getAccountIdOrThrow).thenReturn(accountId);

      // When
      ResponseEntity<AccountIdResponse> response = bookingController.getBookingsByAccountId();

      // Then
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(accountId, response.getBody().getAccountId());
      assertEquals(1, response.getBody().getBookings().size());
      assertEquals(100L, response.getBody().getBookings().get(0).getEventScheduleId());
      assertEquals(com.pyokemon.booking.entity.Booking.Booked.BOOKED,
          response.getBody().getBookings().get(0).getStatus());

      verify(bookingService).getBookingsByAccountId(accountId);
    }
  }

  @Test
  @DisplayName("예약 생성 - 성공")
  void createBooking_Success() {
    // Given
    Long accountId = 300L;
    BookingResponse expectedResponse = new BookingResponse(100L, 1L);
    when(bookingService.createBooking(testBookingRequest, accountId)).thenReturn(expectedResponse);

    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getAccountIdOrThrow).thenReturn(accountId);

      // When
      ResponseEntity<BookingResponse> response =
          bookingController.createBooking(testBookingRequest);

      // Then
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(100L, response.getBody().getEventScheduleId());
      assertEquals(1L, response.getBody().getBookingId());

      verify(bookingService).createBooking(testBookingRequest, accountId);
    }
  }

  @Test
  @DisplayName("예약 취소 - 성공")
  void cancelBooking_Success() {
    // Given
    Long eventScheduleId = 100L;
    Long accountId = 300L;
    doNothing().when(bookingService).cancelBooking(eventScheduleId, accountId);

    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getAccountIdOrThrow).thenReturn(accountId);

      // When
      ResponseEntity<Void> response = bookingController.cancelBooking(eventScheduleId);

      // Then
      assertEquals(HttpStatus.OK, response.getStatusCode());

      verify(bookingService).cancelBooking(eventScheduleId, accountId);
    }
  }

  @Test
  @DisplayName("계정 ID로 예약 조회 - 인증 실패")
  void getBookingsByAccountId_AuthFailure() {
    // Given
    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getAccountIdOrThrow)
          .thenThrow(new RuntimeException("인증 실패"));

      // When & Then
      assertThrows(RuntimeException.class, () -> bookingController.getBookingsByAccountId());

      verify(bookingService, never()).getBookingsByAccountId(any());
    }
  }

  @Test
  @DisplayName("예약 생성 - 인증 실패")
  void createBooking_AuthFailure() {
    // Given
    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getAccountIdOrThrow)
          .thenThrow(new RuntimeException("인증 실패"));

      // When & Then
      assertThrows(RuntimeException.class,
          () -> bookingController.createBooking(testBookingRequest));

      verify(bookingService, never()).createBooking(any(), any());
    }
  }

  @Test
  @DisplayName("예약 취소 - 인증 실패")
  void cancelBooking_AuthFailure() {
    // Given
    Long eventScheduleId = 100L;

    try (MockedStatic<GatewayRequestHeaderUtils> mockedUtils =
        mockStatic(GatewayRequestHeaderUtils.class)) {
      mockedUtils.when(GatewayRequestHeaderUtils::getAccountIdOrThrow)
          .thenThrow(new RuntimeException("인증 실패"));

      // When & Then
      assertThrows(RuntimeException.class, () -> bookingController.cancelBooking(eventScheduleId));

      verify(bookingService, never()).cancelBooking(any(), any());
    }
  }
}
