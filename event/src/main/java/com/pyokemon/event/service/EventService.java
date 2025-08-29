package com.pyokemon.event.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.pyokemon.common.exception.BusinessException;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.event.dto.*;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.*;
import com.pyokemon.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

  private final EventRepository eventRepository;
  private final SavedEventRepository savedEventRepository;

  // 공연 상세 조회
  public EventDetailResponseDTO getEventDetail(Long eventId, Long accountId)
      throws NotFoundException {
    EventDetailResponseDTO dto = findEventDetailOrThrow(eventId);
    dto.setSeatPrice(findSeatPrices(dto.getEventScheduleId()));
    dto.setSaved(checkIfSaved(eventId, accountId));
    return dto;
  }

  // 관심공연 등록
  @Transactional
  public String saveSavedEvent(Long accountId, Long eventId) {
    Object event = eventRepository.findEventDetailByEventId(eventId);
    if (event == null) {
      return "존재하지 않는 공연입니다";
    }

    // 이미 관심 공연인지 확인
    boolean exists = savedEventRepository.existsByAccountIdAndEventId(accountId, eventId);

    SavedEvent savedEvent = new SavedEvent();
    savedEvent.setEventId(eventId);
    savedEvent.setAccountId(accountId);

    if (exists) {
      savedEventRepository.delete(accountId, eventId);
      return "관심 공연에서 삭제되었습니다";
    } else {
      savedEventRepository.save(savedEvent);
      return "관심 공연으로 등록되었습니다";
    }
  }

  // 관심 공연 조회
  public List<EventItemResponseDTO> getSavedEvents(Long accountId, int offset, int limit) {
    List<EventItemResponseDTO> events =
        savedEventRepository.findByAccountId(accountId, offset, limit);
    int total = savedEventRepository.countTotalEventsByAccountId(accountId);

    for (EventItemResponseDTO event : events) {
      event.setTotal(total);
    }

    return events;
  }


  private EventDetailResponseDTO findEventDetailOrThrow(Long eventId) throws NotFoundException {
    EventDetailResponseDTO dto = eventRepository.findEventDetailByEventId(eventId);
    if (dto == null) {
      throw new NotFoundException("해당 공연을 찾을 수 없습니다.");
    }
    return dto;
  }

  private List<SeatPriceResponseDto> findSeatPrices(Long eventScheduleId) {
    return eventRepository.findSeatPriceByEventScheduleId(eventScheduleId);
  }

  private boolean checkIfSaved(Long eventId, Long accountId) {
    if (accountId == null) {
      return false;
    }
    return savedEventRepository.existsByAccountIdAndEventId(accountId, eventId);
  }

  // 좌석 상세 정보 조회
  public SeatDetailResponseDTO getSeatDetail(Long eventScheduleId, Long seatId)
      throws NotFoundException {
    SeatDetailResponseDTO dto =
        eventRepository.findSeatDetailByEventScheduleIdAndSeatId(eventScheduleId, seatId);
    if (dto == null) {
      throw new NotFoundException("해당 좌석 정보를 찾을 수 없습니다.");
    }
    return dto;
  }

  @Transactional(readOnly = true)
  public EventInfoDto getEvent(Long id) {
    return eventRepository.findEventById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<EventInfoDto> getEvents(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return List.of();

    // DB에서 한 번에 조회
    List<EventInfoDto> rows = eventRepository.findEventsByIdIn(ids);

    // ID 기준 Map으로 정리
    Map<Long, EventInfoDto> byId =
            rows.stream().collect(Collectors.toMap(EventInfoDto::getEventId, Function.identity()));

    // 누락된 ID 체크
    List<Long> missing = ids.stream().filter(id -> !byId.containsKey(id)).distinct().toList();

    if (!missing.isEmpty()) {
      throw new BusinessException("이벤트 정보를 조회할 수 없습니다. ids=" + missing, "EVENT_NOT_FOUND");
    }

    // 요청 순서 / 중복 유지해서 반환
    return ids.stream().map(byId::get).toList();
  }

  @Transactional(readOnly = true)
  public List<Long> findEventIdsByGenre(String genre) {
    return eventRepository.findIdsByGenre(genre);
  }

  @Transactional(readOnly = true)
  public List<Long> findScheduleIdsByEventIds(List<Long> eventIds) {
    return eventRepository.findIdsByEventIds(eventIds);
  }

}
