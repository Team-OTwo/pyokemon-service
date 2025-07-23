package com.pyokemon.admin.api.controller;

import com.pyokemon.admin.dto.PerformanceApprovalDto;
import com.pyokemon.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/performances")
@RequiredArgsConstructor
public class PerformanceApprovalController {
    
    private final RestTemplate restTemplate;
    
    //@Value("${service.performance.url}")
    private String performanceServiceUrl;
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<Void>> approvePerformance(
            @PathVariable Long id,
            @Valid @RequestBody PerformanceApprovalDto requestDto) {
        
        String url = performanceServiceUrl + "/api/performances/" + id + "/status";
        
        try {
            // 상태 변경 요청 객체 생성
            var requestBody = Map.of(
                    "status", "APPROVED"
            );
            
            // 공연 서비스 API 호출
            ResponseEntity<ResponseDto<Void>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestBody),
                    new ParameterizedTypeReference<ResponseDto<Void>>() {}
            );
            
            return ResponseEntity.ok(ResponseDto.success("공연이 승인되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<ResponseDto<Void>> rejectPerformance(
            @PathVariable Long id,
            @Valid @RequestBody PerformanceApprovalDto requestDto) {
        
        String url = performanceServiceUrl + "/api/performances/" + id + "/status";
        
        try {
            // 상태 변경 요청 객체 생성
            var requestBody = Map.of(
                    "status", "REJECTED"
            );
            
            // 공연 서비스 API 호출
            ResponseEntity<ResponseDto<Void>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestBody),
                    new ParameterizedTypeReference<ResponseDto<Void>>() {}
            );
            
            return ResponseEntity.ok(ResponseDto.success("공연이 거절되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 