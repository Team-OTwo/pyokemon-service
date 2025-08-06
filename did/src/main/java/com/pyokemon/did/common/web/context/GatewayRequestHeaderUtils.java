package com.pyokemon.did.common.web.context;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

public class GatewayRequestHeaderUtils {

    public static String getAccountIdOrThrowException() {
        HttpServletRequest request = getCurrentRequest();
        String accountId = (String) request.getAttribute("X-Auth-AccountId");
        if (accountId == null || accountId.isEmpty()) {
            throw new BusinessException("사용자 인증 정보가 없습니다.", AccountErrorCodes.ACCESS_DENIED);
        }
        return accountId;
    }

    public static Long getAccountIdAsLongOrThrowException() {
        String accountId = getAccountIdOrThrowException();
        try {
            return Long.parseLong(accountId);
        } catch (NumberFormatException e) {
            throw new BusinessException("잘못된 사용자 ID 형식입니다.", AccountErrorCodes.ACCESS_DENIED);
        }
    }

    public static String getRoleOrThrowException() {
        HttpServletRequest request = getCurrentRequest();
        String role = (String) request.getAttribute("X-Auth-Role");
        if (role == null || role.isEmpty()) {
            throw new BusinessException("사용자 권한 정보가 없습니다.", AccountErrorCodes.ACCESS_DENIED);
        }
        return role;
    }

    public static String getAccountId() {
        HttpServletRequest request = getCurrentRequest();
        return (String) request.getAttribute("X-Auth-AccountId");
    }

    public static String getRole() {
        HttpServletRequest request = getCurrentRequest();
        return (String) request.getAttribute("X-Auth-Role");
    }

    public static boolean isTenant() {
        return "TENANT".equals(getRole());
    }

    public static boolean isUser() {
        return "USER".equals(getRole());
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("요청 컨텍스트를 찾을 수 없습니다.", AccountErrorCodes.INTERNAL_ERROR);
        }
        return attributes.getRequest();
    }
} 