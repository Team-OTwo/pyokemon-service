package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pyokemon.event.dto.BookingInfoResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.dto.PriceWithSeatClassDTO;
import com.pyokemon.event.dto.SeatInfoResponseDTO;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.SeatClassRepository;
import com.pyokemon.event.repository.SeatRepository;
import com.pyokemon.event.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventScheduleService {

  private final EventScheduleRepository eventScheduleRepository;
  private final PriceRepository priceRepository;
  private final SeatRepository seatRepository;
  private final SeatClassRepository seatClassRepository;
  private final RedisService redisService;

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
    EventSchedule eventSchedule = mapToEventSchedule(eventScheduleDto);
    eventScheduleRepository.save(eventSchedule);
    Long eventScheduleId = eventSchedule.getEventScheduleId();

    redisService.initSeatStatuses(eventScheduleId, eventScheduleDto.getVenueId());

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

  public List<BookingInfoResponseDTO> getBookingInfo(Long eventScheduleId) {
    Long venueId = eventScheduleRepository.findVenueIdByEventScheduleId(eventScheduleId);

    List<PriceWithSeatClassDTO> prices =
        priceRepository.findPricesWithSeatClassByEventScheduleId(eventScheduleId);

    return prices.stream().map(price -> {
      Long seatCount = seatRepository.countByVenueIdAndSeatClassId(venueId, price.getSeatClassId());
      return BookingInfoResponseDTO.builder().seatClassId(price.getSeatClassId())
          .seatGrade(price.getClassName()).price(price.getPrice()).seatCount(seatCount).build();
    }).toList();
  }

  public List<Long> getSeatIdsByGrade(String seatGradeName) {
    SeatClass seatClass = seatClassRepository.findByClassName(seatGradeName)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 등급입니다: " + seatGradeName));

    List<Seat> seats = seatRepository.findBySeatClassId(seatClass.getSeatClassId());

    return seats.stream().map(Seat::getSeatId).collect(Collectors.toList());
  }

  public List<SeatInfoResponseDTO> getSeatInfoByGrade(Long eventScheduleId, String seatGradeName) {
    Long venueId = eventScheduleRepository.findVenueIdByEventScheduleId(eventScheduleId);

    SeatClass seatClass = seatClassRepository.findByClassName(seatGradeName)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 등급입니다: " + seatGradeName));

    List<Seat> seats =
        seatRepository.findByVenueIdAndSeatClassId(venueId, seatClass.getSeatClassId());

    return seats.stream()
        .map(seat -> SeatInfoResponseDTO.builder().seatId(seat.getSeatId()).col(seat.getCol())
            .row(seat.getRow()).seatGrade(seatClass.getClassName()).build())
        .collect(Collectors.toList());
  }

}
