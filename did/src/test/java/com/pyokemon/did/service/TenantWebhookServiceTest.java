package com.pyokemon.did.service;

import static com.pyokemon.common.exception.code.DidErrorCodes.CONNECTION_CREATION_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.pyokemon.did.domain.dto.request.webhook.ConnectionWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryException;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;
import com.pyokemon.did.service.impl.TenantWebhookServiceImpl;

@ExtendWith(MockitoExtension.class)
class TenantWebhookServiceTest {

  @Mock
  private AcaPyConnectionRepository acaPyConnectionRepository;

  @InjectMocks
  private TenantWebhookServiceImpl tenantWebhookService;

  private ConnectionWebhookRequest request;
  private AcaPyConnection connection;

  @BeforeEach
  void setUp() {
    // 테스트 요청 객체 생성
    request = new ConnectionWebhookRequest();
    request.setInvitationMsgId("test-invitation-id");
    request.setConnectionId("test-connection-id");
    request.setState("active");

    // 테스트용 AcaPyConnection 객체 생성
    connection = mock(AcaPyConnection.class);
  }

  @Test
    @DisplayName("웹훅 처리 성공 테스트")
    void handleTenantConnectionWebhook_Success() {
        // Given
        when(acaPyConnectionRepository.findByInviMsgId(anyString())).thenReturn(Optional.of(connection));

        // When
        tenantWebhookService.handleTenantConnectionWebhook(request);

        // Then
        verify(connection).activate(request.getConnectionId());
        verify(acaPyConnectionRepository).update(connection);
    }

  @Test
  @DisplayName("웹훅 처리 - 비활성 상태 무시 테스트")
  void handleTenantConnectionWebhook_IgnoreInactiveState() {
    // Given
    request.setState("inactive");

    // When
    tenantWebhookService.handleTenantConnectionWebhook(request);

    // Then
    verify(acaPyConnectionRepository, never()).findByInviMsgId(anyString());
    verify(acaPyConnectionRepository, never()).update(any());
  }

  @Test
    @DisplayName("웹훅 처리 - 연결 찾기 실패 및 예외 발생 테스트")
    void handleTenantConnectionWebhook_ConnectionNotFoundWithException() {
        // Given
        // 항상 빈 Optional을 반환하도록 설정 (연결을 찾지 못하는 상황)
        when(acaPyConnectionRepository.findByInviMsgId(anyString())).thenReturn(Optional.empty());

        // When & Then
        // 서비스 메서드 호출 시 RetryException 발생 확인
        RetryException exception = assertThrows(RetryException.class, 
            () -> tenantWebhookService.handleTenantConnectionWebhook(request));
        
        // 예외 메시지 확인
        assertTrue(exception.getMessage().contains("AcaPy Connection NotFound"));
        
        // 연결을 찾지 못했으므로 activate와 update는 호출되지 않아야 함
        verify(connection, never()).activate(anyString());
        verify(acaPyConnectionRepository, never()).update(any(AcaPyConnection.class));
    }

  @Test
  @DisplayName("복구 처리 - 연결 찾음 테스트")
  void recoverTenantConnectionWebhook_ConnectionFound() {
    // Given
    Exception exception = new RuntimeException("Test Exception");
    when(acaPyConnectionRepository.findByInviMsgId(anyString()))
        .thenReturn(Optional.of(connection));

    // When & Then
    BusinessException businessException = assertThrows(BusinessException.class,
        () -> tenantWebhookService.recoverTenantConnectionWebhook(exception, request));

    verify(connection).deactivate();
    verify(acaPyConnectionRepository).update(connection);
    assertEquals(CONNECTION_CREATION_FAILED, businessException.getErrorCode());
  }

  @Test
  @DisplayName("복구 처리 - 연결 찾기 실패 테스트")
  void recoverTenantConnectionWebhook_ConnectionNotFoundInRecover() {
    // Given
    Exception exception = new RuntimeException("Test Exception");
    when(acaPyConnectionRepository.findByInviMsgId(anyString())).thenReturn(Optional.empty());

    // When & Then
    BusinessException businessException = assertThrows(BusinessException.class,
        () -> tenantWebhookService.recoverTenantConnectionWebhook(exception, request));

    verify(acaPyConnectionRepository, never()).update(any());
    assertEquals(CONNECTION_CREATION_FAILED, businessException.getErrorCode());
  }

  @Test
  @DisplayName("복구 처리 - 비활성화 중 예외 발생 테스트")
  void recoverTenantConnectionWebhook_ExceptionDuringDeactivation() {
    // Given
    Exception exception = new RuntimeException("Test Exception");
    when(acaPyConnectionRepository.findByInviMsgId(anyString()))
        .thenReturn(Optional.of(connection));
    doThrow(new RuntimeException("Test DB error")).when(acaPyConnectionRepository)
        .update(connection);

    // When & Then
    BusinessException businessException = assertThrows(BusinessException.class,
        () -> tenantWebhookService.recoverTenantConnectionWebhook(exception, request));

    verify(acaPyConnectionRepository).update(any());
    assertEquals(CONNECTION_CREATION_FAILED, businessException.getErrorCode());
  }
}
