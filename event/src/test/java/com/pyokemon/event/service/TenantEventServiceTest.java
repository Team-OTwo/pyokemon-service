package com.pyokemon.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.tenant.EventRegisterDto;
import com.pyokemon.event.dto.tenant.EventResponseDto;
import com.pyokemon.event.dto.tenant.EventScheduleDto;
import com.pyokemon.event.dto.tenant.EventUpdateDto;
import com.pyokemon.event.dto.tenant.PriceDto;
import com.pyokemon.event.dto.tenant.TenantEventListDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.entity.Venue;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.TenantEventRepository;
import com.pyokemon.event.repository.VenueRepository;

@ExtendWith(MockitoExtension.class)
class TenantEventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private TenantEventRepository tenantEventRepository;

  @Mock
  private EventScheduleRepository eventScheduleRepository;

  @Mock
  private VenueRepository venueRepository;

  @Mock
  private PriceRepository priceRepository;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private TenantEventService tenantEventService;

  private EventRegisterDto mockEventRegisterDto;
  private EventUpdateDto mockEventUpdateDto;
  private EventScheduleDto mockEventScheduleDto;
  private EventDetailResponseDTO mockTenantEventDetail;
  private TenantEventListDto mockTenantEventList;
  private Event mockEvent;
  private Venue mockVenue;

  @BeforeEach
  void setUp() {
    mockEventRegisterDto = EventRegisterDto.builder().accountId(1L).title("Test Event")
        .ageLimit(19L).description("Test Description").genre("Pop").thumbnailUrl("test.jpg")
        .schedules(Arrays.asList(
            EventScheduleDto.builder().venueId(1L).ticketOpenAt(LocalDateTime.now().plusDays(1))
                .eventDate(LocalDateTime.now().plusDays(7))
                .prices(Arrays.asList(PriceDto.builder().seatClassId(1L).price(50000).build()))
                .build()))
        .build();

    mockEventUpdateDto = EventUpdateDto.builder().eventId(1L).title("Updated Event").ageLimit(19L)
        .description("Updated Description").genre("Rock").thumbnailUrl("updated.jpg").build();

    mockEventScheduleDto = EventScheduleDto.builder().eventId(1L).venueId(1L)
        .ticketOpenAt(LocalDateTime.now().plusDays(1)).eventDate(LocalDateTime.now().plusDays(7))
        .prices(Arrays.asList(PriceDto.builder().seatClassId(1L).price(50000).build())).build();

    mockTenantEventDetail = new EventDetailResponseDTO();
    mockTenantEventDetail.setEventId(1L);
    mockTenantEventDetail.setTitle("Test Event");



    mockTenantEventList = new TenantEventListDto();
    mockTenantEventList.setEventId(1L);
    mockTenantEventList.setTitle("Test Event");

    mockEvent = Event.builder().eventId(1L).accountId(1L).title("Test Event").ageLimit(19L)
        .description("Test Description").genre("Pop").thumbnailUrl("test.jpg")
        .status(Event.EventStatus.APPROVED).build();

    mockVenue = Venue.builder().venueId(1L).venueName("Test Venue").build();
  }

  @Test
  void getTenantEventDetailByEventId_ShouldReturnTenantEventDetail() {
    Long eventId = 1L;

    when(tenantEventRepository.findTenantEventDetailByEventId(eventId))
        .thenReturn(mockTenantEventDetail);

    EventDetailResponseDTO result = tenantEventService.getTenantEventDetailByEventId(eventId);

    assertNotNull(result);
    assertEquals(mockTenantEventDetail.getEventId(), result.getEventId());
    verify(tenantEventRepository).findTenantEventDetailByEventId(eventId);
  }

  @Test
  void getTenantEventListByAccountId_ShouldReturnTenantEventList() {
    Long accountId = 1L;
    List<TenantEventListDto> expectedEvents = Arrays.asList(mockTenantEventList);

    when(tenantEventRepository.findTenantEventListByAccountId(accountId))
        .thenReturn(expectedEvents);

    List<TenantEventListDto> result = tenantEventService.getTenantEventListByAccountId(accountId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockTenantEventList.getEventId(), result.get(0).getEventId());
    verify(tenantEventRepository).findTenantEventListByAccountId(accountId);
  }



  @Test
  void registerEvent_ShouldRegisterEventSuccessfully() {
    Long accountId = 1L;

    when(venueRepository.findById(1L)).thenReturn(Optional.of(mockVenue));
    when(tenantEventRepository.save(any(Event.class))).thenReturn(1L);
    when(eventScheduleRepository.save(any(EventSchedule.class))).thenReturn(1L);
    when(priceRepository.save(any(Price.class))).thenReturn(1L);

    EventResponseDto result = tenantEventService.registerEvent(mockEventRegisterDto, accountId);

    assertNotNull(result);
    assertEquals(mockEventRegisterDto.getTitle(), result.getTitle());
    verify(tenantEventRepository).save(any(Event.class));
    verify(eventScheduleRepository).save(any(EventSchedule.class));
    verify(priceRepository).save(any(Price.class));
  }

  @Test
  void registerEvent_WithInvalidVenue_ShouldThrowBusinessException() {
    Long accountId = 1L;

    when(venueRepository.findById(999L)).thenReturn(Optional.empty());

    mockEventRegisterDto.getSchedules().get(0).setVenueId(999L);

    assertThrows(BusinessException.class, () -> {
      tenantEventService.registerEvent(mockEventRegisterDto, accountId);
    });

    verify(venueRepository).findById(999L);
  }

  @Test
    void updateEvent_ShouldUpdateEventSuccessfully() {
        when(tenantEventRepository.findTenantEventDetailByEventId(1L)).thenReturn(mockTenantEventDetail);
        when(objectMapper.convertValue(any(), eq(Event.class))).thenReturn(mockEvent);
        when(tenantEventRepository.updateEvent(any(Event.class))).thenReturn(1);

        EventResponseDto result = tenantEventService.updateEvent(mockEventUpdateDto);

        assertNotNull(result);
        assertEquals(mockEventUpdateDto.getTitle(), result.getTitle());
        verify(tenantEventRepository).updateEvent(any(Event.class));
    }

  @Test
    void updateEvent_WhenEventNotFound_ShouldThrowBusinessException() {
        when(tenantEventRepository.findTenantEventDetailByEventId(999L)).thenReturn(null);

        mockEventUpdateDto.setEventId(999L);

        assertThrows(BusinessException.class, () -> {
            tenantEventService.updateEvent(mockEventUpdateDto);
        });
    }

  @Test
  void registerEventSchedule_ShouldRegisterEventScheduleSuccessfully() {
    Long eventId = 1L;

    when(eventScheduleRepository.save(any(EventSchedule.class))).thenReturn(1L);
    when(priceRepository.save(any(Price.class))).thenReturn(1L);

    String result = tenantEventService.registerEventSchedule(eventId, mockEventScheduleDto);

    assertEquals("Event schedule registered successfully", result);
    verify(eventScheduleRepository).save(any(EventSchedule.class));
    verify(priceRepository).save(any(Price.class));
  }
}
