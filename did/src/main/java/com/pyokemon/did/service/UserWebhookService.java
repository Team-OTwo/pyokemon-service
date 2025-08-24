package com.pyokemon.did.service;

import com.pyokemon.did.api.backend.dto.ConnectionWebhookDto;
import com.pyokemon.did.api.backend.dto.OutOfBandWebhookDto;

/**
 * User ACA-Py webhook 처리를 위한 서비스 인터페이스
 */
public interface UserWebhookService {

    /**
     * Connection webhook을 처리합니다.
     *
     * @param webhookDto Connection webhook 데이터
     */
    void handleConnectionWebhook(ConnectionWebhookDto webhookDto);

    /**
     * Out of Band webhook을 처리합니다.
     *
     * @param webhookDto Out of Band webhook 데이터
     */
    void handleOutOfBandWebhook(OutOfBandWebhookDto webhookDto);
}
