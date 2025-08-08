package com.pyokemon.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingInfo;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.service.BookingService;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Long validAccountId;
    private Long validEventScheduleId;
    private Long validSeatId;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        validAccountId = 1L;
        validEventScheduleId = 1L;
        validSeatId = 1L;

        validRequest = new BookingRequest();
        validRequest.setEventScheduleId(validEventScheduleId);
        validRequest.setSeatId(validSeatId);
    }

    @Test
    @DisplayName("좌석 ID 조회 성공")
    void getSeatIdsByEventScheduleId_Success() throws Exception {
        List<Long> expectedSeatIds = Arrays.asList(1L, 2L, 3L);
        EventScheduleIdResponse expectedResponse = new EventScheduleIdResponse(expectedSeatIds);
        
        when(bookingService.getSeatIdsByEventScheduleId(validEventScheduleId))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/api/bookings/{eventScheduleId}", validEventScheduleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.seatIds").isArray())
                .andExpect(jsonPath("$.seatIds[0]").value(1))
                .andExpect(jsonPath("$.seatIds[1]").value(2))
                .andExpect(jsonPath("$.seatIds[2]").value(3));
    }

    @Test
    @DisplayName("좌석 ID 조회 - 잘못된 eventScheduleId")
    void getSeatIdsByEventScheduleId_InvalidEventScheduleId() throws Exception {
        when(bookingService.getSeatIdsByEventScheduleId(0L))
                .thenThrow(new BusinessException("이벤트 스케줄 ID가 필요합니다.", "INVALID_EVENT_SCHEDULE_ID"));

        mockMvc.perform(get("/api/bookings/{eventScheduleId}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_EVENT_SCHEDULE_ID"));
    }

    @Test
    @DisplayName("계정별 예약 조회 성공")
    void getBookingsByAccountId_Success() throws Exception {
        List<BookingInfo> bookingInfos = Arrays.asList(
                new BookingInfo(1L, null, Booking.Booked.PENDING, 1L, LocalDateTime.now()),
                new BookingInfo(1L, null, Booking.Booked.BOOKED, 2L, LocalDateTime.now())
        );
        AccountIdResponse expectedResponse = new AccountIdResponse(validAccountId, bookingInfos);
        
        when(bookingService.getBookingsByAccountId(validAccountId))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/api/bookings/account")
                        .header("X-Auth-AccountId", validAccountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value(validAccountId))
                .andExpect(jsonPath("$.bookings").isArray())
                .andExpect(jsonPath("$.bookings.length()").value(2));
    }

    @Test
    @DisplayName("계정별 예약 조회 - 잘못된 accountId")
    void getBookingsByAccountId_InvalidAccountId() throws Exception {
        when(bookingService.getBookingsByAccountId(0L))
                .thenThrow(new BusinessException("계정 ID가 필요합니다.", "INVALID_ACCOUNT_ID"));

        mockMvc.perform(get("/api/bookings/account")
                        .header("X-Auth-AccountId", 0))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_ACCOUNT_ID"));
    }

    @Test
    @DisplayName("계정별 예약 조회 - 헤더 누락")
    void getBookingsByAccountId_MissingHeader() throws Exception {
        mockMvc.perform(get("/api/bookings/account"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 생성 성공")
    void createOrUpdateBooking_Success() throws Exception {
        BookingResponse expectedResponse = new BookingResponse(validEventScheduleId, 1L);
        
        when(bookingService.createOrUpdateBooking(any(BookingRequest.class), eq(validAccountId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/bookings/booking")
                        .header("X-Auth-AccountId", validAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventScheduleId").value(validEventScheduleId))
                .andExpect(jsonPath("$.bookingId").value(1));
    }

    @Test
    @DisplayName("예약 생성 - 잘못된 요청")
    void createOrUpdateBooking_InvalidRequest() throws Exception {
        BookingRequest invalidRequest = new BookingRequest();
        invalidRequest.setEventScheduleId(null);
        invalidRequest.setSeatId(null);

        when(bookingService.createOrUpdateBooking(any(BookingRequest.class), eq(validAccountId)))
                .thenThrow(new BusinessException("이벤트 스케줄 ID가 필요합니다.", "INVALID_EVENT_SCHEDULE_ID"));

        mockMvc.perform(post("/api/bookings/booking")
                        .header("X-Auth-AccountId", validAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_EVENT_SCHEDULE_ID"));
    }

    @Test
    @DisplayName("예약 생성 - 헤더 누락")
    void createOrUpdateBooking_MissingHeader() throws Exception {
        mockMvc.perform(post("/api/bookings/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 생성 - 잘못된 JSON")
    void createOrUpdateBooking_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/bookings/booking")
                        .header("X-Auth-AccountId", validAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 생성 - 결제중인 내역 오류")
    void createOrUpdateBooking_PaymentInProgress() throws Exception {
        when(bookingService.createOrUpdateBooking(any(BookingRequest.class), eq(validAccountId)))
                .thenThrow(new BusinessException("결제중인 내역이 있습니다.", "PAYMENT_IN_PROGRESS"));

        mockMvc.perform(post("/api/bookings/booking")
                        .header("X-Auth-AccountId", validAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_IN_PROGRESS"));
    }

    @Test
    @DisplayName("예약 생성 - 1인1매 제한 오류")
    void createOrUpdateBooking_OnePerEventLimit() throws Exception {
        when(bookingService.createOrUpdateBooking(any(BookingRequest.class), eq(validAccountId)))
                .thenThrow(new BusinessException("1인 1매만 가능합니다.", "BOOKING_ONE_PER_EVENT"));

        mockMvc.perform(post("/api/bookings/booking")
                        .header("X-Auth-AccountId", validAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_ONE_PER_EVENT"));
    }

    @Test
    @DisplayName("예약 생성 - 이미 예약된 좌석 오류")
    void createOrUpdateBooking_SeatAlreadyBooked() throws Exception {
        when(bookingService.createOrUpdateBooking(any(BookingRequest.class), eq(validAccountId)))
                .thenThrow(new BusinessException("이미 예약된 좌석입니다.", "SEAT_ALREADY_BOOKED"));

        mockMvc.perform(post("/api/bookings/booking")
                        .header("X-Auth-AccountId", validAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("SEAT_ALREADY_BOOKED"));
    }
}