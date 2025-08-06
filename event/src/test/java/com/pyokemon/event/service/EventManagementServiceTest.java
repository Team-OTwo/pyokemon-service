package com.pyokemon.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.EventUpdateDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.dto.TenantEventDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventListDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.Event.EventStatus;
import com.pyokemon.event.entity.Venue;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.VenueRepository;

@ExtendWith(MockitoExtension.class)
class TenantEventManagementServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventScheduleRepository eventScheduleRepository;

  @Mock
  private VenueRepository venueRepository;

  @Mock
  private PriceRepository priceRepository;

  @InjectMocks
  private EventService eventService;

  private Long validAccountId = 1L;
  private Long validEventId = 1L;

  @BeforeEach
  void setUp() {
    // 테스트 설정
  }

  @Test
  @DisplayName("TenantEventListTest - 테넌트별 공연 목록 조회 성공")
  void getTenantEventListByAccountId_Success() {
    // given
    TenantEventListDto mockEvent = TenantEventListDto.builder().eventId(validEventId)
        .title("테스트 공연").eventDate(LocalDateTime.now().plusDays(30)).venueName("테스트 공연장")
        .status("APPROVED").build();

    when(eventRepository.findTenantEventListByAccountId(validAccountId))
        .thenReturn(List.of(mockEvent));

    // when
    List<TenantEventListDto> result = eventService.getTenantEventListByAccountId(validAccountId);

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("테스트 공연", result.get(0).getTitle());
    assertEquals("테스트 공연장", result.get(0).getVenueName());
    assertEquals("APPROVED", result.get(0).getStatus());
    verify(eventRepository).findTenantEventListByAccountId(validAccountId);
  }

  @Test
  @DisplayName("TenantEventDetailTest - 테넌트별 공연 상세 조회 성공")
  void getTenantEventDetailByEventId_Success() {
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

    when(eventRepository.findTenantEventDetailByEventId(validEventId)).thenReturn(mockDetail);

    // when
    TenantEventDetailResponseDTO result = eventService.getTenantEventDetailByEventId(validEventId);

    // then
    assertNotNull(result);
    assertEquals("테스트 공연", result.getTitle());
    assertEquals("테스트 공연장", result.getVenueName());
    assertEquals(2, result.getPrices().size());
    assertEquals("VIP", result.getPrices().get(0).getClassName());
    assertEquals(150000, result.getPrices().get(0).getPrice());
    assertEquals("R석", result.getPrices().get(1).getClassName());
    assertEquals(100000, result.getPrices().get(1).getPrice());
    verify(eventRepository).findTenantEventDetailByEventId(validEventId);
  }

  @Test
  @DisplayName("EventRegistTest - 공연 등록 성공")
  void registerEvent_Success() {
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

    Event mockEvent = Event.builder().eventId(validEventId).accountId(validAccountId)
        .title("새로운 공연").status(EventStatus.PENDING).build();

    when(venueRepository.findById(1L)).thenReturn(java.util.Optional.of(new Venue()));
    when(eventRepository.save(any(Event.class))).thenReturn(1);
    when(eventScheduleRepository.save(any())).thenReturn(1);
    when(priceRepository.save(any())).thenReturn(1L);

    // when
    EventResponseDto result = eventService.registerEvent(registerDto);

    // then
    assertNotNull(result);
    assertEquals("새로운 공연", result.getTitle());
    assertEquals(EventStatus.PENDING, result.getStatus());
    verify(eventRepository).save(any(Event.class));
    verify(eventScheduleRepository).save(any());
    verify(priceRepository, times(2)).save(any());
  }

  @Test
  @DisplayName("EventEditTest - 공연 수정 성공")
  void updateEvent_Success() {
    // given
    EventUpdateDto updateDto = EventUpdateDto.builder().eventId(validEventId).title("수정된 공연")
        .ageLimit(15L).description("수정된 공연 설명").genre("연극")
        .thumbnailUrl("https://example.com/updated-image.jpg").status(EventStatus.APPROVED).build();

    Event existingEvent = Event.builder().eventId(validEventId).accountId(validAccountId)
        .title("기존 공연").status(EventStatus.PENDING).build();

    when(eventRepository.findById(validEventId)).thenReturn(existingEvent);
    when(eventRepository.updateEvent(any(Event.class))).thenReturn(1);

    // when
    EventResponseDto result = eventService.updateEvent(updateDto);

    // then
    assertNotNull(result);
    assertEquals("수정된 공연", result.getTitle());
    assertEquals(EventStatus.APPROVED, result.getStatus());
    verify(eventRepository).findById(validEventId);
    verify(eventRepository).updateEvent(any(Event.class));
  }
}
