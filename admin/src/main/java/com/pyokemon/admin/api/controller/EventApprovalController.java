package com.pyokemon.admin.api.controller;

import com.pyokemon.admin.dto.EventApprovalDto;
import com.pyokemon.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventApprovalController {
    
    @Value("${service.event.url:http://localhost:8082}")
    private String eventServiceUrl;
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<Void>> approveEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventApprovalDto requestDto) {
        
        String url = eventServiceUrl + "/api/events/" + id + "/status";
        String reason = requestDto.getReason();
        
        try {
            // 요청 본문(JSON) 구성
            String requestBody = "{\"status\":\"APPROVED\",\"reason\":\"" + reason + "\"}";
            
            // HttpClient 생성
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .build();
            
            // HttpRequest 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            // API 호출
            log.info("이벤트 승인 API 호출: URL={}, 이벤트ID={}, 사유={}", url, id, reason);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 응답 처리
            int statusCode = response.statusCode();
            String responseBody = response.body();
            log.info("이벤트 승인 API 응답: 상태코드={}, 응답={}", statusCode, responseBody);
            
            if (statusCode >= 200 && statusCode < 300) {
                return ResponseEntity.ok(ResponseDto.success("이벤트가 승인되었습니다."));
            } else {
                return ResponseEntity.status(statusCode).body(
                    ResponseDto.error("이벤트 승인 실패: " + responseBody, "EVENT_APPROVAL_FAILED")
                );
            }
        } catch (IOException e) {
            log.error("이벤트 승인 API 호출 중 I/O 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ResponseDto.error("이벤트 승인 중 I/O 오류가 발생했습니다.", "IO_ERROR")
            );
        } catch (InterruptedException e) {
            log.error("이벤트 승인 API 호출 중 중단 발생: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return ResponseEntity.badRequest().body(
                ResponseDto.error("이벤트 승인 중 처리가 중단되었습니다.", "INTERRUPTED")
            );
        } catch (Exception e) {
            log.error("이벤트 승인 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                ResponseDto.error("이벤트 승인 중 오류가 발생했습니다.", "INTERNAL_ERROR")
            );
        }
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<ResponseDto<Void>> rejectEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventApprovalDto requestDto) {
        
        String url = eventServiceUrl + "/api/events/" + id + "/status";
        String reason = requestDto.getReason();
        
        try {
            // 요청 본문(JSON) 구성
            String requestBody = "{\"status\":\"REJECTED\",\"reason\":\"" + reason + "\"}";
            
            // HttpClient 생성
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .build();
            
            // HttpRequest 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            // API 호출
            log.info("이벤트 거절 API 호출: URL={}, 이벤트ID={}, 사유={}", url, id, reason);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 응답 처리
            int statusCode = response.statusCode();
            String responseBody = response.body();
            log.info("이벤트 거절 API 응답: 상태코드={}, 응답={}", statusCode, responseBody);
            
            if (statusCode >= 200 && statusCode < 300) {
                return ResponseEntity.ok(ResponseDto.success("이벤트가 거절되었습니다."));
            } else {
                return ResponseEntity.status(statusCode).body(
                    ResponseDto.error("이벤트 거절 실패: " + responseBody, "EVENT_REJECTION_FAILED")
                );
            }
        } catch (IOException e) {
            log.error("이벤트 거절 API 호출 중 I/O 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ResponseDto.error("이벤트 거절 중 I/O 오류가 발생했습니다.", "IO_ERROR")
            );
        } catch (InterruptedException e) {
            log.error("이벤트 거절 API 호출 중 중단 발생: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return ResponseEntity.badRequest().body(
                ResponseDto.error("이벤트 거절 중 처리가 중단되었습니다.", "INTERRUPTED")
            );
        } catch (Exception e) {
            log.error("이벤트 거절 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                ResponseDto.error("이벤트 거절 중 오류가 발생했습니다.", "INTERNAL_ERROR")
            );
        }
    }
}