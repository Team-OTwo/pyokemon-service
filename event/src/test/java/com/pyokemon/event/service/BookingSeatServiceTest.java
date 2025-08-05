package com.pyokemon.event.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;
import com.pyokemon.event.dto.*;
import com.pyokemon.event.entity.*;
import com.pyokemon.event.repository.*;
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
class BookingSeatServiceTest {

    @Mock
    private EventScheduleRepository eventScheduleRepository;
    
    @Mock
    private SeatRepository seatRepository;
    
    @Mock
    private SeatClassRepository seatClassRepository;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private BookingSeatService bookingSeatService;

    private BookingRequestDto validBookingRequest;
    private EventSchedule validEventSchedule;
    private Seat validSeat;
    private Booking validBooking;
    private Long validAccountId = 1L;
    private Long validEventScheduleId = 1L;
    private Long validSeatId = 1L;
    private Long validVenueId = 1L;

    @BeforeEach
    void setUp() {
        validBookingRequest = new BookingRequestDto(validEventScheduleId, validSeatId);
        
        validEventSchedule = new EventSchedule();
        validEventSchedule.setEventScheduleId(validEventScheduleId);
        validEventSchedule.setVenueId(validVenueId);
        
        validSeat = new Seat();
        validSeat.setSeatId(validSeatId);
        validSeat.setVenueId(validVenueId);
        validSeat.setSeatClassId(1L);
        validSeat.setRow("A");
        validSeat.setCol("1");
        
        validBooking = new Booking();
        validBooking.setBookingId(1L);
        validBooking.setEventScheduleId(validEventScheduleId);
        validBooking.setSeatId(validSeatId);
        validBooking.setAccountId(validAccountId);
        validBooking.setStatus(Booking.Booked.PENDING);
        validBooking.setCreatedAt(LocalDateTime.now());
        validBooking.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("정상적인 예약 생성 테스트")
    void createBooking_Success() {
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findById(validSeatId))
            .thenReturn(Optional.of(validSeat));
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.PENDING))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
            .thenReturn(Collections.emptyList());
        doAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId(1L);
            return null;
        }).when(bookingRepository).insert(any(Booking.class));

        BookingResponseDto result = bookingSeatService.createBooking(validBookingRequest, validAccountId);

        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals(validEventScheduleId, result.getEventScheduleId());
        
        verify(bookingRepository).insert(any(Booking.class));
        verify(eventScheduleRepository).findById(validEventScheduleId);
        verify(seatRepository).findById(validSeatId);
    }

    @Test
    @DisplayName("null 예약 요청으로 예약 생성 시 예외 발생")
    void createBooking_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(null, validAccountId));
        
        assertEquals(EventErrorCodes.BOOKING_ID_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효하지 않은 이벤트 스케줄 ID로 예약 생성 시 예외 발생")
    void createBooking_InvalidEventScheduleId_ThrowsException() {
        BookingRequestDto invalidRequest = new BookingRequestDto(null, validSeatId);

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(invalidRequest, validAccountId));
        
        assertEquals(EventErrorCodes.EVENT_ID_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효하지 않은 좌석 ID로 예약 생성 시 예외 발생")
    void createBooking_InvalidSeatId_ThrowsException() {
        BookingRequestDto invalidRequest = new BookingRequestDto(validEventScheduleId, null);

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(invalidRequest, validAccountId));
        
        assertEquals(EventErrorCodes.SEAT_ID_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 스케줄로 예약 생성 시 예외 발생")
    void createBooking_NonExistentEventSchedule_ThrowsException() {
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(validBookingRequest, validAccountId));
        
        assertEquals(EventErrorCodes.SCHEDULE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 좌석으로 예약 생성 시 예외 발생")
    void createBooking_NonExistentSeat_ThrowsException() {
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findById(validSeatId))
            .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(validBookingRequest, validAccountId));
        
        assertEquals(EventErrorCodes.SEAT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 예약된 좌석으로 예약 생성 시 예외 발생")
    void createBooking_AlreadyBookedSeat_ThrowsException() {
        Booking bookedBooking = new Booking();
        bookedBooking.setSeatId(validSeatId);
        bookedBooking.setStatus(Booking.Booked.BOOKED);
        
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findById(validSeatId))
            .thenReturn(Optional.of(validSeat));
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Arrays.asList(bookedBooking));

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(validBookingRequest, validAccountId));
        
        assertEquals(EventErrorCodes.SEAT_ALREADY_RESERVED, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 예약 중인 좌석으로 예약 생성 시 예외 발생")
    void createBooking_PendingSeat_ThrowsException() {
        Booking pendingBooking = new Booking();
        pendingBooking.setSeatId(validSeatId);
        pendingBooking.setStatus(Booking.Booked.PENDING);
        
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findById(validSeatId))
            .thenReturn(Optional.of(validSeat));
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.PENDING))
            .thenReturn(Arrays.asList(pendingBooking));

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(validBookingRequest, validAccountId));
        
        assertEquals(EventErrorCodes.SEAT_ALREADY_RESERVED, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 해당 이벤트에 예약이 있는 경우 예외 발생")
    void createBooking_ExistingBookingForEvent_ThrowsException() {
        Booking existingBooking = new Booking();
        existingBooking.setAccountId(validAccountId);
        existingBooking.setStatus(Booking.Booked.BOOKED);
        
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findById(validSeatId))
            .thenReturn(Optional.of(validSeat));
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.PENDING))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndAccountId(validEventScheduleId, validAccountId))
            .thenReturn(Arrays.asList(existingBooking));

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.createBooking(validBookingRequest, validAccountId));
        
        assertEquals(EventErrorCodes.BOOKING_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 좌석 정보 조회 테스트")
    void getEventScheduleSeats_Success() {
        List<Seat> seats = Arrays.asList(validSeat);
        List<SeatClass> seatClasses = Arrays.asList(createSeatClass(1L, "VIP"));
        List<Price> prices = Arrays.asList(createPrice(1L, 50000));
        
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findByVenueId(validVenueId))
            .thenReturn(seats);
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.PENDING))
            .thenReturn(Collections.emptyList());
        when(priceRepository.findByEventScheduleId(validEventScheduleId))
            .thenReturn(prices);
        when(seatClassRepository.findAll())
            .thenReturn(seatClasses);

        EventScheduleSeatResponse result = bookingSeatService.getEventScheduleSeats(validEventScheduleId);

        assertNotNull(result);
        assertEquals(validEventScheduleId, result.getEventScheduleId());
        assertEquals(1, result.getRemainingSeatsByGrade().size());
        assertEquals("VIP", result.getRemainingSeatsByGrade().get(0).getSeatGrade());
        assertEquals(1, result.getRemainingSeatsByGrade().get(0).getRemainingSeats());
        assertEquals(50000, result.getRemainingSeatsByGrade().get(0).getPrice());
    }

    @Test
    @DisplayName("유효하지 않은 이벤트 스케줄 ID로 좌석 정보 조회 시 예외 발생")
    void getEventScheduleSeats_InvalidEventScheduleId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.getEventScheduleSeats(null));
        
        assertEquals(EventErrorCodes.EVENT_ID_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 스케줄로 좌석 정보 조회 시 예외 발생")
    void getEventScheduleSeats_NonExistentEventSchedule_ThrowsException() {
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.getEventScheduleSeats(validEventScheduleId));
        
        assertEquals(EventErrorCodes.SCHEDULE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좌석이 없는 장소로 좌석 정보 조회 시 예외 발생")
    void getEventScheduleSeats_NoSeatsInVenue_ThrowsException() {
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findByVenueId(validVenueId))
            .thenReturn(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.getEventScheduleSeats(validEventScheduleId));
        
        assertEquals(EventErrorCodes.SEAT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 좌석 맵 조회 테스트")
    void getSeatMapOnlyByGrade_Success() {
        List<Seat> seats = Arrays.asList(validSeat);
        List<SeatClass> seatClasses = Arrays.asList(createSeatClass(1L, "VIP"));
        
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findByVenueId(validVenueId))
            .thenReturn(seats);
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.PENDING))
            .thenReturn(Collections.emptyList());
        when(seatClassRepository.findAll())
            .thenReturn(seatClasses);

        List<SeatMapDetail> result = bookingSeatService.getSeatMapOnlyByGrade(validEventScheduleId, "VIP");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(validSeatId, result.get(0).getSeatId());
        assertEquals("A", result.get(0).getRow());
        assertEquals(1, result.get(0).getCol());
        assertEquals("VIP", result.get(0).getSeatGrade());
        assertFalse(result.get(0).isBooked());
    }

    @Test
    @DisplayName("유효하지 않은 이벤트 스케줄 ID로 좌석 맵 조회 시 예외 발생")
    void getSeatMapOnlyByGrade_InvalidEventScheduleId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.getSeatMapOnlyByGrade(null, "VIP"));
        
        assertEquals(EventErrorCodes.EVENT_ID_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("빈 좌석 등급으로 좌석 맵 조회 시 예외 발생")
    void getSeatMapOnlyByGrade_EmptySeatGrade_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.getSeatMapOnlyByGrade(validEventScheduleId, ""));
        
        assertEquals(EventErrorCodes.SEAT_CLASS_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 좌석 등급으로 좌석 맵 조회 시 예외 발생")
    void getSeatMapOnlyByGrade_NonExistentSeatGrade_ThrowsException() {
        List<Seat> seats = Arrays.asList(validSeat);
        List<SeatClass> seatClasses = Arrays.asList(createSeatClass(1L, "VIP"));
        
        when(eventScheduleRepository.findById(validEventScheduleId))
            .thenReturn(Optional.of(validEventSchedule));
        when(seatRepository.findByVenueId(validVenueId))
            .thenReturn(seats);
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.BOOKED))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.findByEventScheduleIdAndStatus(validEventScheduleId, Booking.Booked.PENDING))
            .thenReturn(Collections.emptyList());
        when(seatClassRepository.findAll())
            .thenReturn(seatClasses);

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bookingSeatService.getSeatMapOnlyByGrade(validEventScheduleId, "NON_EXISTENT"));
        
        assertEquals(EventErrorCodes.SEAT_CLASS_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("PENDING 예약 삭제 테스트")
    void deletePendingBookings_Success() {
        List<Booking> pendingBookings = Arrays.asList(validBooking);
        when(bookingRepository.findByStatus(Booking.Booked.PENDING))
            .thenReturn(pendingBookings);

        bookingSeatService.deletePendingBookings();

        verify(bookingRepository).findByStatus(Booking.Booked.PENDING);
        verify(bookingRepository).delete(validBooking);
    }

    @Test
    @DisplayName("삭제할 PENDING 예약이 없는 경우")
    void deletePendingBookings_NoPendingBookings() {
        when(bookingRepository.findByStatus(Booking.Booked.PENDING))
            .thenReturn(Collections.emptyList());

        bookingSeatService.deletePendingBookings();

        verify(bookingRepository).findByStatus(Booking.Booked.PENDING);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    @DisplayName("PENDING 예약 삭제 중 예외 발생 시 로그 기록")
    void deletePendingBookings_ExceptionDuringDeletion_LogsError() {
        List<Booking> pendingBookings = Arrays.asList(validBooking);
        when(bookingRepository.findByStatus(Booking.Booked.PENDING))
            .thenReturn(pendingBookings);
        doThrow(new RuntimeException("Database error"))
            .when(bookingRepository).delete(validBooking);

        bookingSeatService.deletePendingBookings();

        verify(bookingRepository).findByStatus(Booking.Booked.PENDING);
        verify(bookingRepository).delete(validBooking);
    }

    private SeatClass createSeatClass(Long seatClassId, String className) {
        SeatClass seatClass = new SeatClass();
        seatClass.setSeatClassId(seatClassId);
        seatClass.setClassName(className);
        return seatClass;
    }

    private Price createPrice(Long seatClassId, Integer price) {
        Price priceObj = new Price();
        priceObj.setSeatClassId(seatClassId);
        priceObj.setPrice(price);
        return priceObj;
    }
}