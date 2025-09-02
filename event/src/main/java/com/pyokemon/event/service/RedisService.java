package com.pyokemon.event.service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.event.client.BookingServiceClient;
import com.pyokemon.event.dto.BookingStatusResponse;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.SeatClassRepository;
import com.pyokemon.event.repository.SeatRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisService {

  private final RedisTemplate<String, String> redis;
  private final SeatRepository seatRepository;
  private final SeatClassRepository seatClassRepository;
  private final EventScheduleRepository eventScheduleRepository;
  private final BookingServiceClient bookingServiceClient;

  public RedisService(@Qualifier("redisTemplate") RedisTemplate<String, String> redis,
      SeatRepository seatRepository, SeatClassRepository seatClassRepository,
      EventScheduleRepository eventScheduleRepository, BookingServiceClient bookingServiceClient) {
    this.redis = redis;
    this.seatRepository = seatRepository;
    this.seatClassRepository = seatClassRepository;
    this.eventScheduleRepository = eventScheduleRepository;
    this.bookingServiceClient = bookingServiceClient;
  }

  private static final String SEAT_CLASS_STATUS_KEY_PATTERN = "seat:class:status:%d:%s";
  private static final String VENUE_SCHEDULE_KEY_PATTERN = "venue:schedule:%d:%d";
  private static final String SEAT_HOLD_KEY_PATTERN = "seat:hold:%d:%d";

  public void initSeatStatuses(Long scheduleId, Long venueId) {

    try {
      String venueScheduleKey = String.format(VENUE_SCHEDULE_KEY_PATTERN, venueId, scheduleId);
      redis.opsForValue().set(venueScheduleKey, "active", Duration.ofDays(30));

      List<Seat> seats = seatRepository.findByVenueId(venueId);
      if (seats.isEmpty()) {
        log.warn("해당 venue에 좌석이 없습니다: venueId={}", venueId);
        return;
      }

      Map<Long, List<Seat>> seatsByClass =
          seats.stream().collect(Collectors.groupingBy(Seat::getSeatClassId));

      for (Map.Entry<Long, List<Seat>> entry : seatsByClass.entrySet()) {
        Long seatClassId = entry.getKey();
        List<Seat> classSeats = entry.getValue();

        Optional<SeatClass> seatClassOpt = seatClassRepository.findById(seatClassId);
        if (seatClassOpt.isEmpty()) {
          log.warn("SeatClass를 찾을 수 없습니다: seatClassId={}", seatClassId);
          continue;
        }

        String className = seatClassOpt.get().getClassName();
        String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);

        Long existingSize = redis.opsForHash().size(classKey);
        if (existingSize != null && existingSize > 0) {
          log.info("기존 Redis 데이터 삭제: scheduleId={}, className={}, existingSeats={}", scheduleId,
              className, existingSize);
          redis.delete(classKey);
        }

        Map<String, String> initMap = new LinkedHashMap<>();
        for (Seat seat : classSeats) {
          initMap.put(String.valueOf(seat.getSeatId()), "");
        }

        redis.opsForHash().putAll(classKey, initMap);
      }

      restoreBookingStatusFromBookingService(scheduleId);
    } catch (Exception e) {
      log.error("좌석 상태 초기화 실패: scheduleId={}, venueId={}, error={}", scheduleId, venueId,
          e.getMessage(), e);
      throw new RuntimeException("좌석 상태 초기화에 실패했습니다: " + e.getMessage(), e);
    }
  }

  private void restoreBookingStatusFromBookingService(Long scheduleId) {
    try {
      BookingStatusResponse bookingResponse =
          bookingServiceClient.getBookingStatusByEventScheduleId(scheduleId);
      if (bookingResponse == null || bookingResponse.getSeatStatusInfos() == null) {
        log.warn("Booking 서비스에서 예매 상태를 가져올 수 없습니다: scheduleId={}", scheduleId);
        return;
      }

      for (BookingStatusResponse.SeatStatusInfo seatInfo : bookingResponse.getSeatStatusInfos()) {
        try {
          Long seatId = seatInfo.getSeatId();
          String status = seatInfo.getStatus();

          if ("BOOKED".equals(status)) {
            updateSeatStatusByClassName(scheduleId, seatId, "BOOKED");
          } else if ("PENDING".equals(status)) {
            holdSeat(scheduleId, seatId, 0L, 300);
          }
        } catch (Exception e) {
          log.error("개별 좌석 상태 복원 실패: scheduleId={}, seatId={}, status={}, error={}", scheduleId,
              seatInfo.getSeatId(), seatInfo.getStatus(), e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      log.error("Booking 서비스에서 예매 상태 복원 실패: scheduleId={}, error={}", scheduleId, e.getMessage(),
          e);
    }
  }

  public Map<String, Map<String, String>> getAllSeatStatusesBySeatClass(Long scheduleId) {
    try {
      Map<String, Map<String, String>> result = new LinkedHashMap<>();

      Long venueId = getVenueIdByScheduleId(scheduleId);
      List<SeatClass> seatClasses = seatClassRepository.findByVenueId(venueId);

      String holdKeyPattern = String.format("seat:hold:%d:*", scheduleId);
      Set<String> holdKeys = redis.keys(holdKeyPattern);
      Map<String, String> holdStatuses = new HashMap<>();

      if (!holdKeys.isEmpty()) {
        List<String> holdValues = redis.opsForValue().multiGet(holdKeys);
        for (int i = 0; i < holdKeys.size(); i++) {
          String key = holdKeys.toArray(new String[0])[i];
          String value = holdValues.get(i);
          if (value != null) {
            String seatId = key.substring(key.lastIndexOf(":") + 1);
            holdStatuses.put(seatId, "HELD");
          }
        }
      }

      for (SeatClass seatClass : seatClasses) {
        String className = seatClass.getClassName();
        String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);

        Map<Object, Object> statusMap = redis.opsForHash().entries(classKey);
        if (!statusMap.isEmpty()) {
          Map<String, String> classStatuses = new LinkedHashMap<>();
          for (Map.Entry<Object, Object> entry : statusMap.entrySet()) {
            String seatId = (String) entry.getKey();
            String status = (String) entry.getValue();

            if (holdStatuses.containsKey(seatId)) {
              classStatuses.put(seatId, "HELD");
            } else {
              classStatuses.put(seatId, status != null ? status : "");
            }
          }
          result.put(className, classStatuses);
        }
      }

      return result;
    } catch (Exception e) {
      log.error("좌석 클래스별 상태 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      throw new RuntimeException("좌석 클래스별 상태 조회에 실패했습니다: " + e.getMessage(), e);
    }
  }

  public Map<String, String> getSeatStatusesBySeatClassName(Long scheduleId, String seatClassName) {
    log.info("좌석 클래스별 상태 조회: scheduleId={}, seatClassName={}", scheduleId, seatClassName);

    try {
      String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, seatClassName);
      Map<Object, Object> statusMap = redis.opsForHash().entries(classKey);

      if (statusMap.isEmpty()) {
        return new LinkedHashMap<>();
      }

      String holdKeyPattern = String.format("seat:hold:%d:*", scheduleId);
      Set<String> holdKeys = redis.keys(holdKeyPattern);
      Map<String, String> holdStatuses = new HashMap<>();

      if (!holdKeys.isEmpty()) {
        List<String> holdValues = redis.opsForValue().multiGet(holdKeys);
        for (int i = 0; i < holdKeys.size(); i++) {
          String key = holdKeys.toArray(new String[0])[i];
          String value = holdValues.get(i);
          if (value != null) {
            String seatId = key.substring(key.lastIndexOf(":") + 1);
            holdStatuses.put(seatId, "HOLD");
          }
        }
      }

      Map<String, String> result = new LinkedHashMap<>();
      for (Map.Entry<Object, Object> entry : statusMap.entrySet()) {
        String seatId = (String) entry.getKey();
        String status = (String) entry.getValue();

        if (holdStatuses.containsKey(seatId)) {
          result.put(seatId, "HOLD");
        } else {
          result.put(seatId, status != null ? status : "");
        }
      }

      return result;
    } catch (Exception e) {
      log.error("좌석 클래스별 상태 조회 실패: scheduleId={}, seatClassName={}, error={}", scheduleId,
          seatClassName, e.getMessage(), e);
      throw new RuntimeException("좌석 클래스별 상태 조회에 실패했습니다: " + e.getMessage(), e);
    }
  }

  public void updateSeatStatusByClassName(Long scheduleId, Long seatId, String status) {
    try {
      List<Seat> seats = seatRepository.findByVenueId(getVenueIdByScheduleId(scheduleId));
      Optional<Seat> seatOpt =
          seats.stream().filter(seat -> seat.getSeatId().equals(seatId)).findFirst();

      if (seatOpt.isEmpty()) {
        throw new IllegalArgumentException("좌석을 찾을 수 없습니다: seatId=" + seatId);
      }

      Seat seat = seatOpt.get();
      Optional<SeatClass> seatClassOpt = seatClassRepository.findById(seat.getSeatClassId());
      if (seatClassOpt.isEmpty()) {
        throw new IllegalArgumentException(
            "좌석 클래스를 찾을 수 없습니다: seatClassId=" + seat.getSeatClassId());
      }

      String className = seatClassOpt.get().getClassName();
      String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);

      redis.opsForHash().put(classKey, String.valueOf(seatId), status);
    } catch (Exception e) {
      log.error("좌석 상태 업데이트 실패: scheduleId={}, seatId={}, status={}, error={}", scheduleId, seatId,
          status, e.getMessage(), e);
      throw new RuntimeException("좌석 상태 업데이트에 실패했습니다: " + e.getMessage(), e);
    }
  }

  @PostConstruct
  @Transactional(readOnly = true)
  public void initializeRedisOnStartup() {
    try {
      List<Map<String, Object>> eventSchedules = eventScheduleRepository.findAllEventSchedules();
      if (eventSchedules.isEmpty()) {
        log.info("Redis 초기화할 이벤트 스케줄이 없습니다.");
        return;
      }

      for (Map<String, Object> eventSchedule : eventSchedules) {
        try {
          Long eventScheduleId = ((Number) eventSchedule.get("event_schedule_id")).longValue();
          Long venueId = ((Number) eventSchedule.get("venue_id")).longValue();
          initSeatStatuses(eventScheduleId, venueId);
        } catch (Exception e) {
          log.error("Redis 초기화 실패: eventScheduleId={}, venueId={}, error={}",
              eventSchedule.get("event_schedule_id"), eventSchedule.get("venue_id"), e.getMessage(),
              e);
          continue;
        }
      }
    } catch (Exception e) {
      log.error("Redis 초기화 중 오류 발생: {}", e.getMessage(), e);
      e.printStackTrace();
    }
  }

  private Long getVenueIdByScheduleId(Long scheduleId) {
    try {
      Long venueId = eventScheduleRepository.findVenueIdByEventScheduleId(scheduleId);
      if (venueId == null) {
        throw new IllegalArgumentException("해당 스케줄의 venue 정보를 찾을 수 없습니다: scheduleId=" + scheduleId);
      }
      log.debug("스케줄 ID {}에 대한 venue ID 조회 완료: {}", scheduleId, venueId);
      return venueId;
    } catch (Exception e) {
      log.error("스케줄 ID {}에 대한 venue ID 조회 실패: {}", scheduleId, e.getMessage(), e);
      throw new IllegalArgumentException("해당 스케줄의 venue 정보를 찾을 수 없습니다: scheduleId=" + scheduleId,
          e);
    }
  }

  public void holdSeat(Long scheduleId, Long seatId, Long userId, long ttlSeconds) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.opsForValue().set(holdKey, String.valueOf(userId), ttlSeconds, TimeUnit.SECONDS);
    log.info("좌석 홀드 설정: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId, userId,
        ttlSeconds);
  }

  public void releaseSeatHold(Long scheduleId, Long seatId) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.delete(holdKey);
    log.info("좌석 홀드 해제: scheduleId={}, seatId={}", scheduleId, seatId);
  }


  public void confirmSeat(Long scheduleId, Long seatId) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.delete(holdKey);
    updateSeatStatusByClassName(scheduleId, seatId, "BOOKED");
    log.info("좌석 예약 확정: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void blockSeat(Long scheduleId, Long seatId) {
    updateSeatStatusByClassName(scheduleId, seatId, "BLOCKED");
    log.info("좌석 블록: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void clearSeatStatus(Long scheduleId, Long seatId) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.delete(holdKey);
    updateSeatStatusByClassName(scheduleId, seatId, "");
    log.info("좌석 상태 초기화: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void cancelSeat(Long scheduleId, Long seatId) {
    clearSeatStatus(scheduleId, seatId);
    log.info("좌석 예매 취소: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public Map<String, Integer> getAvailableSeatCountsBySeatClass(Long scheduleId) {
    try {
      Map<String, Integer> result = new LinkedHashMap<>();

      Long venueId = getVenueIdByScheduleId(scheduleId);
      List<SeatClass> seatClasses = seatClassRepository.findByVenueId(venueId);

      String holdKeyPattern = String.format("seat:hold:%d:*", scheduleId);
      Set<String> holdKeys = redis.keys(holdKeyPattern);
      Map<String, String> holdStatuses = new HashMap<>();

      if (!holdKeys.isEmpty()) {
        List<String> holdValues = redis.opsForValue().multiGet(holdKeys);
        for (int i = 0; i < holdKeys.size(); i++) {
          String key = holdKeys.toArray(new String[0])[i];
          String value = holdValues.get(i);
          if (value != null) {
            String seatId = key.substring(key.lastIndexOf(":") + 1);
            holdStatuses.put(seatId, "HELD");
          }
        }
      }

      for (SeatClass seatClass : seatClasses) {
        String className = seatClass.getClassName();
        String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);

        Map<Object, Object> statusMap = redis.opsForHash().entries(classKey);
        if (!statusMap.isEmpty()) {
          int availableCount = 0;
          for (Map.Entry<Object, Object> entry : statusMap.entrySet()) {
            String seatId = (String) entry.getKey();
            String status = (String) entry.getValue();

            // HELD 상태가 아니고, 빈 문자열("")이거나 null인 경우만 카운트
            if (!holdStatuses.containsKey(seatId) && (status == null || status.isEmpty())) {
              availableCount++;
            }
          }
          result.put(className, availableCount);
        } else {
          result.put(className, 0);
        }
      }

      return result;
    } catch (Exception e) {
      log.error("좌석 클래스별 남은 좌석 개수 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      throw new RuntimeException("좌석 클래스별 남은 좌석 개수 조회에 실패했습니다: " + e.getMessage(), e);
    }
  }
}
