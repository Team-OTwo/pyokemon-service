package com.pyokemon.did.common.web.constant;

/**
 * Gateway를 통해 전달되는 인증 헤더 상수들을 정의하는 클래스
 *
 * @author pyokemon
 * @since 1.0.0
 */
public final class GatewayHeaderConstants {

  /**
   * 유틸리티 클래스이므로 인스턴스화를 방지
   */
  private GatewayHeaderConstants() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static class Auth {
    /**
     * 사용자 계정 ID 헤더
     */
    public static final String X_AUTH_ACCOUNT_ID = "X-Auth-AccountId";
    /**
     * 사용자 역할 헤더
     */
    public static final String X_AUTH_ROLE = "X-Auth-Role";

    /**
     * 사용자 디바이스 ID 헤더
     */
    public static final String X_AUTH_DEVICE_ID = "X-Auth-DeviceId";
  }

  public static class Role {
    /**
     * 테넌트 역할 값
     */
    public static final String ROLE_TENANT = "TENANT";

    /**
     * 일반 사용자 역할 값
     */
    public static final String ROLE_USER = "USER";

    /**
     * 관리자 역할 값
     */
    public static final String ROLE_ADMIN = "ADMIN";

  }

}
