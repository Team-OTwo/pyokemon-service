package com.pyokemon.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.booking.dto.kafka.EventKafkaDto;
import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.request.ValidBookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.dto.response.SeatStatusInfo;
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.repository.BookingRepository;
import com.pyokemon.common.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService 단위 테스트")
class BookingServiceTest {

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private BookingEventPublisher bookingEventPublisher;

  @InjectMocks
  private BookingService bookingService;

  private Booking testBooking;
  private BookingRequest testBookingRequest;

  @BeforeEach
  void setUp() {
    testBooking = new Booking();
    testBooking.setId(1L);
    testBooking.setEventScheduleId(100L);
    testBooking.setSeatId(200L);
    testBooking.setAccountId(300L);
    testBooking.setTenantId(400L);
    testBooking.setPaymentId(null);
    testBooking.setStatus(Booking.Booked.PENDING);
    testBooking.setCreatedAt(LocalDateTime.now());
    testBooking.setUpdatedAt(LocalDateTime.now());

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
    when(bookingRepository.findSeatStatusInfosByEventScheduleId(eventScheduleId))
        .thenReturn(seatStatusInfos);

    // When
    EventScheduleIdResponse response = bookingService.getSeatIdsByEventScheduleId(eventScheduleId);

    // Then
    assertNotNull(response);
    assertEquals(2, response.getSeatStatusInfos().size());
    verify(bookingRepository).findSeatStatusInfosByEventScheduleId(eventScheduleId);
  }

