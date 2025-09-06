package com.pyokemon.event.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.tenant.*;
import com.pyokemon.event.dto.tenant.app.TenantEventListResponseDtoForApp;
import com.pyokemon.event.service.TenantEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/tenant")
@RequiredArgsConstructor
public class TenantEventController {

  private final TenantEventService tenantEventService;
  private final ObjectMapper objectMapper;

  // 테넌트별 공연 목록 조회 (테넌트 웹용)
  @GetMapping
  public ResponseDto<List<TenantEventListDto>> getTenantEventList(@RequestParam Long account_id) {
    List<TenantEventListDto> events = tenantEventService.getTenantEventListByAccountId(account_id);
    return ResponseDto.success(events,
        "Tenant events retrieved successfully for account_id: " + account_id);
  }

  // 테넌트용 이벤트 상세조회 (가격 정보 포함)
  @GetMapping("/{eventId}/detail")
  public ResponseDto<EventDetailResponseDTO> getTenantEventDetail(@PathVariable Long eventId) {
    EventDetailResponseDTO eventDetail = tenantEventService.getTenantEventDetailByEventId(eventId);
    return ResponseDto.success(eventDetail, "Tenant event detail retrieved successfully");
  }

  // 이벤트 등록 (테넌트용)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<EventResponseDto> registerEvent(
      @RequestParam("eventData") String eventDataJson,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile) {
    try {
      // JSON 문자열을 EventRegisterDto로 변환
      EventRegisterDto eventRegisterDto = objectMapper.readValue(eventDataJson, EventRegisterDto.class);
      
      // 썸네일 파일이 있으면 Base64로 변환
      if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
        String base64Thumbnail = convertMultipartFileToBase64(thumbnailFile);
        eventRegisterDto.setThumbnailUrl(base64Thumbnail);
      }
      
      EventResponseDto registeredEvent =
          tenantEventService.registerEvent(eventRegisterDto, eventRegisterDto.getAccountId());
      return ResponseDto.success(registeredEvent, "Event registered successfully");
    } catch (Exception e) {
      throw new BusinessException("Failed to process multipart request", "MULTIPART_PROCESSING_FAILED");
    }
  }

  // 이벤트 수정 (테넌트용)
  @PutMapping("/{eventId}")
  public ResponseDto<EventResponseDto> updateEvent(@PathVariable Long eventId,
      @RequestParam("eventData") String eventDataJson,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile) {
    try {
      // JSON 문자열을 EventUpdateDto로 변환
      EventUpdateDto eventUpdateDto = objectMapper.readValue(eventDataJson, EventUpdateDto.class);
      eventUpdateDto.setEventId(eventId);
      
      // 썸네일 파일이 있으면 Base64로 변환
      if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
        String base64Thumbnail = convertMultipartFileToBase64(thumbnailFile);
        eventUpdateDto.setThumbnailUrl(base64Thumbnail);
      }
      
      EventResponseDto updatedEvent = tenantEventService.updateEvent(eventUpdateDto);
      return ResponseDto.success(updatedEvent, "Event updated successfully");
    } catch (Exception e) {
      throw new BusinessException("Failed to process multipart request", "MULTIPART_PROCESSING_FAILED");
    }
  }

  // 일정 등록 (기존 공연에 일정 추가) - 테넌트용
  @PostMapping("/{eventId}/schedules")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<String> registerEventSchedule(@PathVariable Long eventId,
      @RequestBody EventScheduleDto eventScheduleDto) {
    String result = tenantEventService.registerEventSchedule(eventId, eventScheduleDto);
    return ResponseDto.success(result, "Event schedule registered successfully");
  }


  // 앱 커서기반 공연 조회
  @GetMapping("/app")
  public ResponseDto<TenantEventListResponseDtoForApp> getEventListForApp(
      @RequestParam(required = false) LocalDateTime cursorDate,
      @RequestParam(required = false) Long cursorId, @RequestParam(defaultValue = "8") int limit,
      @RequestParam(required = false) String genre) {
    Long accountId = GatewayRequestHeaderUtils.getAccountIdOrThrow();
    TenantEventListResponseDtoForApp response = tenantEventService
        .getEventListForAppResponse(accountId, cursorDate, cursorId, limit, genre);
    return ResponseDto.success(response, "Tenant events retrieved successfully");
  }

  // 공연삭제
  @PostMapping("/{eventId}")
  public ResponseEntity<Void> updateStatusEvent(@PathVariable Long eventId) {
    tenantEventService.updateStatus(eventId, "CANCELED");
    return ResponseEntity.ok().build();
  }

  // 이미지 파일 업로드 API(React Quill 에디터에서 호출)
  @PostMapping("/upload-image")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<String> uploadImage(@RequestParam("file") MultipartFile file) {
    String fileUrl = tenantEventService.uploadImageFile(file);

    return ResponseDto.success(fileUrl, "Image uploaded successfully");
  }

  // MultipartFile을 Base64로 변환하는 헬퍼 메서드
  private String convertMultipartFileToBase64(MultipartFile file) {
    try {
      byte[] fileBytes = file.getBytes();
      String base64String = java.util.Base64.getEncoder().encodeToString(fileBytes);
      String mimeType = file.getContentType();
      return "data:" + mimeType + ";base64," + base64String;
    } catch (Exception e) {
      throw new BusinessException("Failed to convert file to Base64", "FILE_CONVERSION_FAILED");
    }
  }

}
