package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;
import com.pyokemon.event.dto.*;
import com.pyokemon.event.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pyokemon.event.dto.tenant.EventScheduleDto;
import com.pyokemon.event.dto.tenant.PriceDto;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.producer.KafkaMessageProducer;
import com.pyokemon.event.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventScheduleService {

  private final EventScheduleRepository eventScheduleRepository;
  private final PriceRepository priceRepository;
  private final SeatRepository seatRepository;
  private final SeatClassRepository seatClassRepository;
  private final VenueRepository venueRepository;
  private final RedisService redisService;
  private final KafkaMessageProducer kafkaMessageProducer;

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


  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void publishTwoHoursAheadEvents() {
    List<Long> upcomingIds = eventScheduleRepository.findEventScheduleIdTwoHoursLater();
    for (Long id : upcomingIds) {
      log.info("Publishing eventScheduleId={} to Kafka", id);
      kafkaMessageProducer.sendTwoHoursBeforeEvent(id);
    }
  }

  @Transactional(readOnly = true)
  public EventScheduleInfoDto getEventSchedule(Long id) {
    return eventScheduleRepository.findEventScheduleById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EVENT_SCHEDULE_NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<EventScheduleInfoDto> getEventSchedules(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return List.of();
    List<EventScheduleInfoDto> rows = eventScheduleRepository.findEventSchedulesByIdIn(ids);
    Map<Long, EventScheduleInfoDto> byId = rows.stream()
            .collect(Collectors.toMap(EventScheduleInfoDto::getEventScheduleId, Function.identity()));
    List<Long> missing = ids.stream().filter(id -> !byId.containsKey(id)).distinct().toList();

    if (!missing.isEmpty()) {
      throw new BusinessException("일정 정보를 조회할 수 없습니다. ids=" + missing, "SCHEDULE_NOT_FOUND");
    }

    return ids.stream().map(byId::get).toList();
  }

  @Transactional(readOnly = true)
  public VenueInfoDto getVenue(Long id) {
    return venueRepository.findVenueById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<VenueInfoDto> getVenues(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return List.of();

    List<VenueInfoDto> rows = venueRepository.findVenuesByIdIn(ids);

    Map<Long, VenueInfoDto> byId =
            rows.stream().collect(Collectors.toMap(VenueInfoDto::getVenueId, Function.identity()));

    List<Long> missing = ids.stream().filter(id -> !byId.containsKey(id)).distinct().toList();

    if (!missing.isEmpty()) {
      throw new BusinessException("공연장 정보를 조회할 수 없습니다. ids=" + missing, "VENUE_NOT_FOUND");
    }

    return ids.stream().map(byId::get).toList();

  }

  @Transactional(readOnly = true)
  public SeatInfoDto getSeat(Long id) {
    return seatRepository.findSeatById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<SeatInfoDto> getSeats(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return List.of();

    List<SeatInfoDto> rows = seatRepository.findSeatsByIdIn(ids);
    Map<Long, SeatInfoDto> byId =
            rows.stream().collect(Collectors.toMap(SeatInfoDto::getSeatId, Function.identity()));

    // 하나라도 없으면 예외를 던질지, 있는 것만 쓸지는 정책대로
    List<Long> missing = ids.stream().filter(id -> !byId.containsKey(id)).distinct().toList();
    if (!missing.isEmpty()) {
      throw new BusinessException("좌석을 찾을 수 없습니다. ids=" + missing, EventErrorCodes.SEAT_NOT_FOUND);
    }

    // 요청 순서/중복 그대로 복원
    return ids.stream().map(byId::get).toList();
  }

  @Transactional(readOnly = true)
  public SeatClassInfoDto getSeatClass(Long id) {
    return seatClassRepository.findSeatClassById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SEAT_CLASS_NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<SeatClassInfoDto> getSeatClasses(List<Long> seatClassIds) {
    if (seatClassIds == null || seatClassIds.isEmpty())
      return List.of();

    List<SeatClassInfoDto> rows = seatClassRepository.findSeatClassesByIdIn(seatClassIds);
    Map<Long, SeatClassInfoDto> byId = rows.stream()
            .collect(Collectors.toMap(SeatClassInfoDto::getSeatClassId, Function.identity()));

    // 누락 처리: 기존 정책에 맞춰서 예외 or skip
    List<Long> missing =
            seatClassIds.stream().filter(id -> !byId.containsKey(id)).distinct().toList();
    if (!missing.isEmpty()) {
      throw new BusinessException("좌석 등급을 찾을 수 없습니다. ids=" + missing,
              EventErrorCodes.SEAT_CLASS_NOT_FOUND);
    }

    // 요청 순서/중복 그대로 반환
    return seatClassIds.stream().map(byId::get).toList();
  }

  @Transactional(readOnly = true)
  public List<ScheduleDetailDto> getScheduleDetailsByIds(List<Long> scheduleIds) {
    if (scheduleIds == null || scheduleIds.isEmpty()) {
      return List.of();
    }
    // DB에서 JOIN된 결과를 한 번에 조회
    List<ScheduleDetailDto> rows = eventScheduleRepository.findScheduleDetailsByIds(scheduleIds);
    // ID 기준 Map으로 정리 (누락된 ID 체크 및 순서 보장용)
    Map<Long, ScheduleDetailDto> byId = rows.stream()
            .collect(Collectors.toMap(ScheduleDetailDto::getEventScheduleId, Function.identity()));

    // 요청한 ID 중 DB에 없는 ID가 있는지 확인
    List<Long> missing =
            scheduleIds.stream().filter(id -> !byId.containsKey(id)).distinct().toList();
    if (!missing.isEmpty()) {
      throw new BusinessException("일부 일정 정보를 조회할 수 없습니다. ids=" + missing, "SCHEDULE_DETAILS_NOT_FOUND");
    }

    // 요청받은 ID 목록의 순서와 중복을 그대로 유지하여 반환
    return scheduleIds.stream().map(byId::get).toList();
  }

}
