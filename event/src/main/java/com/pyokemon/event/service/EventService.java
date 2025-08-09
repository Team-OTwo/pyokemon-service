package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.dto.*;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.*;
import com.pyokemon.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final VenueRepository venueRepository;
    private final PriceRepository priceRepository;
    private final SavedEventRepository savedEventRepository;

    // 공연 상세 조회
    public EventDetailResponseDTO getEventDetail(Long eventId, Long accountId)
            throws NotFoundException {
        EventDetailResponseDTO dto = eventRepository.findEventDetailByEventId(eventId);
        if (dto == null) {
            throw new NotFoundException("해당 공연을 찾을 수 없습니다.");
        }

        if (accountId != null) {
            boolean isSaved = savedEventRepository.existsByAccountIdAndEventId(accountId, eventId);
            dto.setSaved(isSaved);
        } else {
            // 비회원이면 관심공연 isSaved 다 false로 설정
            dto.setSaved(false);
        }
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
}
