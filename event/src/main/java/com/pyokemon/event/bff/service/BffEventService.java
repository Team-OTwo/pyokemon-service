package com.pyokemon.event.bff.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;
import com.pyokemon.event.bff.dto.*;
import com.pyokemon.event.bff.repository.BffEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BffEventService {
    private final BffEventRepository repo;

    public BffEventScheduleDto getEventSchedule(Long id) {
        return repo.findEventScheduleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EVENT_SCHEDULE_NOT_FOUND"));
    }
    public BffVenueDto getVenue(Long id) {
        return repo.findVenueById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND"));
    }
    public BffSeatDto getSeat(Long id) {
        return repo.findSeatById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND"));
    }
    public List<BffSeatDto> getSeats(List<Long> ids){
        if (ids == null || ids.isEmpty()) return List.of();

        List<BffSeatDto> rows = repo.findSeatsByIdIn(ids);
        Map<Long, BffSeatDto> byId = rows.stream()
                .collect(Collectors.toMap(BffSeatDto::getSeatId, Function.identity()));

        // 하나라도 없으면 예외를 던질지, 있는 것만 쓸지는 정책대로
        List<Long> missing = ids.stream().filter(id -> !byId.containsKey(id)).distinct().toList();
        if (!missing.isEmpty()) {
            throw new BusinessException("좌석을 찾을 수 없습니다. ids=" + missing, EventErrorCodes.SEAT_NOT_FOUND);
        }

        // 요청 순서/중복 그대로 복원
        return ids.stream().map(byId::get).toList();
    }

    public BffEventDto getEvent(Long id) {
        return repo.findEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND"));
    }
    public BffSeatClassDto getSeatClass(Long id) {
        return repo.findSeatClassById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SEAT_CLASS_NOT_FOUND"));
    }

    public List<BffSeatClassDto> getSeatClasses(List<Long> seatClassIds) {
        if (seatClassIds == null || seatClassIds.isEmpty()) return List.of();

        List<BffSeatClassDto> rows = repo.findSeatClassesByIdIn(seatClassIds);
        Map<Long, BffSeatClassDto> byId = rows.stream()
                .collect(Collectors.toMap(BffSeatClassDto::getSeatClassId, Function.identity()));

        // 누락 처리: 기존 정책에 맞춰서 예외 or skip
        List<Long> missing = seatClassIds.stream().filter(id -> !byId.containsKey(id)).distinct().toList();
        if (!missing.isEmpty()) {
            throw new BusinessException("좌석 등급을 찾을 수 없습니다. ids=" + missing,
                    EventErrorCodes.SEAT_CLASS_NOT_FOUND);
        }

        // 요청 순서/중복 그대로 반환
        return seatClassIds.stream().map(byId::get).toList();
    }
}

