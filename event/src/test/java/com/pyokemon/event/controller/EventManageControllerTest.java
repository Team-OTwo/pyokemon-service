package com.pyokemon.event.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.EventUpdateDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.dto.TenantBookingDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventListDto;
import com.pyokemon.event.entity.Event.EventStatus;
import com.pyokemon.event.service.EventScheduleService;
import com.pyokemon.event.service.EventService;

@WebMvcTest(EventController.class)
class TenantEventManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EventService eventService;

  @MockBean
  private EventScheduleService eventScheduleService;

  @Autowired
  private ObjectMapper objectMapper;

  private Long validAccountId = 1L;
  private Long validEventId = 1L;

  @BeforeEach
  void setUp() {
    // 테스트 설정
  }

  @Test
  @DisplayName("TenantEventListTest - 테넌트별 공연 목록 조회 API 테스트")
  void getTenantEventList_Success() throws Exception {
    // given
    TenantEventListDto mockEvent = TenantEventListDto.builder()
        .eventId(validEventId)
        .eventScheduleId(1L)
        .title("테스트 공연")
        .eventDate(LocalDateTime.now().plusDays(30))
        .venueName("테스트 공연장")
        .status("APPROVED")
        .build();

    when(eventService.getTenantEventListByAccountId(validAccountId)).thenReturn(List.of(mockEvent));

    // when & then
    mockMvc.perform(get("/api/events/tenant").param("account_id", validAccountId.toString()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].eventId").value(1))
        .andExpect(jsonPath("$.data[0].eventScheduleId").value(1))
        .andExpect(jsonPath("$.data[0].title").value("테스트 공연"))
        .andExpect(jsonPath("$.data[0].venueName").value("테스트 공연장"))
        .andExpect(jsonPath("$.data[0].status").value("APPROVED"));

    verify(eventService).getTenantEventListByAccountId(validAccountId);
  }

  @Test
  @DisplayName("TenantEventDetailTest - 테넌트별 공연 상세 조회 API 테스트")
  void getTenantEventDetail_Success() throws Exception {
    // given
    TenantEventDetailResponseDTO mockDetail =
        TenantEventDetailResponseDTO.builder().eventId(validEventId).title("테스트 공연").ageLimit(12L)
            .description("테스트 공연 설명").genre("콘서트").thumbnailUrl("https://example.com/image.jpg")
            .status("APPROVED").eventScheduleId(1L).ticketOpenAt(LocalDateTime.now().plusDays(7))
            .eventDate(LocalDateTime.now().plusDays(30)).venueName("테스트 공연장")
            .prices(List.of(
                TenantEventDetailResponseDTO.PriceInfo.builder().priceId(1L).seatClassId(1L)
                    .className("VIP").price(150000).build(),
                TenantEventDetailResponseDTO.PriceInfo.builder().priceId(2L).seatClassId(2L)
                    .className("R석").price(100000).build()))
            .build();

    when(eventService.getTenantEventDetailByEventId(validEventId)).thenReturn(mockDetail);

    // when & then
    mockMvc.perform(get("/api/events/tenant/{eventId}/detail", validEventId))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.eventId").value(1))
        .andExpect(jsonPath("$.data.title").value("테스트 공연"))
        .andExpect(jsonPath("$.data.venueName").value("테스트 공연장"))
        .andExpect(jsonPath("$.data.prices[0].className").value("VIP"))
        .andExpect(jsonPath("$.data.prices[0].price").value(150000))
        .andExpect(jsonPath("$.data.prices[1].className").value("R석"))
        .andExpect(jsonPath("$.data.prices[1].price").value(100000));

    verify(eventService).getTenantEventDetailByEventId(validEventId);
  }

  @Test
  @DisplayName("EventRegistTest - 공연 등록 API 테스트")
  void registerEvent_Success() throws Exception {
    // given
    EventRegisterDto registerDto =
        EventRegisterDto.builder().accountId(validAccountId).title("새로운 공연").ageLimit(12L)
            .description("새로운 공연 설명").genre("뮤지컬").thumbnailUrl("https://example.com/new-image.jpg")
            .schedules(List.of(
                EventScheduleDto.builder().venueId(1L).ticketOpenAt(LocalDateTime.now().plusDays(7))
                    .eventDate(LocalDateTime.now().plusDays(30))
                    .prices(List.of(PriceDto.builder().seatClassId(1L).price(150000).build(),
                        PriceDto.builder().seatClassId(2L).price(100000).build()))
                    .build()))
            .build();

    EventResponseDto mockResponse = EventResponseDto.builder().eventId(validEventId)
        .accountId(validAccountId).title("새로운 공연").status(EventStatus.PENDING).build();

    when(eventService.registerEvent(any(EventRegisterDto.class))).thenReturn(mockResponse);

    // when & then
    mockMvc
        .perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerDto)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title").value("새로운 공연"))
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andExpect(jsonPath("$.message").value("Event registered successfully"));

    verify(eventService).registerEvent(any(EventRegisterDto.class));
  }

  @Test
  @DisplayName("EventEditTest - 공연 수정 API 테스트")
  void updateEvent_Success() throws Exception {
    // given
    EventUpdateDto updateDto = EventUpdateDto.builder().eventId(validEventId).title("수정된 공연")
        .ageLimit(15L).description("수정된 공연 설명").genre("연극")
        .thumbnailUrl("https://example.com/updated-image.jpg").status(EventStatus.APPROVED).build();

    EventResponseDto mockResponse = EventResponseDto.builder().eventId(validEventId)
        .accountId(validAccountId).title("수정된 공연").status(EventStatus.APPROVED).build();

    when(eventService.updateEvent(any(EventUpdateDto.class))).thenReturn(mockResponse);

    // when & then
    mockMvc
        .perform(put("/api/events/{eventId}", validEventId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title").value("수정된 공연"))
        .andExpect(jsonPath("$.data.status").value("APPROVED"))
        .andExpect(jsonPath("$.message").value("Event updated successfully"));

    verify(eventService).updateEvent(any(EventUpdateDto.class));
  }

  @Test
  @DisplayName("TenantBookingDetailTest - 테넌트별 예매 현황 조회 API 테스트")
  void getTenantBookingDetail_Success() throws Exception {
    // given
    Long eventScheduleId = 1L;
    TenantBookingDetailResponseDTO mockBookingDetail = TenantBookingDetailResponseDTO.builder()
        .eventId(validEventId)
        .title("테스트 공연")
        .genre("콘서트")
        .status("APPROVED")
        .eventScheduleId(eventScheduleId)
        .ticketOpenAt(LocalDateTime.now().plusDays(7))
        .eventDate(LocalDateTime.now().plusDays(30))
        .venueName("테스트 공연장")
        .bookingStatus(List.of(
            TenantBookingDetailResponseDTO.BookingStatusInfo.builder()
                .seatClassId(1L)
                .className("VIP")
                .totalSeats(100)
                .bookedSeats(30)
                .availableSeats(70)
                .price(150000)
                .bookingRate(30.0)
                .build(),
            TenantBookingDetailResponseDTO.BookingStatusInfo.builder()
                .seatClassId(2L)
                .className("R석")
                .totalSeats(200)
                .bookedSeats(80)
                .availableSeats(120)
                .price(100000)
                .bookingRate(40.0)
                .build()
        ))
        .build();

    when(eventService.getTenantBookingDetailByEventScheduleId(eventScheduleId))
        .thenReturn(mockBookingDetail);

    // when & then
    mockMvc.perform(get("/api/events/tenant/booking/{eventScheduleId}/detail", eventScheduleId))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.eventId").value(1))
        .andExpect(jsonPath("$.data.title").value("테스트 공연"))
        .andExpect(jsonPath("$.data.venueName").value("테스트 공연장"))
        .andExpect(jsonPath("$.data.bookingStatus[0].className").value("VIP"))
        .andExpect(jsonPath("$.data.bookingStatus[0].totalSeats").value(100))
        .andExpect(jsonPath("$.data.bookingStatus[0].bookedSeats").value(30))
        .andExpect(jsonPath("$.data.bookingStatus[0].availableSeats").value(70))
        .andExpect(jsonPath("$.data.bookingStatus[0].bookingRate").value(30.0));

    verify(eventService).getTenantBookingDetailByEventScheduleId(eventScheduleId);
  }
}
