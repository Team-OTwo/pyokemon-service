package com.pyokemon.did.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DID 문서 모델
 * W3C DID 표준을 기반으로 한 문서 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DidDocument {
    
    // DID 식별자
    private String id;
    
    // DID 컨트롤러(관리자)
    private List<String> controller;
    
    // 검증 방법(공개키 등)
    private List<VerificationMethod> verificationMethod;
    
    // 인증 방법
    private List<String> authentication;
    
    // 추가 정보
    private Map<String, Object> additionalProperties;
    
    // 서비스 엔드포인트
    private List<Service> service;
    
    /**
     * 검증 방법 모델
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationMethod {
        private String id;
        private String type;
        private String controller;
        private String publicKeyMultibase;
    }
    
    /**
     * 서비스 엔드포인트 모델
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Service {
        private String id;
        private String type;
        private String serviceEndpoint;
    }
} 