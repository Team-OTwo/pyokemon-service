package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.TenantEventDetailResponseDTO;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.EventScheduleUpdateDto;
import com.pyokemon.event.dto.EventUpdateDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.dto.PriceUpdateDto;
import com.pyokemon.event.dto.TenantEventListDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

  private final EventRepository eventRepository;
  private final EventScheduleRepository eventScheduleRepository;
  private final VenueRepository venueRepository;
  private final PriceRepository priceRepository;


  public EventDetailResponseDTO getEventDetailByEventId(Long eventId) {
    return eventRepository.findEventDetailByEventId(eventId);
  }

  public TenantEventDetailResponseDTO getTenantEventDetailByEventId(Long eventId) {
    return eventRepository.findTenantEventDetailByEventId(eventId);
  }

  public List<EventResponseDto> getEventsByAccountId(Long accountId) {
    List<Event> events = eventRepository.findByAccountId(accountId);
    return events.stream().map(this::mapToEventResponseDto).toList();
  }

  public List<TenantEventListDto> getTenantEventListByAccountId(Long accountId) {
    return eventRepository.findTenantEventListByAccountId(accountId);
  }

  @Transactional
  public EventResponseDto updateEvent(EventUpdateDto eventUpdateDto) {
    // 기존 이벤트 존재 여부 확인
    Event existingEvent = findEventById(eventUpdateDto.getEventId());
    if (existingEvent == null) {
      throw new BusinessException("Event not found with id: " + eventUpdateDto.getEventId(),
          "EVENT_NOT_FOUND");
    }

    // 공연장 유효성 검사
    if (eventUpdateDto.getSchedules() != null && !eventUpdateDto.getSchedules().isEmpty()) {
      for (EventScheduleUpdateDto scheduleDto : eventUpdateDto.getSchedules()) {
        if (!validateVenueExists(scheduleDto.getVenueId())) {
          throw new BusinessException("Venue not found with id: " + scheduleDto.getVenueId(),
              "VENUE_NOT_FOUND");
        }
      }
    }

    // 이벤트 정보 업데이트
    updateEventInfo(existingEvent, eventUpdateDto);

    // 스케줄 및 가격 정보 업데이트
    if (eventUpdateDto.getSchedules() != null) {
      updateEventSchedules(eventUpdateDto.getEventId(), eventUpdateDto.getSchedules());
    }

    return mapToEventResponseDto(existingEvent);
  }

  private Event findEventById(Long eventId) {
    return eventRepository.findById(eventId);
  }

  private void updateEventInfo(Event event, EventUpdateDto updateDto) {
    event.setTitle(updateDto.getTitle());
    event.setAgeLimit(updateDto.getAgeLimit());
    event.setDescription(updateDto.getDescription());
    event.setGenre(updateDto.getGenre());
    event.setThumbnailUrl(updateDto.getThumbnailUrl());
    if (updateDto.getStatus() != null) {
      event.setStatus(updateDto.getStatus());
    }
    event.setUpdatedAt(LocalDateTime.now());

    // 이벤트 정보 저장
    eventRepository.updateEvent(event);
  }

  private void updateEventSchedules(Long eventId, List<EventScheduleUpdateDto> scheduleDtos) {
    for (EventScheduleUpdateDto scheduleDto : scheduleDtos) {
      if (scheduleDto.getEventScheduleId() != null) {
        // 기존 스케줄 업데이트
        updateExistingSchedule(scheduleDto);
      } else {
        // 새 스케줄 추가
        addNewSchedule(eventId, scheduleDto);
      }
    }
  }

  private void updateExistingSchedule(EventScheduleUpdateDto scheduleDto) {
    EventSchedule schedule = mapToEventScheduleForUpdate(scheduleDto);
    eventScheduleRepository.updateEventSchedule(schedule);

    // 가격 정보 업데이트
    if (scheduleDto.getPrices() != null) {
      updateSchedulePrices(scheduleDto.getEventScheduleId(), scheduleDto.getPrices());
    }
  }

  private void addNewSchedule(Long eventId, EventScheduleUpdateDto scheduleDto) {
    scheduleDto.setEventScheduleId(null); // 새 스케줄임을 명시
    EventSchedule newSchedule = mapToEventScheduleForUpdate(scheduleDto);
    newSchedule.setEventId(eventId);

    eventScheduleRepository.save(newSchedule);
    Long newScheduleId = newSchedule.getEventScheduleId();

    // 새 가격 정보 추가
    if (scheduleDto.getPrices() != null) {
      for (PriceUpdateDto priceDto : scheduleDto.getPrices()) {
        Price price = mapToPriceForUpdate(priceDto);
        price.setEventScheduleId(newScheduleId);
        priceRepository.save(price);
      }
    }
  }

  private void updateSchedulePrices(Long scheduleId, List<PriceUpdateDto> priceDtos) {
    for (PriceUpdateDto priceDto : priceDtos) {
      if (priceDto.getPriceId() != null) {
        // 기존 가격 업데이트
        Price price = mapToPriceForUpdate(priceDto);
        price.setEventScheduleId(scheduleId);
        priceRepository.updatePrice(price);
      } else {
        // 새 가격 추가
        Price newPrice = mapToPriceForUpdate(priceDto);
        newPrice.setEventScheduleId(scheduleId);
        priceRepository.save(newPrice);
      }
    }
  }

  private EventSchedule mapToEventScheduleForUpdate(EventScheduleUpdateDto dto) {
    return EventSchedule.builder().eventScheduleId(dto.getEventScheduleId())
        .venueId(dto.getVenueId()).ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate())
        .updatedAt(LocalDateTime.now()).build();
  }

  private Price mapToPriceForUpdate(PriceUpdateDto dto) {
    return Price.builder().priceId(dto.getPriceId()).seatClassId(dto.getSeatClassId())
        .price(dto.getPrice()).updatedAt(LocalDateTime.now()).build();
  }

  @Transactional
  public EventResponseDto registerEvent(EventRegisterDto eventRegisterDto) {

    // 공연장 유효성 검사
    if (eventRegisterDto.getSchedules() != null && !eventRegisterDto.getSchedules().isEmpty()) {
      for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
        if (!validateVenueExists(scheduleDto.getVenueId())) {
          throw new BusinessException("Venue not found with id: " + scheduleDto.getVenueId(),
              "VENUE_NOT_FOUND");
        }
      }
    }

    // 새로 추가된 공연 status PENDING으로 설정
    eventRegisterDto.setStatus(Event.EventStatus.PENDING);

    // 임시로 account_id를 1로 설정 (account 서비스 완료 전까지)
    eventRegisterDto.setAccountId(1L);

    // Create and save event
    Event event = mapToEvent(eventRegisterDto);
    Long eventId = saveEvent(event);
    event.setEventId(eventId);

    // Create and save schedules and prices if present
    if (eventRegisterDto.getSchedules() != null) {
      for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
        // 새로운 eventId를 설정
        scheduleDto.setEventId(eventId);

        // EventSchedule 생성 시 eventId를 직접 전달
        EventSchedule eventSchedule = EventSchedule.builder().eventId(eventId) // 직접 eventId 설정
            .venueId(scheduleDto.getVenueId()).ticketOpenAt(scheduleDto.getTicketOpenAt())
            .eventDate(scheduleDto.getEventDate()).createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now()).build();

        Long eventScheduleId = saveEventSchedule(eventSchedule);

        // Save prices if present
        if (scheduleDto.getPrices() != null) {
          for (PriceDto priceDto : scheduleDto.getPrices()) {
            priceDto.setEventScheduleId(eventScheduleId);
            Price price = mapToPrice(priceDto);
            savePrice(price);
          }
        }
      }
    }

    return mapToEventResponseDto(event);
  }

  private boolean validateVenueExists(Long venueId) {
    return venueRepository.findById(venueId).isPresent();
  }

  private Event mapToEvent(EventRegisterDto dto) {
    return Event.builder().accountId(dto.getAccountId()).title(dto.getTitle())
        .ageLimit(dto.getAgeLimit()).description(dto.getDescription()).genre(dto.getGenre())
        .thumbnailUrl(dto.getThumbnailUrl()).status(dto.getStatus()).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

  private EventSchedule mapToEventSchedule(EventScheduleDto dto, Long eventId) {
    System.out.println("mapToEventSchedule - dto.getEventId(): " + dto.getEventId()
        + ", passed eventId: " + eventId);
    EventSchedule eventSchedule = EventSchedule.builder().eventId(eventId).venueId(dto.getVenueId())
        .ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate())
        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    System.out
        .println("mapToEventSchedule - built eventSchedule.eventId: " + eventSchedule.getEventId());
    return eventSchedule;
  }

  private Price mapToPrice(PriceDto dto) {
    return Price.builder().eventScheduleId(dto.getEventScheduleId())
        .seatClassId(dto.getSeatClassId()).price(dto.getPrice()).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

  private EventResponseDto mapToEventResponseDto(Event event) {
    EventResponseDto responseDto = new EventResponseDto();
    responseDto.setEventId(event.getEventId());
    responseDto.setAccountId(event.getAccountId());
    responseDto.setTitle(event.getTitle());
    responseDto.setAgeLimit(event.getAgeLimit());
    responseDto.setDescription(event.getDescription());
    responseDto.setGenre(event.getGenre());
    responseDto.setThumbnailUrl(event.getThumbnailUrl());
    responseDto.setStatus(event.getStatus());
    responseDto.setCreatedAt(event.getCreatedAt());
    responseDto.setUpdatedAt(event.getUpdatedAt());
    return responseDto;
  }

  private Long saveEvent(Event event) {
    // save() 메서드는 영향받은 행의 수(1)를 반환
    eventRepository.save(event);
    // event 객체에는 생성된 ID가 설정되어 있음
    return event.getEventId();
  }

  private Long saveEventSchedule(EventSchedule eventSchedule) {
    // save() 메서드는 영향받은 행의 수(1)를 반환
    eventScheduleRepository.save(eventSchedule);
    // eventSchedule 객체에는 생성된 ID가 설정되어 있음
    return eventSchedule.getEventScheduleId();
  }

  private Long savePrice(Price price) {
    return priceRepository.save(price);

  }
}
