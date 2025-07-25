package com.pyokemon.admin.web.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Gateway에서 전달받은 헤더에서 사용자 정보를 추출하는 유틸리티 클래스
 */
public class GatewayRequestHeaderUtils {

    private static final String AUTH_USER_ID_HEADER = "X-Auth-UserId";

    /**
     * Gateway에서 전달받은 헤더에서 사용자 ID를 추출합니다.
     * 
     * @return 사용자 ID (관리자 사용자명)
     * @throws RuntimeException 헤더가 없거나 값이 비어있는 경우
     */
    public static String getUserIdOrThrowException() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("요청 컨텍스트를 찾을 수 없습니다.");
        }

        String userId = attributes.getRequest().getHeader(AUTH_USER_ID_HEADER);
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        return userId;
    }

    /**
     * Gateway에서 전달받은 헤더에서 사용자 ID를 추출합니다.
     * 
     * @return 사용자 ID (관리자 사용자명), 없는 경우 null 반환
     */
    public static String getUserIdOrNull() {
        try {
            return getUserIdOrThrowException();
        } catch (Exception e) {
            return null;
        }
    }
} 