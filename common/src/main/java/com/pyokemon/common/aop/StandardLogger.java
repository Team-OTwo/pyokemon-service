
package com.pyokemon.common.aop;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.util.MultiReadHttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class StandardLogger {
  private static final String HTTP_REQUEST = "httpRequest";
  private static final String HTTP_RESPONSE = "httpResponse";
  private static final String GENERAL = "service";
  private static final double MILLISECOND_TO_SECOND = 1000.0;
  private static final String HEADER_PREFIX = "x-";
  private static final String API_SEPARATOR = "=================";

  @Autowired
  private ObjectMapper objectMapper;

  @Pointcut("execution(* com.pyokemon.common.exception.GlobalExceptionHandler.*(..))")
  private void controllerAdviceMethod() {}

  @Pointcut("execution(* com.pyokemon..api..*Controller.*(..))")
  private void controllerMethod() {}

  @Pointcut("execution(* com.pyokemon..api..*Service.*(..)) || execution(* com.pyokemon.common.util.*.*(..))")
  private void serviceAndUtilMethod() {}

  @AfterReturning(value = "controllerAdviceMethod()", returning = "returnObj")
  public void afterControllerAdvice(Object returnObj) {
    if (returnObj instanceof ResponseDto) {
      ResponseDto<?> responseDto = (ResponseDto<?>) returnObj;
      if (!responseDto.isSuccess()) {
        MDC.put("errorCode", responseDto.getErrorCode());
      }
    } else {
      MDC.put("errorCode", "Internal Api Error");
    }
  }

  @Before("controllerMethod()")
  public void beforeControllerLog(JoinPoint joinPoint) {
    HttpServletRequest request = getCurrentRequest();

    log.info("{} API Start : {} {}", API_SEPARATOR, request.getRequestURI(), API_SEPARATOR);
    log.info("Api Start timestamp - {}", new Timestamp(System.currentTimeMillis()));
    log.info("Session ID : {}", MDC.get("sessionId"));
    log.info("Span ID : {}", MDC.get("spanId"));
    writeRequestLog();

    loggingParameter(joinPoint);
  }

  @AfterReturning(value = "controllerMethod()", returning = "returnObj")
  public void afterControllerLog(Object returnObj) {
    HttpServletRequest request = getCurrentRequest();
    if (returnObj == null) {
      log.info("return type = null");
    } else {
      log.info("[{}] return type = {}", request.getRequestURI(),
          returnObj.getClass().getSimpleName());
      log.info("[{}] return value = {}", request.getRequestURI(), returnObj);
    }
    log.info("API End timestamp - {}", new Timestamp(System.currentTimeMillis()));

    String startTime = MDC.get("apiStartTime");
    if (startTime != null) {
      long executionTime = System.currentTimeMillis() - Long.parseLong(startTime);
      log.info("수행시간 : {}초", executionTime / MILLISECOND_TO_SECOND);
    }

    log.info("{} API End : {} {}", API_SEPARATOR, request.getRequestURI(), API_SEPARATOR);
  }

  @AfterThrowing(value = "controllerMethod()", throwing = "exception")
  public void exceptionController(Exception exception) {
    HttpServletRequest request = getCurrentRequest();
    log.error("Exception occurred: {}", exception.getMessage(), exception);
    log.info("{} API End : {} {}", API_SEPARATOR, request.getRequestURI(), API_SEPARATOR);
  }

  @Before("serviceAndUtilMethod()")
  public void beforeServiceLog(JoinPoint joinPoint) {
    Method method = getMethod(joinPoint);
    log.info("{} Service Start : {} {}", API_SEPARATOR, method.getName(), API_SEPARATOR);
    loggingParameter(joinPoint);
  }

  @AfterReturning(value = "serviceAndUtilMethod()", returning = "returnObj")
  public void afterServiceLog(JoinPoint joinPoint, Object returnObj) {
    Method method = getMethod(joinPoint);
    if (returnObj == null) {
      log.info("[{}] return type = null", method.getName());
    } else {
      log.info("[{}] return type = {}, value = {}", method.getName(),
          returnObj.getClass().getSimpleName(), returnObj);
    }
    log.info("{} Service End : {} {}", API_SEPARATOR, method.getName(), API_SEPARATOR);
  }

  private Method getMethod(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    return signature.getMethod();
  }

  private void loggingParameter(JoinPoint joinPoint) {
    Method method = getMethod(joinPoint);

    Object[] args = joinPoint.getArgs();
    if (args == null || args.length <= 0) {
      log.info("no parameter");
      return;
    }

    for (int i = 0; i < args.length; i++) {
      if (args[i] == null) {
        continue;
      }
      log.info("parameter name = {}, type = {}, value = {}", method.getParameters()[i].getName(),
          args[i].getClass().getSimpleName(), args[i]);
    }
  }

  /**
   * Response Log 를 남기는 Method
   *
   * @param loggerName
   */
  public void writeResponseLog(MultiReadHttpServletResponse httpSvtResp, String loggerName,
      String accept) {
    try {
      /* Response String 추출 */
      HttpServletRequest request = getCurrentRequest();
      httpSvtResp.flushBuffer();
      byte[] copy = httpSvtResp.getCopy();
      String responseStr = new String(copy, httpSvtResp.getCharacterEncoding());

      /* Response String -> JSON 로 변환 */
      ObjectNode responseObj = responseStringToJson(responseStr, accept);
      setResponseMetadata();
      this.writeLog(loggerName,
          String.format("[%s] Response > %s", request.getRequestURI(), responseObj.toString()));
      setGeneralMetadata();
    } catch (IOException e) {
      this.writeLog(loggerName, "Response Log Write Fail");
      log.error("Response log write failed: {}", e.getMessage(), e);
    }
  }

  private ObjectNode responseStringToJson(String responseStr, String accept) {
    ObjectNode result = objectMapper.createObjectNode();
    try {
      if (org.springframework.util.StringUtils.hasText(responseStr)) {
        accept = accept == null ? "" : accept;

        /* Accept 타입에 맞는 Parser 사용. */
        try {
          if (accept.contains("json")) {
            result = (ObjectNode) objectMapper.readTree(responseStr);
          } else {
            // XML 처리는 단순화하여 문자열로 저장
            result.put("content", responseStr);
          }
        } catch (Exception e) {
          /*
           * Parsing Error 발생시에 다른 Type으로 한번도 Parsing 시도 후에도 Exception이 발생하면 로깅 Fail
           */
          log.error("Response parsing failed: {}", e.getMessage(), e);
          result.put("content", responseStr);
          result.put("parseError", true);
        }
      }
    } catch (Exception e) {
      log.error("Response Parsing Error. Maybe View Type Response.");
      log.error("Response parsing error: {}", e.getMessage(), e);
      result.put("content", responseStr);
      result.put("parseError", true);
    }
    return result;
  }

  public void writeRequestLog() {
    HttpServletRequest request = getCurrentRequest();
    writeRequestLog(request, null);
  }

  /** Request Log 를 남기는 Method */
  public void writeRequestLog(HttpServletRequest httpSvtReq, String loggerName) {
    try {
      ObjectNode logData = objectMapper.createObjectNode();
      /* 헤더정보 설정 */
      ObjectNode headers = objectMapper.createObjectNode();
      Enumeration<String> headerNames = httpSvtReq.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        String lowerHeaderName = headerName.toLowerCase(Locale.getDefault());
        if (lowerHeaderName.contains(HEADER_PREFIX)) {
          headers.put(lowerHeaderName, httpSvtReq.getHeader(headerName));
        }
      }
      logData.set("headers", headers);
      /* 파라미터 로그 */
      ObjectNode params = objectMapper.createObjectNode();
      if (httpSvtReq.getParameterNames().hasMoreElements()) {
        Enumeration<String> paramNames = httpSvtReq.getParameterNames();
        while (paramNames.hasMoreElements()) {
          String paramName = paramNames.nextElement();
          params.put(paramName, httpSvtReq.getParameter(paramName));
        }
      }

      /* 파라미터 타입 */
      if (org.springframework.util.StringUtils.hasText(httpSvtReq.getQueryString())) {
        logData.put("paramType", "query");
      } else {
        if (params.size() > 0) {
          logData.put("paramType", "body");
        } else {
          logData.put("paramType", "none");
        }
      }
      logData.set("params", params);
      Object globalId = httpSvtReq.getAttribute("globalId");
      if (globalId != null) {
        logData.put("globalId", globalId.toString());
      }
      /* Request 로그 */
      setRequestMetadata();
      if (loggerName == null) {
        log.info("[{}] Request Information > {}", httpSvtReq.getRequestURI(), logData);
      } else {
        this.writeLog(loggerName,
            String.format("[%s] Request > %s", httpSvtReq.getRequestURI(), logData));
      }
      String xForwardedFor = httpSvtReq.getHeader("X-Forwarded-For");
      if (StringUtils.isBlank(xForwardedFor)) {
        ThreadContext.put("clientIp", "-");
      } else {
        ThreadContext.put("clientIp", xForwardedFor.split(",")[0]);
      }
      setGeneralMetadata();
    } catch (Exception e) {
      log.error("Request log write failed: {}", e.getMessage(), e);
    }
  }

  /**
   * 로깅을 위한 단순화된 메서드
   *
   * @param loggerName : 로거 설정 명 (현재는 사용하지 않음)
   * @param logText : 로깅내용
   */
  public void writeLog(String loggerName, String logText) {
    log.info(logText);
  }

  private void setRequestMetadata() {
    MDC.put("apiStartTime", Long.toString(System.currentTimeMillis()));
    HttpServletRequest request = getCurrentRequest();

    ObjectNode requestMetadata = objectMapper.createObjectNode();

    requestMetadata.put("traceId", MDC.get("traceId"));
    requestMetadata.put("spanId", MDC.get("spanId"));
    requestMetadata.put("type", HTTP_REQUEST);
    requestMetadata.put("clientId", request.getHeader("X-Client-ID"));
    requestMetadata.put("method", request.getMethod());
    requestMetadata.put("api", request.getRequestURI());

    ThreadContext.put("metadata", requestMetadata.toString());
  }

  private void setGeneralMetadata(String type) {
    ObjectNode generalMetadata = objectMapper.createObjectNode();
    generalMetadata.put("traceId", MDC.get("traceId"));
    generalMetadata.put("spanId", MDC.get("spanId"));
    generalMetadata.put("type", type);

    ThreadContext.put("metadata", generalMetadata.toString());
  }

  private void setGeneralMetadata() {
    setGeneralMetadata(GENERAL);
  }

  private HttpServletRequest getCurrentRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }

  private HttpServletResponse getCurrentResponse() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getResponse();
  }

  private void setResponseMetadata() {
    HttpServletRequest request = getCurrentRequest();
    HttpServletResponse response = getCurrentResponse();
    ObjectNode responseMetadata = objectMapper.createObjectNode();

    responseMetadata.put("traceId", MDC.get("traceId"));
    responseMetadata.put("spanId", MDC.get("spanId"));
    responseMetadata.put("type", HTTP_RESPONSE);
    responseMetadata.put("method", request.getMethod());
    responseMetadata.put("api", request.getRequestURI());

    String startTime = MDC.get("apiStartTime");
    if (startTime != null) {
      long responseTime = System.currentTimeMillis() - Long.parseLong(startTime);
      responseMetadata.put("responseTimeMs", Long.toString(responseTime));
    }

    if (response != null) {
      responseMetadata.put("statusCode", response.getStatus());
    }

    String errorCode = MDC.get("errorCode");
    if (errorCode != null) {
      responseMetadata.put("errorCode", errorCode);
    }

    ThreadContext.put("metadata", responseMetadata.toString());
  }

  public void setLoggerInformation(HttpServletRequest request) {
    String sessionId = request.getHeader("X-Login-Session");
    String clientId = request.getHeader("X-Client-ID");
    MDC.put("spanId", UUID.randomUUID().toString());
    MDC.put("sessionId", sessionId);
    MDC.put("clientId", clientId);

    String traceId = request.getHeader("X-Trace-ID");
    if (traceId == null || traceId.isEmpty()) {
      MDC.put("traceId", MDC.get("spanId"));
    } else {
      MDC.put("traceId", traceId);
    }
  }
}
