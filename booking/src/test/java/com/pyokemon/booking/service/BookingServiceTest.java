package com.pyokemon.booking.service;

import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.request.ValidBookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.repository.BookingRepository;
import com.pyokemon.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest validRequest;
    private Long validAccountId;
    private Long validEventScheduleId;
    private Long validSeatId;
    private ValidBookingRequest validBookingRequest;
    private List<ValidBookingDetail> mockBookingDetails;

    @BeforeEach
    void setUp() {
        validAccountId = 1L;
        validEventScheduleId = 1L;
        validSeatId = 1L;
        
        validRequest = new BookingRequest();
        validRequest.setEventScheduleId(validEventScheduleId);
        validRequest.setSeatId(validSeatId);

        // ValidBooking 테스트용 데이터
        validBookingRequest = ValidBookingRequest.builder()
                .userId(398413L)
                .bookings(Arrays.asList(12312341L, 12431231L, 141231L))
                .build();

        mockBookingDetails = Arrays.asList(
                ValidBookingDetail.builder()
                        .bookingId(12312341L)
                        .eventScheduleId(101L)
                        .tenantId(201L)
                        .build(),
                ValidBookingDetail.builder()
                        .bookingId(12431231L)
                        .eventScheduleId(102L)
                        .tenantId(202L)
                        .build()
        );
    }

    @Test
    @DisplayName("좌석 ID 조회 성공")
    void getSeatIdsByEventScheduleId_Success() {
        List<Long> expectedSeatIds = Arrays.asList(1L, 2L, 3L);
        when(bookingRepository.findSeatIdsByEventScheduleId(validEventScheduleId))
                .thenReturn(expectedSeatIds);

        EventScheduleIdResponse response = bookingService.getSeatIdsByEventScheduleId(validEventScheduleId);

        assertNotNull(response);
        assertEquals(expectedSeatIds, response.getSeatIds());
        verify(bookingRepository).findSeatIdsByEventScheduleId(validEventScheduleId);
    }

    @Test
    @DisplayName("좌석 ID 조회 - null eventScheduleId")
    void getSeatIdsByEventScheduleId_NullEventScheduleId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.getSeatIdsByEventScheduleId(null);
        });
        
        assertEquals("INVALID_EVENT_SCHEDULE_ID", exception.getErrorCode());
    }

    @Test
    @DisplayName("계정별 예약 조회 성공")
    void getBookingsByAccountId_Success() {
        List<Booking> bookings = Arrays.asList(
                createBooking(1L, Booking.Booked.PENDING),
                createBooking(2L, Booking.Booked.BOOKED)
        );
        when(bookingRepository.findByAccountId(validAccountId)).thenReturn(bookings);

        AccountIdResponse response = bookingService.getBookingsByAccountId(validAccountId);

        assertNotNull(response);
        assertEquals(validAccountId, response.getAccountId());
        assertEquals(2, response.getBookings().size());
        verify(bookingRepository).findByAccountId(validAccountId);
    }

    @Test
    @DisplayName("계정별 예약 조회 - null accountId")
    void getBookingsByAccountId_NullAccountId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.getBookingsByAccountId(null);
        });
        
        assertEquals("INVALID_ACCOUNT_ID", exception.getErrorCode());
    }

    @Test
    @DisplayName("새 예약 생성 성공")
    void createOrUpdateBooking_NewBooking_Success() {
        when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
                .thenReturn(Optional.empty());
        when(bookingRepository.findAllByEventScheduleIdAndSeatId(validEventScheduleId, validSeatId))
                .thenReturn(Collections.emptyList());
        doAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId(1L);
            return null;
        }).when(bookingRepository).save(any(Booking.class));

        BookingResponse response = bookingService.createOrUpdateBooking(validRequest, validAccountId);

        assertNotNull(response);
        assertEquals(validEventScheduleId, response.getEventScheduleId());
        assertEquals(1L, response.getBookingId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("같은 좌석 취소 성공")
    void createOrUpdateBooking_SameSeat_CancelSuccess() {
        Booking existingBooking = createBooking(validSeatId, Booking.Booked.PENDING);
        when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
                .thenReturn(Optional.of(existingBooking));

        BookingResponse response = bookingService.createOrUpdateBooking(validRequest, validAccountId);

        assertNotNull(response);
        assertEquals(validEventScheduleId, response.getEventScheduleId());
        verify(bookingRepository).update(any(Booking.class));
    }

    @Test
    @DisplayName("다른 좌석 예약 시도 - PENDING 상태")
    void createOrUpdateBooking_DifferentSeat_PendingStatus() {
        Booking existingBooking = createBooking(2L, Booking.Booked.PENDING);
        when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
                .thenReturn(Optional.of(existingBooking));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createOrUpdateBooking(validRequest, validAccountId);
        });
        
        assertEquals("PAYMENT_IN_PROGRESS", exception.getErrorCode());
    }

    @Test
    @DisplayName("다른 좌석 예약 시도 - BOOKED 상태")
    void createOrUpdateBooking_DifferentSeat_BookedStatus() {
        Booking existingBooking = createBooking(2L, Booking.Booked.BOOKED);
        when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
                .thenReturn(Optional.of(existingBooking));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createOrUpdateBooking(validRequest, validAccountId);
        });
        
        assertEquals("BOOKING_ONE_PER_EVENT", exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 예약된 좌석 예약 시도")
    void createOrUpdateBooking_AlreadyBookedSeat() {
        when(bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
                .thenReturn(Optional.empty());
        
        Booking existingSeatBooking = createBooking(validSeatId, Booking.Booked.PENDING);
        when(bookingRepository.findAllByEventScheduleIdAndSeatId(validEventScheduleId, validSeatId))
                .thenReturn(Collections.singletonList(existingSeatBooking));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createOrUpdateBooking(validRequest, validAccountId);
        });
        
        assertEquals("SEAT_ALREADY_BOOKED", exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 생성 - null eventScheduleId")
    void createOrUpdateBooking_NullEventScheduleId() {
        validRequest.setEventScheduleId(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createOrUpdateBooking(validRequest, validAccountId);
        });
        
        assertEquals("INVALID_EVENT_SCHEDULE_ID", exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 생성 - null seatId")
    void createOrUpdateBooking_NullSeatId() {
        validRequest.setSeatId(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createOrUpdateBooking(validRequest, validAccountId);
        });
        
        assertEquals("INVALID_SEAT_ID", exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 생성 - null accountId")
    void createOrUpdateBooking_NullAccountId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createOrUpdateBooking(validRequest, null);
        });
        
        assertEquals("INVALID_ACCOUNT_ID", exception.getErrorCode());
    }

    @Test
    @DisplayName("PENDING 예약 삭제 성공")
    void deletePendingBookings_Success() {
        List<Booking> pendingBookings = Arrays.asList(
                createBooking(1L, Booking.Booked.PENDING),
                createBooking(2L, Booking.Booked.PENDING)
        );
        when(bookingRepository.findPendingBookings()).thenReturn(pendingBookings);

        bookingService.deletePendingBookings();

        verify(bookingRepository, times(2)).delete(anyLong());
    }

    @Test
    @DisplayName("PENDING 예약 삭제 - 빈 리스트")
    void deletePendingBookings_EmptyList() {
        when(bookingRepository.findPendingBookings()).thenReturn(Collections.emptyList());

        bookingService.deletePendingBookings();

        verify(bookingRepository, never()).delete(anyLong());
    }

    @Test
    @DisplayName("유효한 예약 검증 - 성공")
    void validateBookings_Success() {
        when(bookingRepository.findValidBookingsWithEventInfo(
                eq(validBookingRequest.getBookings()), 
                eq(validBookingRequest.getUserId())))
                .thenReturn(mockBookingDetails);

        ValidBookingResponse response = bookingService.validateBookings(validBookingRequest);

        assertNotNull(response);
        assertEquals(2, response.getBookings().size());
        assertEquals(12312341L, response.getBookings().get(0).getBookingId());
        assertEquals(101L, response.getBookings().get(0).getEventScheduleId());
        assertEquals(201L, response.getBookings().get(0).getTenantId());
        verify(bookingRepository).findValidBookingsWithEventInfo(
                validBookingRequest.getBookings(), 
                validBookingRequest.getUserId());
    }

    @Test
    @DisplayName("유효한 예약 검증 - 빈 결과")
    void validateBookings_EmptyResult() {
        when(bookingRepository.findValidBookingsWithEventInfo(
                eq(validBookingRequest.getBookings()), 
                eq(validBookingRequest.getUserId())))
                .thenReturn(Collections.emptyList());

        ValidBookingResponse response = bookingService.validateBookings(validBookingRequest);

        assertNotNull(response);
        assertTrue(response.getBookings().isEmpty());
    }

    @Test
    @DisplayName("유효한 예약 검증 - userId가 null인 경우")
    void validateBookings_UserIdNull() {
        ValidBookingRequest invalidRequest = ValidBookingRequest.builder()
                .userId(null)
                .bookings(Arrays.asList(12312341L, 12431231L))
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.validateBookings(invalidRequest);
        });
        
        assertEquals("INVALID_USER_ID", exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 예약 검증 - bookings가 null인 경우")
    void validateBookings_BookingsNull() {
        ValidBookingRequest invalidRequest = ValidBookingRequest.builder()
                .userId(398413L)
                .bookings(null)
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.validateBookings(invalidRequest);
        });
        
        assertEquals("INVALID_BOOKING_IDS", exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 예약 검증 - bookings가 빈 리스트인 경우")
    void validateBookings_BookingsEmpty() {
        ValidBookingRequest invalidRequest = ValidBookingRequest.builder()
                .userId(398413L)
                .bookings(Collections.emptyList())
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.validateBookings(invalidRequest);
        });
        
        assertEquals("INVALID_BOOKING_IDS", exception.getErrorCode());
    }

    private Booking createBooking(Long seatId, Booking.Booked status) {
        return Booking.builder()
                .bookingId(1L)
                .eventScheduleId(validEventScheduleId)
                .seatId(seatId)
                .accountId(validAccountId)
                .paymentId(null)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}