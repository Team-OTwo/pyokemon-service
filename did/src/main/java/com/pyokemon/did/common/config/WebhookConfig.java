package com.pyokemon.did.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Webhook 처리를 위한 설정 클래스
 * - ACA-Py로부터 받은 이벤트를 처리하는 방법 정의
 * - WebClient를 통한 HTTP 통신 설정
 */
@Configuration
public class WebhookConfig {

    /**
     * Webhook 이벤트 처리를 위한 핸들러 Bean
     * - ACA-Py로부터 받은 이벤트를 처리하는 로직 정의
     *
     * @return WebhookEventHandler 인스턴스
     */
    @Bean
    public WebhookEventHandler webhookEventHandler() {
        return new WebhookEventHandler();
    }

    /**
     * Webhook 이벤트 핸들러
     * - ACA-Py로부터 받은 다양한 이벤트 타입별 처리 로직
     */
    public static class WebhookEventHandler {

        /**
         * Connection 상태 변경 이벤트 처리
         * - ACA-Py에서 연결 상태가 변경될 때 호출
         * - Connection.status = ACTIVE 처리
         *
         * @param eventData ACA-Py로부터 받은 이벤트 데이터 (JSON)
         */
        public void handleConnectionEvent(String eventData) {
            // TODO: Connection.status = ACTIVE 처리 로직 구현
            System.out.println("Connection event received: " + eventData);
        }

        /**
         * Out of Band 이벤트 처리
         * - 초대 URL 생성 및 처리 관련 이벤트
         * - Mediator 초대 URL 생성 시 호출
         *
         * @param eventData ACA-Py로부터 받은 이벤트 데이터 (JSON)
         */
        public void handleOutOfBandEvent(String eventData) {
            // TODO: 초대 URL 관련 이벤트 처리 로직 구현
            System.out.println("Out of Band event received: " + eventData);
        }

        /**
         * Credential 발급 이벤트 처리
         * - Verifiable Credential 발급 완료 시 호출
         * - VC_meta.status = ISSUED 처리
         *
         * @param eventData ACA-Py로부터 받은 이벤트 데이터 (JSON)
         */
        public void handleIssueCredentialEvent(String eventData) {
            // TODO: VC_meta.status = ISSUED 처리 로직 구현
            System.out.println("Issue Credential event received: " + eventData);
        }
    }
}