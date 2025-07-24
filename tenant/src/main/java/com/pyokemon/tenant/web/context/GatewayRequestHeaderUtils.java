package com.pyokemon.tenant.web.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pyokemon.tenant.exception.TenantException;

// Gateway에서 전달한 요청 헤더 정보를 쉽게 꺼내기 위한 유틸 클래스
public class GatewayRequestHeaderUtils {

  // 요청 헤더에서 특정 키의 값을 문자열로 가져옴
  public static String getRequestHeaderParamAsString(String key) {
    // 현재 쓰레드의 요청 정보를 가져옴 (Spring의 RequestContextHolder 사용)
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    // 요청 객체에서 헤더 값 추출
    return requestAttributes.getRequest().getHeader(key);
  }

  // 사용자 ID 가져오기
  public static String getUserId() {
    return getRequestHeaderParamAsString("X-Auth-UserId");
  }

  // 클라이언트 디바이스 정보 가져오기 (ex: WEB, MOBILE 등)
  public static String getClientDevice() {
    return getRequestHeaderParamAsString("X-Client-Device");
  }

  // 클라이언트 IP 주소 가져오기
  public static String getClientAddress() {
    return getRequestHeaderParamAsString("X-Client-Address");
  }

  // 사용자 ID가 없으면 예외를 던지도록 강제
  public static String getUserIdOrThrowException() {
    String userId = getUserId();
    if (userId == null || userId.trim().isEmpty()) {
      throw TenantException.accessDenied();
    }
    return userId;
  }

  // 디바이스 정보가 없으면 예외 던짐
  public static String getClientDeviceOrThrowException() {
    String clientDevice = getClientDevice();
    if (clientDevice == null || clientDevice.trim().isEmpty()) {
      throw new TenantException("클라이언트 디바이스 정보가 없습니다", "CLIENT_DEVICE_REQUIRED");
    }
    return clientDevice;
  }

  // IP 주소가 없으면 예외 던짐
  public static String getClientAddressOrThrowException() {
    String clientAddress = getClientAddress();
    if (clientAddress == null || clientAddress.trim().isEmpty()) {
      throw new TenantException("클라이언트 IP 정보가 없습니다", "CLIENT_ADDRESS_REQUIRED");
    }
    return clientAddress;
  }
}