  @Test
  @DisplayName("이벤트 스케줄 ID로 좌석 상태 조회 - null 입력 시 예외")
  void getSeatIdsByEventScheduleId_NullInput_ThrowsException() {
    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.getSeatIdsByEventScheduleId(null);
    });
    assertEquals("이벤트 스케줄 ID가 필요합니다.", exception.getMessage());
    assertEquals("INVALID_EVENT_SCHEDULE_ID", exception.getErrorCode());
  }

  @Test
  @DisplayName("계정 ID로 예약 조회 - 성공")
  void getBookingsByAccountId_Success() {
    // Given
    Long accountId = 300L;
    List<Booking> bookings = Arrays.asList(testBooking);
    when(bookingRepository.findByAccountId(accountId)).thenReturn(bookings);

    // When
    AccountIdResponse response = bookingService.getBookingsByAccountId(accountId);

    // Then
    assertNotNull(response);
    assertEquals(accountId, response.getAccountId());
    assertEquals(1, response.getBookings().size());
    verify(bookingRepository).findByAccountId(accountId);
  }

  @Test
  @DisplayName("계정 ID로 예약 조회 - null 입력 시 예외")
  void getBookingsByAccountId_NullInput_ThrowsException() {
    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.getBookingsByAccountId(null);
    });
    assertEquals("계정 ID가 필요합니다.", exception.getMessage());
    assertEquals("INVALID_ACCOUNT_ID", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 유효성 검사 - 성공")
  void validateBookings_Success() {
    // Given
    ValidBookingRequest request = new ValidBookingRequest();
    request.setUserId(300L);
    request.setBookings(Arrays.asList(1L, 2L));
    List<ValidBookingDetail> validBookings = Arrays.asList(new ValidBookingDetail(1L, 100L, 400L),
        new ValidBookingDetail(2L, 101L, 400L));
    when(bookingRepository.findValidBookingsWithEventInfo(request.getBookings(),
        request.getUserId())).thenReturn(validBookings);

    // When
    ValidBookingResponse response = bookingService.validateBookings(request);

    // Then
    assertNotNull(response);
    assertEquals(2, response.getBookings().size());
    verify(bookingRepository).findValidBookingsWithEventInfo(request.getBookings(),
        request.getUserId());
  }

  @Test
  @DisplayName("예약 유효성 검사 - null 사용자 ID 시 예외")
  void validateBookings_NullUserId_ThrowsException() {
    // Given
    ValidBookingRequest request = new ValidBookingRequest();
    request.setUserId(null);
    request.setBookings(Arrays.asList(1L, 2L));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.validateBookings(request);
    });
    assertEquals("사용자 ID가 필요합니다.", exception.getMessage());
    assertEquals("INVALID_USER_ID", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 생성 - 성공")
  void createBooking_Success() {
    // Given
    Long accountId = 300L;
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(
        testBookingRequest.getEventScheduleId(), accountId)).thenReturn(Optional.empty());
    when(bookingRepository.findAllByEventScheduleIdAndSeatId(
        testBookingRequest.getEventScheduleId(), testBookingRequest.getSeatId()))
        .thenReturn(Collections.emptyList());
    doAnswer(invocation -> {
      Booking booking = invocation.getArgument(0);
      booking.setId(1L);
      return null;
    }).when(bookingRepository).save(any(Booking.class));

    // When
    BookingResponse response = bookingService.createBooking(testBookingRequest, accountId);

    // Then
    assertNotNull(response);
    assertEquals(testBookingRequest.getEventScheduleId(), response.getEventScheduleId());
    assertEquals(1L, response.getBookingId());
    verify(bookingRepository).save(any(Booking.class));
  }

  @Test
  @DisplayName("예약 생성 - 이미 PENDING 상태 예약이 있을 때 예외")
  void createBooking_ExistingPendingBooking_ThrowsException() {
    // Given
    Long accountId = 300L;
    Booking existingBooking = new Booking();
    existingBooking.setId(1L);
    existingBooking.setStatus(Booking.Booked.PENDING);
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(
        testBookingRequest.getEventScheduleId(), accountId))
        .thenReturn(Optional.of(existingBooking));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.createBooking(testBookingRequest, accountId);
    });
    assertEquals("결제중인 내역이 있습니다.", exception.getMessage());
    assertEquals("PAYMENT_IN_PROGRESS", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 생성 - 이미 BOOKED 상태 예약이 있을 때 예외")
  void createBooking_ExistingBookedBooking_ThrowsException() {
    // Given
    Long accountId = 300L;
    Booking existingBooking = new Booking();
    existingBooking.setId(1L);
    existingBooking.setStatus(Booking.Booked.BOOKED);
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(
        testBookingRequest.getEventScheduleId(), accountId))
        .thenReturn(Optional.of(existingBooking));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.createBooking(testBookingRequest, accountId);
    });
    assertEquals("1인 1매만 가능합니다.", exception.getMessage());
    assertEquals("BOOKING_ONE_PER_EVENT", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 생성 - 좌석이 이미 예약된 경우 예외")
  void createBooking_SeatAlreadyBooked_ThrowsException() {
    // Given
    Long accountId = 300L;
    Booking existingSeatBooking = new Booking();
    existingSeatBooking.setId(1L);
    existingSeatBooking.setStatus(Booking.Booked.BOOKED);
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(
        testBookingRequest.getEventScheduleId(), accountId)).thenReturn(Optional.empty());
    when(bookingRepository.findAllByEventScheduleIdAndSeatId(
        testBookingRequest.getEventScheduleId(), testBookingRequest.getSeatId()))
        .thenReturn(Arrays.asList(existingSeatBooking));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.createBooking(testBookingRequest, accountId);
    });
    assertEquals("이미 예약된 좌석입니다.", exception.getMessage());
    assertEquals("SEAT_ALREADY_BOOKED", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 취소 - 성공")
  void cancelBooking_Success() {
    // Given
    Long eventScheduleId = 100L;
    Long accountId = 300L;
    Booking booking = new Booking();
    booking.setId(1L);
    booking.setStatus(Booking.Booked.BOOKED);
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(eventScheduleId,
        accountId)).thenReturn(Optional.of(booking));

    // When
    bookingService.cancelBooking(eventScheduleId, accountId);

    // Then
    verify(bookingRepository).update(any(Booking.class));
    verify(bookingEventPublisher).publishBookingStatusUpdate(any(Booking.class));
  }

  @Test
  @DisplayName("예약 취소 - 예약을 찾을 수 없을 때 예외")
  void cancelBooking_BookingNotFound_ThrowsException() {
    // Given
    Long eventScheduleId = 100L;
    Long accountId = 300L;
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(eventScheduleId,
        accountId)).thenReturn(Optional.empty());

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.cancelBooking(eventScheduleId, accountId);
    });
    assertEquals("취소할 예약을 찾을 수 없습니다.", exception.getMessage());
    assertEquals("BOOKING_NOT_FOUND", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 취소 - BOOKED 상태가 아닐 때 예외")
  void cancelBooking_InvalidStatus_ThrowsException() {
    // Given
    Long eventScheduleId = 100L;
    Long accountId = 300L;
    Booking booking = new Booking();
    booking.setId(1L);
    booking.setStatus(Booking.Booked.PENDING);
    when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(eventScheduleId,
        accountId)).thenReturn(Optional.of(booking));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.cancelBooking(eventScheduleId, accountId);
    });
    assertEquals("BOOKED 상태의 예약만 취소할 수 있습니다.", exception.getMessage());
    assertEquals("INVALID_BOOKING_STATUS", exception.getErrorCode());
  }

  @Test
  @DisplayName("예약 상태 업데이트 - 성공")
  void updateBookingStatus_Success() {
    // Given
    Long bookingId = 1L;
    Booking.Booked newStatus = Booking.Booked.BOOKED;
    Long paymentId = 500L;
    Booking booking = new Booking();
    booking.setId(bookingId);
    booking.setStatus(Booking.Booked.PENDING);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

    // When
    bookingService.updateBookingStatus(bookingId, newStatus, paymentId);

    // Then
    verify(bookingRepository).update(any(Booking.class));
    verify(bookingEventPublisher).publishBookingStatusUpdate(any(Booking.class));
  }

  @Test
  @DisplayName("예약 상태 업데이트 - 예약을 찾을 수 없을 때 경고 로그")
  void updateBookingStatus_BookingNotFound_LogsWarning() {
    // Given
    Long bookingId = 1L;
    Booking.Booked newStatus = Booking.Booked.BOOKED;
    Long paymentId = 500L;
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

    // When
    bookingService.updateBookingStatus(bookingId, newStatus, paymentId);

    // Then
    verify(bookingRepository, never()).update(any(Booking.class));
    verify(bookingEventPublisher, never()).publishBookingStatusUpdate(any(Booking.class));
  }

  @Test
  @DisplayName("예약 상태 업데이트 - 이미 취소된 예약일 때 무시")
  void updateBookingStatus_AlreadyCanceled_IgnoresUpdate() {
    // Given
    Long bookingId = 1L;
    Booking.Booked newStatus = Booking.Booked.BOOKED;
    Long paymentId = 500L;
    Booking booking = new Booking();
    booking.setId(bookingId);
    booking.setStatus(Booking.Booked.CANCELED);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

    // When
    bookingService.updateBookingStatus(bookingId, newStatus, paymentId);

    // Then
    verify(bookingRepository, never()).update(any(Booking.class));
    verify(bookingEventPublisher, never()).publishBookingStatusUpdate(any(Booking.class));
  }

  @Test
  @DisplayName("이벤트 취소 시 예약 상태 업데이트 - 성공")
  void cancel_Success() {
    // Given
    EventKafkaDto dto = EventKafkaDto.builder().eventScheduleId(100L).build();
    List<Booking> bookings = Arrays.asList(testBooking);
    when(bookingRepository.findAllByEventScheduleId(dto.getEventScheduleId())).thenReturn(bookings);

    // When
    bookingService.cancel(dto);

    // Then
    verify(bookingRepository).updateStatus(dto.getEventScheduleId(), "CANCELED");
    verify(bookingEventPublisher).publishBookingStatusUpdate(testBooking);
  }

  @Test
  @DisplayName("이벤트 취소 시 예약 상태 업데이트 - 예약이 없을 때 예외")
  void cancel_NoBookings_ThrowsException() {
    // Given
    EventKafkaDto dto = EventKafkaDto.builder().eventScheduleId(100L).build();
    when(bookingRepository.findAllByEventScheduleId(dto.getEventScheduleId()))
        .thenReturn(Collections.emptyList());

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      bookingService.cancel(dto);
    });
    assertEquals("Booking not found.", exception.getMessage());
  }
}
