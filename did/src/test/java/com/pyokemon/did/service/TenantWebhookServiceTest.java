package com.pyokemon.did.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.did.domain.dto.request.webhook.ConnectionWebhookRequest;
import com.pyokemon.did.service.impl.TenantWebhookServiceImpl;

@ExtendWith(MockitoExtension.class)
class TenantWebhookServiceTest {

  @Mock
  private AcaPyConnectionService acaPyConnectionService;

  @InjectMocks
  private TenantWebhookServiceImpl tenantWebhookService;

  private ConnectionWebhookRequest request;

  @BeforeEach
  void setUp() {
    // 테스트 요청 객체 생성
    request = new ConnectionWebhookRequest();
    request.setInvitationMsgId("test-invitation-id");
    request.setConnectionId("test-connection-id");
    request.setState("active");
  }

  @Test
  @DisplayName("웹훅 처리 성공 테스트")
  void handleTenantConnectionWebhook_Success() {
    // Given

    // When
    tenantWebhookService.handleTenantConnectionWebhook(request);

    // Then
    verify(acaPyConnectionService).updateConnectionId(anyString(), anyString());
  }

  @Test
  @DisplayName("웹훅 처리 - 비활성 상태 무시 테스트")
  void handleTenantConnectionWebhook_IgnoreInactiveState() {
    // Given
    request.setState("inactive");

    // When
    tenantWebhookService.handleTenantConnectionWebhook(request);

    // Then
    verify(acaPyConnectionService, never()).updateConnectionId(anyString(), anyString());
  }
}
