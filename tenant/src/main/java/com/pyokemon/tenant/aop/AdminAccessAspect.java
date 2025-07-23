package com.pyokemon.tenant.aop;

import com.pyokemon.tenant.exception.TenantException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAccessAspect {

    //@AdminOnly 어노테이션이 붙은 메서드에 대한 권한 체크
    @Around("@annotation(com.pyokemon.tenant.annotation.AdminOnly)")
    public Object checkAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // HTTP 요청에서 권한 정보 확인
        HttpServletRequest request = getCurrentRequest();
        
        // TODO: 실제로는 JWT 토큰이나 Spring Security Context에서 권한 확인
        // 현재는 임시로 헤더 체크
        String userRole = request.getHeader("X-User-Role");
        
        if (!"ADMIN".equals(userRole)) {
            log.warn("Admin access denied for method: {}", joinPoint.getSignature().getName());
            throw TenantException.accessDenied();
        }
        
        log.debug("Admin access granted for method: {}", joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }
    
    /**
     * 현재 HTTP 요청 객체 가져오기
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }
} 