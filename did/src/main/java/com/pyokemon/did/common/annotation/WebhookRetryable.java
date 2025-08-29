package com.pyokemon.did.common.annotation;

import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 웹훅 메소드를 위한 재시도 어노테이션
 * - 최대 3회 재시도
 * - 재시도 간격: 1초, 2초, 4초 (지수 백오프)
 * - BusinessException 제외
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    retryFor = {RetryException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2.0)
)
public @interface WebhookRetryable {
}
