package com.pyokemon.event.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.GlobalExceptionHandler;
import com.pyokemon.event.dto.BookingInfoResponseDTO;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.service.EventScheduleService;
import com.pyokemon.event.service.EventService;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

  @Mock
  private EventService eventService;

  @Mock
  private EventScheduleService eventScheduleService;

  @InjectMocks
  private EventController eventController;

  private MockMvc mockMvc;

  private EventItemResponseDTO mockEventItem;
  private EventDetailResponseDTO mockEventDetail;
  private BookingInfoResponseDTO mockBookingInfo;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(eventController)
        .setControllerAdvice(new GlobalExceptionHandler()).build();

    mockEventItem = new EventItemResponseDTO();
    mockEventItem.setEventId(1L);
    mockEventItem.setTitle("Test Event");
    mockEventItem.setGenre("Pop");

    mockEventDetail = EventDetailResponseDTO.builder().eventId(1L).title("Test Event")
        .description("Test Description").genre("Pop").build();

    mockBookingInfo = BookingInfoResponseDTO.builder().seatClassId(1L).seatGrade("VIP").price(50000)
        .seatCount(100L).build();
  }

  @Test
  void getOpenTicketsToday_ShouldReturnTodayOpenedTickets() throws Exception {
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
    when(eventScheduleService.getTodayOpenedTickets()).thenReturn(expectedEvents);

    mockMvc.perform(get("/api/events/open-today")).andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].eventId").value(1))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventScheduleService).getTodayOpenedTickets();
  }

  @Test
  void getOpenTicketsToBeOpened_ShouldReturnTicketsToBeOpened() throws Exception {
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
    when(eventScheduleService.getTicketsToBeOpened()).thenReturn(expectedEvents);

    mockMvc.perform(get("/api/events/to-be-opened")).andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].eventId").value(1))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventScheduleService).getTicketsToBeOpened();
  }

  @Test
  void getEventDetail_ShouldReturnEventDetail() throws Exception {
    Long eventId = 1L;
    Long accountId = 1L;

    when(eventService.getEventDetail(eventId, accountId)).thenReturn(mockEventDetail);

    mockMvc.perform(get("/api/events/{eventId}", eventId).header("X-Auth-AccountId", accountId))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.eventId").value(1))
        .andExpect(jsonPath("$.title").value("Test Event"));

    verify(eventService).getEventDetail(eventId, accountId);
  }

  @Test
  void getEventDetail_WithoutAccountId_ShouldReturnEventDetail() throws Exception {
    Long eventId = 1L;

    when(eventService.getEventDetail(eventId, null)).thenReturn(mockEventDetail);

    mockMvc.perform(get("/api/events/{eventId}", eventId)).andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.eventId").value(1))
        .andExpect(jsonPath("$.title").value("Test Event"));

    verify(eventService).getEventDetail(eventId, null);
  }

  @Test
  void getEventDetail_WhenEventNotFound_ShouldReturnNotFound() throws Exception {
    Long eventId = 999L;
    Long accountId = 1L;

    when(eventService.getEventDetail(eventId, accountId))
        .thenThrow(new NotFoundException("Event not found"));

    mockMvc.perform(get("/api/events/{eventId}", eventId).header("X-Auth-AccountId", accountId))
        .andExpect(status().isNotFound());

    verify(eventService).getEventDetail(eventId, accountId);
  }

  @Test
  void getConcertsByPage_ShouldReturnConcertsByPage() throws Exception {
    String genre = "Pop";
    int page = 1;
    int size = 9;
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);

    when(eventScheduleService.getConcertsByPage(genre, 0, size)).thenReturn(expectedEvents);

    mockMvc
        .perform(get("/api/events").param("genre", genre).param("page", String.valueOf(page))
            .param("size", String.valueOf(size)))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].eventId").value(1))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventScheduleService).getConcertsByPage(genre, 0, size);
  }

  @Test
  void getConcertsByPage_WithDefaultParameters_ShouldReturnConcertsByPage() throws Exception {
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);

    when(eventScheduleService.getConcertsByPage("전체", 0, 9)).thenReturn(expectedEvents);

    mockMvc.perform(get("/api/events")).andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].eventId").value(1))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventScheduleService).getConcertsByPage("전체", 0, 9);
  }

  @Test
  void saveEvent_WhenEventExistsAndNotSaved_ShouldReturnSuccessMessage() throws Exception {
    Long eventId = 1L;
    Long accountId = 1L;
    String successMessage = "관심 공연으로 등록되었습니다";

    when(eventService.saveSavedEvent(accountId, eventId)).thenReturn(successMessage);

    mockMvc
        .perform(post("/api/events/save/{eventId}", eventId).header("X-Auth-AccountId", accountId))
        .andExpect(status().isOk()).andExpect(content().string(successMessage));

    verify(eventService).saveSavedEvent(accountId, eventId);
  }

  @Test
  void saveEvent_WhenEventExistsAndAlreadySaved_ShouldReturnDeleteMessage() throws Exception {
    Long eventId = 1L;
    Long accountId = 1L;
    String deleteMessage = "관심 공연에서 삭제되었습니다";

    when(eventService.saveSavedEvent(accountId, eventId)).thenReturn(deleteMessage);

    mockMvc
        .perform(post("/api/events/save/{eventId}", eventId).header("X-Auth-AccountId", accountId))
        .andExpect(status().isOk()).andExpect(content().string(deleteMessage));

    verify(eventService).saveSavedEvent(accountId, eventId);
  }

  @Test
  void getSavedEvents_ShouldReturnSavedEvents() throws Exception {
    Long accountId = 1L;
    int page = 1;
    int size = 9;
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);

    when(eventService.getSavedEvents(accountId, 0, size)).thenReturn(expectedEvents);

    mockMvc
        .perform(get("/api/events/saved-events").header("X-Auth-AccountId", accountId)
            .param("page", String.valueOf(page)).param("size", String.valueOf(size)))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].eventId").value(1))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventService).getSavedEvents(accountId, 0, size);
  }

  @Test
  void getSavedEvents_WithoutAccountId_ShouldThrowBusinessException() throws Exception {
    mockMvc.perform(get("/api/events/saved-events")).andExpect(status().isBadRequest());

    verify(eventService, never()).getSavedEvents(any(), anyInt(), anyInt());
  }

  @Test
  void getEventSearch_ShouldReturnSearchResults() throws Exception {
    String keyword = "test";
    int page = 1;
    int size = 9;
    String genre = "Pop";
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);

    when(eventScheduleService.getEventSearch(keyword, 0, size, genre)).thenReturn(expectedEvents);

    mockMvc
        .perform(
            get("/api/events/keyword").param("keyword", keyword).param("page", String.valueOf(page))
                .param("size", String.valueOf(size)).param("genre", genre))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].eventId").value(1))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventScheduleService).getEventSearch(keyword, 0, size, genre);
  }

  @Test
  void getBookingInfo_ShouldReturnBookingInfo() throws Exception {
    Long eventScheduleId = 1L;
    List<BookingInfoResponseDTO> expectedBookingInfo = Arrays.asList(mockBookingInfo);

    when(eventScheduleService.getBookingInfo(eventScheduleId)).thenReturn(expectedBookingInfo);

    mockMvc.perform(get("/api/events/booking-info/{eventScheduleId}", eventScheduleId))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].seatClassId").value(1))
        .andExpect(jsonPath("$[0].seatGrade").value("VIP"))
        .andExpect(jsonPath("$[0].price").value(50000))
        .andExpect(jsonPath("$[0].seatCount").value(100));

    verify(eventScheduleService).getBookingInfo(eventScheduleId);
  }
}
