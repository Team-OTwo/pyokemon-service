package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventScheduleService {

  private final EventScheduleRepository eventScheduleRepository;
  private final PriceRepository priceRepository;
  private final SeatRepository seatRepository;

  public List<EventItemResponseDTO> getTodayOpenedTickets() {
    return eventScheduleRepository.selectTodayOpenedTickets();
  }

  public List<EventItemResponseDTO> getTicketsToBeOpened() {
    return eventScheduleRepository.selectTicketsToBeOpened();
  }

  public List<EventItemResponseDTO> getConcertsByPage(String genre, int offset, int limit) {
    List<EventItemResponseDTO> events =
        eventScheduleRepository.selectEventList(genre, limit, offset);
    int total = eventScheduleRepository.getTotalCountByGenre(genre);

    for (EventItemResponseDTO event : events) {
      event.setTotal(total);
    }

    return events;
  }

  public List<EventItemResponseDTO> getEventSearch(String keyword, int offset, int limit,
      String genre) {
    List<EventItemResponseDTO> events =
        eventScheduleRepository.selectEventSearchList(keyword, limit, offset, genre);
    int total = eventScheduleRepository.getSearchTotalCount(keyword, genre);

    for (EventItemResponseDTO event : events) {
      event.setTotal(total);
    }

    return events;
  }
  
  public void registerEventSchedule(EventScheduleDto eventScheduleDto) {
    // Create and save event schedule
    EventSchedule eventSchedule = mapToEventSchedule(eventScheduleDto);
    eventScheduleRepository.save(eventSchedule);
    Long eventScheduleId = eventSchedule.getEventScheduleId();

    // Save prices if present
    if (eventScheduleDto.getPrices() != null) {
      for (PriceDto priceDto : eventScheduleDto.getPrices()) {
        priceDto.setEventScheduleId(eventScheduleId);
        Price price = mapToPrice(priceDto);
        priceRepository.save(price);
      }
    }
  }

  private EventSchedule mapToEventSchedule(EventScheduleDto dto) {
    EventSchedule eventSchedule = EventSchedule.builder().eventId(dto.getEventId())
        .venueId(dto.getVenueId()).ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate())
        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    return eventSchedule;
  }

  private Price mapToPrice(PriceDto dto) {
    return Price.builder().eventScheduleId(dto.getEventScheduleId())
        .seatClassId(dto.getSeatClassId()).price(dto.getPrice()).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

  /**
   * 예매 정보 조회 - 좌석 등급별 가격과 좌석 개수 정보
   */
  public List<BookingInfoResponseDTO> getBookingInfo(Long eventScheduleId) {
    // 1. 해당 이벤트 스케줄의 venueId 조회
    Long venueId = eventScheduleRepository.findVenueIdByEventScheduleId(eventScheduleId);
    
    // 2. 해당 이벤트 스케줄의 좌석 등급별 가격 정보 조회
    List<PriceWithSeatClassDTO> prices = priceRepository.findPricesWithSeatClassByEventScheduleId(eventScheduleId);
    
    // 3. 각 등급별 좌석 개수 조회하여 BookingInfoResponseDTO 리스트 생성
    return prices.stream()
        .map(price -> {
          Long seatCount = seatRepository.countByVenueIdAndSeatClassId(venueId, price.getSeatClassId());
          return BookingInfoResponseDTO.builder()
              .seatClassId(price.getSeatClassId())
              .seatGrade(price.getClassName())
              .price(price.getPrice())
              .seatCount(seatCount)
              .build();
        })
        .toList();
  }

}
