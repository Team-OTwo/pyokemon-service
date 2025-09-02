package com.pyokemon.event.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.exception.GlobalExceptionHandler;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.tenant.EventRegisterDto;
import com.pyokemon.event.dto.tenant.EventResponseDto;
import com.pyokemon.event.dto.tenant.EventScheduleDto;
import com.pyokemon.event.dto.tenant.EventUpdateDto;
import com.pyokemon.event.dto.tenant.TenantEventListDto;
import com.pyokemon.event.service.TenantEventService;

@ExtendWith(MockitoExtension.class)
class TenantEventControllerTest {

  @Mock
  private TenantEventService tenantEventService;

  @InjectMocks
  private TenantEventController tenantEventController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  private EventRegisterDto mockEventRegisterDto;
  private EventResponseDto mockEventResponseDto;
  private TenantEventListDto mockTenantEventList;
  private EventDetailResponseDTO mockTenantEventDetail;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(tenantEventController)
        .setControllerAdvice(new GlobalExceptionHandler()).build();
    objectMapper = new ObjectMapper();

    mockEventRegisterDto = EventRegisterDto.builder().accountId(1L).title("Test Event")
        .ageLimit(19L).description("Test Description").genre("Pop").thumbnailUrl("test.jpg")
        .schedules(Arrays.asList(
            EventScheduleDto.builder().venueId(1L).ticketOpenAt(LocalDateTime.now().plusDays(1))
                .eventDate(LocalDateTime.now().plusDays(7)).build()))
        .build();

    mockEventResponseDto =
        EventResponseDto.builder().eventId(1L).accountId(1L).title("Test Event").ageLimit(19L)
            .description("Test Description").genre("Pop").thumbnailUrl("test.jpg").build();

    mockTenantEventList = new TenantEventListDto();
    mockTenantEventList.setEventId(1L);
    mockTenantEventList.setTitle("Test Event");

    mockTenantEventDetail = new EventDetailResponseDTO();
    mockTenantEventDetail.setEventId(1L);
    mockTenantEventDetail.setTitle("Test Event");



  }

  @Test
  void getTenantEventList_ShouldReturnTenantEventList() throws Exception {
    Long accountId = 1L;
    List<TenantEventListDto> expectedEvents = Arrays.asList(mockTenantEventList);

    when(tenantEventService.getTenantEventListByAccountId(accountId)).thenReturn(expectedEvents);

    mockMvc.perform(get("/api/events/tenant").param("account_id", accountId.toString()))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].eventId").value(1))
        .andExpect(jsonPath("$.data[0].title").value("Test Event"));

    verify(tenantEventService).getTenantEventListByAccountId(accountId);
  }



  @Test
  void getTenantEventDetail_ShouldReturnTenantEventDetail() throws Exception {
    Long eventId = 1L;

    when(tenantEventService.getTenantEventDetailByEventId(eventId))
        .thenReturn(mockTenantEventDetail);

    mockMvc.perform(get("/api/events/tenant/{eventId}/detail", eventId)).andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.eventId").value(1))
        .andExpect(jsonPath("$.data.title").value("Test Event"));

    verify(tenantEventService).getTenantEventDetailByEventId(eventId);
  }



  @Test
    void registerEvent_ShouldRegisterEventSuccessfully() throws Exception {
        when(tenantEventService.registerEvent(any(EventRegisterDto.class), eq(1L))).thenReturn(mockEventResponseDto);

        mockMvc.perform(post("/api/events/tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockEventRegisterDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventId").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Event"));

        verify(tenantEventService).registerEvent(any(EventRegisterDto.class), eq(1L));
    }

  @Test
  void updateEvent_ShouldUpdateEventSuccessfully() throws Exception {
    EventRegisterDto updateDto = EventRegisterDto.builder().title("Updated Event").ageLimit(19L)
        .description("Updated Description").genre("Rock").thumbnailUrl("updated.jpg").build();

    EventResponseDto updatedResponse =
        EventResponseDto.builder().eventId(1L).title("Updated Event").build();

    when(tenantEventService.updateEvent(any(EventUpdateDto.class))).thenReturn(updatedResponse);

    mockMvc
        .perform(put("/api/events/tenant/{eventId}", 1L).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title").value("Updated Event"));

    verify(tenantEventService).updateEvent(any(EventUpdateDto.class));
  }

  @Test
  void registerEventSchedule_ShouldRegisterEventScheduleSuccessfully() throws Exception {
    Long eventId = 1L;
    EventScheduleDto scheduleDto =
        EventScheduleDto.builder().venueId(1L).ticketOpenAt(LocalDateTime.now().plusDays(1))
            .eventDate(LocalDateTime.now().plusDays(7)).build();

    String successMessage = "Event schedule registered successfully";

    when(tenantEventService.registerEventSchedule(eventId, scheduleDto)).thenReturn(successMessage);

    mockMvc
        .perform(post("/api/events/tenant/{eventId}/schedules", eventId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(scheduleDto)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(successMessage));

    verify(tenantEventService).registerEventSchedule(eventId, scheduleDto);
  }
}
