package com.pyokemon.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.request.ValidBookingRequest;
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ValidBookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private ValidBookingController validBookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ValidBookingRequest validRequest;
    private ValidBookingResponse validResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(validBookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        validRequest = ValidBookingRequest.builder()
                .userId(398413L)
                .bookings(Arrays.asList(12312341L, 12431231L, 141231L))
                .build();

        List<ValidBookingDetail> bookingDetails = Arrays.asList(
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

        validResponse = ValidBookingResponse.builder()
                .bookings(bookingDetails)
                .build();
    }

    @Test
    @DisplayName("유효한 예약 검증 API 테스트 - 성공")
    void validateBookings_Success() throws Exception {
        when(bookingService.validateBookings(any(ValidBookingRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/backend/validbookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings").isArray())
                .andExpect(jsonPath("$.bookings.length()").value(2))
                .andExpect(jsonPath("$.bookings[0].bookingId").value(12312341))
                .andExpect(jsonPath("$.bookings[0].eventScheduleId").value(101))
                .andExpect(jsonPath("$.bookings[0].tenantId").value(201))
                .andExpect(jsonPath("$.bookings[1].bookingId").value(12431231))
                .andExpect(jsonPath("$.bookings[1].eventScheduleId").value(102))
                .andExpect(jsonPath("$.bookings[1].tenantId").value(202));
    }

    @Test
    @DisplayName("유효한 예약 검증 API 테스트 - 빈 예약 목록")
    void validateBookings_EmptyBookings() throws Exception {
        ValidBookingRequest emptyRequest = ValidBookingRequest.builder()
                .userId(398413L)
                .bookings(List.of())
                .build();

        ValidBookingResponse emptyResponse = ValidBookingResponse.builder()
                .bookings(List.of())
                .build();

        when(bookingService.validateBookings(any(ValidBookingRequest.class)))
                .thenReturn(emptyResponse);

        mockMvc.perform(post("/backend/validbookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings").isArray())
                .andExpect(jsonPath("$.bookings.length()").value(0));
    }

    @Test
    @DisplayName("유효한 예약 검증 API 테스트 - 잘못된 요청")
    void validateBookings_InvalidRequest() throws Exception {
        ValidBookingRequest invalidRequest = ValidBookingRequest.builder()
                .userId(null)
                .bookings(Arrays.asList(12312341L, 12431231L))
                .build();

        when(bookingService.validateBookings(any(ValidBookingRequest.class)))
                .thenThrow(new BusinessException("사용자 ID가 필요합니다.", "INVALID_USER_ID"));

        mockMvc.perform(post("/backend/validbookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_USER_ID"));
    }

    @Test
    @DisplayName("유효한 예약 검증 API 테스트 - 잘못된 JSON")
    void validateBookings_InvalidJson() throws Exception {
        mockMvc.perform(post("/backend/validbookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }
}
