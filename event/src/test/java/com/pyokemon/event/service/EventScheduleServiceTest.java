package com.pyokemon.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.event.dto.BookingInfoResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.dto.PriceWithSeatClassDTO;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
class EventScheduleServiceTest {

  @Mock
  private EventScheduleRepository eventScheduleRepository;

  @Mock
  private PriceRepository priceRepository;

  @Mock
  private SeatRepository seatRepository;

  @InjectMocks
  private EventScheduleService eventScheduleService;

  private EventItemResponseDTO mockEventItem;
  private EventScheduleDto mockEventScheduleDto;
  private PriceDto mockPriceDto;
  private PriceWithSeatClassDTO mockPriceWithSeatClass;

  @BeforeEach
  void setUp() {
    mockEventItem = new EventItemResponseDTO();
    mockEventItem.setEventId(1L);
    mockEventItem.setTitle("Test Event");
    mockEventItem.setGenre("Pop");
    mockEventItem.setThumbnailUrl("test.jpg");

    mockEventScheduleDto = EventScheduleDto.builder().eventId(1L).venueId(1L)
        .ticketOpenAt(LocalDateTime.now()).eventDate(LocalDateTime.now().plusDays(7))
        .prices(Arrays.asList(PriceDto.builder().seatClassId(1L).price(50000).build())).build();

    mockPriceDto = PriceDto.builder().eventScheduleId(1L).seatClassId(1L).price(50000).build();

    mockPriceWithSeatClass =
        PriceWithSeatClassDTO.builder().seatClassId(1L).className("VIP").price(50000).build();
  }

  @Test
  void getTodayOpenedTickets_ShouldReturnTodayOpenedTickets() {
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
    when(eventScheduleRepository.selectTodayOpenedTickets()).thenReturn(expectedEvents);

    List<EventItemResponseDTO> result = eventScheduleService.getTodayOpenedTickets();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockEventItem, result.get(0));
    verify(eventScheduleRepository).selectTodayOpenedTickets();
  }

  @Test
  void getTicketsToBeOpened_ShouldReturnTicketsToBeOpened() {
    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
    when(eventScheduleRepository.selectTicketsToBeOpened()).thenReturn(expectedEvents);

    List<EventItemResponseDTO> result = eventScheduleService.getTicketsToBeOpened();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockEventItem, result.get(0));
    verify(eventScheduleRepository).selectTicketsToBeOpened();
  }

  @Test
  void getConcertsByPage_ShouldReturnConcertsWithTotalCount() {
    String genre = "Pop";
    int offset = 0;
    int limit = 9;
    int totalCount = 20;

    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
    when(eventScheduleRepository.selectEventList(genre, limit, offset)).thenReturn(expectedEvents);
    when(eventScheduleRepository.getTotalCountByGenre(genre)).thenReturn(totalCount);

    List<EventItemResponseDTO> result =
        eventScheduleService.getConcertsByPage(genre, offset, limit);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(totalCount, result.get(0).getTotal());
    verify(eventScheduleRepository).selectEventList(genre, limit, offset);
    verify(eventScheduleRepository).getTotalCountByGenre(genre);
  }

  @Test
  void getEventSearch_ShouldReturnSearchResultsWithTotalCount() {
    String keyword = "test";
    int offset = 0;
    int limit = 9;
    String genre = "Pop";
    int totalCount = 15;

    List<EventItemResponseDTO> expectedEvents = Arrays.asList(mockEventItem);
    when(eventScheduleRepository.selectEventSearchList(keyword, limit, offset, genre))
        .thenReturn(expectedEvents);
    when(eventScheduleRepository.getSearchTotalCount(keyword, genre)).thenReturn(totalCount);

    List<EventItemResponseDTO> result =
        eventScheduleService.getEventSearch(keyword, offset, limit, genre);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(totalCount, result.get(0).getTotal());
    verify(eventScheduleRepository).selectEventSearchList(keyword, limit, offset, genre);
    verify(eventScheduleRepository).getSearchTotalCount(keyword, genre);
  }

  @Test
  void registerEventSchedule_ShouldSaveEventScheduleAndPrices() {
    EventSchedule savedEventSchedule =
        EventSchedule.builder().eventScheduleId(1L).eventId(1L).venueId(1L).build();

    when(eventScheduleRepository.save(any(EventSchedule.class))).thenReturn(1L);
    when(priceRepository.save(any(Price.class))).thenReturn(1L);

    eventScheduleService.registerEventSchedule(mockEventScheduleDto);

    verify(eventScheduleRepository).save(any(EventSchedule.class));
    verify(priceRepository).save(any(Price.class));
  }

  @Test
  void registerEventSchedule_WithNoPrices_ShouldOnlySaveEventSchedule() {
    EventScheduleDto dtoWithoutPrices =
        EventScheduleDto.builder().eventId(1L).venueId(1L).ticketOpenAt(LocalDateTime.now())
            .eventDate(LocalDateTime.now().plusDays(7)).prices(null).build();

    EventSchedule savedEventSchedule =
        EventSchedule.builder().eventScheduleId(1L).eventId(1L).venueId(1L).build();

    when(eventScheduleRepository.save(any(EventSchedule.class))).thenReturn(1L);

    eventScheduleService.registerEventSchedule(dtoWithoutPrices);

    verify(eventScheduleRepository).save(any(EventSchedule.class));
    verify(priceRepository, never()).save(any(Price.class));
  }

  @Test
  void getBookingInfo_ShouldReturnBookingInfoWithSeatCounts() {
    Long eventScheduleId = 1L;
    Long venueId = 1L;
    Long seatCount = 100L;

    List<PriceWithSeatClassDTO> prices = Arrays.asList(mockPriceWithSeatClass);
    when(eventScheduleRepository.findVenueIdByEventScheduleId(eventScheduleId)).thenReturn(venueId);
    when(priceRepository.findPricesWithSeatClassByEventScheduleId(eventScheduleId))
        .thenReturn(prices);
    when(seatRepository.countByVenueIdAndSeatClassId(venueId,
        mockPriceWithSeatClass.getSeatClassId())).thenReturn(seatCount);

    List<BookingInfoResponseDTO> result = eventScheduleService.getBookingInfo(eventScheduleId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockPriceWithSeatClass.getSeatClassId(), result.get(0).getSeatClassId());
    assertEquals(mockPriceWithSeatClass.getClassName(), result.get(0).getSeatGrade());
    assertEquals(mockPriceWithSeatClass.getPrice(), result.get(0).getPrice());
    assertEquals(seatCount, result.get(0).getSeatCount());

    verify(eventScheduleRepository).findVenueIdByEventScheduleId(eventScheduleId);
    verify(priceRepository).findPricesWithSeatClassByEventScheduleId(eventScheduleId);
    verify(seatRepository).countByVenueIdAndSeatClassId(venueId,
        mockPriceWithSeatClass.getSeatClassId());
  }
}
