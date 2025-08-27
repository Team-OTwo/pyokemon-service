package com.pyokemon.did.service;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.ReceiveInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.Invitation;
import com.pyokemon.did.remote.acapy.common.dto.response.ReceiveInvitationResponse;
import com.pyokemon.did.remote.acapy.service.tenant.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.acapy.service.user.RemoteUserAcaPyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.AcaPyConnection.ConnectionStatus;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;

import com.pyokemon.did.service.impl.AcaPyConnectionServiceImpl;

@ExtendWith(MockitoExtension.class)
class AcaPyConnectionServiceTest {

  @Mock
  private RemoteTenantAcaPyService remoteTenantAcaPyService;

  @Mock
  private RemoteUserAcaPyService remoteUserAcaPyService;

  @Mock
  private AcaPyConnectionRepository acaPyConnectionRepository;

  @Mock
  private WalletService walletService;

  @InjectMocks
  private AcaPyConnectionServiceImpl acaPyConnectionService;

  private final Long TENANT_ID = 1L;
  private final Long USER_ID = 2L;
  private final String TOKEN = "test-token";
  private final String INVITATION_MSG_ID = "test-invitation-msg";

  private Wallet tenantWallet;
  private Wallet userWallet;
  private CreateInvitationResponse createInvitationResponse;
  private ReceiveInvitationResponse receivedInvitationResponse;

  @BeforeEach
  void setUp() {
    // 테넌트 지갑 설정
    tenantWallet = Wallet.builder().accountId(TENANT_ID).token(TOKEN).publicDid("test-public-did")
        .publicVerKey("test-public-verKey").build();

    // 사용자 지갑 설정
    userWallet = Wallet.builder().accountId(USER_ID).token(TOKEN).publicDid("test-public-did")
        .publicVerKey("test-public-verKey").build();

    // 초대장 응답 설정
    Invitation invitation = new Invitation();
    String INVITATION_ID = "test-invitation-id";
    invitation.setId(INVITATION_ID);
    invitation.setLabel("Invitation to USER AcaPy from TENANT AcaPy");

    createInvitationResponse = new CreateInvitationResponse();
    createInvitationResponse.setInvitation(invitation);
    createInvitationResponse.setInviMsgId(INVITATION_MSG_ID);

    // 초대장 수락 응답 설정
    receivedInvitationResponse = new ReceiveInvitationResponse();
    receivedInvitationResponse.setState("deleted");
  }

  @Test
    @DisplayName("AcaPy 연결 생성 성공 테스트")
    void createAcaPyConnection_Success() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(remoteUserAcaPyService.receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class)))
                .thenReturn(receivedInvitationResponse);

        // When
        assertDoesNotThrow(() -> acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        // Then
        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));

        ArgumentCaptor<AcaPyConnection> acaPyConnectionCaptor = ArgumentCaptor.forClass(AcaPyConnection.class);
        verify(acaPyConnectionRepository).save(acaPyConnectionCaptor.capture());

        AcaPyConnection savedConnection = acaPyConnectionCaptor.getValue();
        assertEquals(INVITATION_MSG_ID, savedConnection.getInviMsgId());
        assertEquals(TENANT_ID, savedConnection.getTenantId());
        assertEquals(USER_ID, savedConnection.getUserId());
        assertEquals(ConnectionStatus.PENDING, savedConnection.getStatus());
        assertNull(savedConnection.getConnectionId());

        verify(remoteUserAcaPyService).receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("이미 존재하는 연결 테스트")
    void createAcaPyConnection_AlreadyExists() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(true);

        // When
        acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID);

        // Then
        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService, never()).getWalletByAccountId(anyLong());
        verify(walletService, never()).getWalletByAccountId(anyLong());
        verify(remoteTenantAcaPyService, never()).createInvitation(anyString(), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("테넌트 지갑이 존재하지 않는 경우 예외 발생 테스트")
    void createAcaPyConnection_WalletNotFound() {
      // Given
      when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenThrow(
              new BusinessException("계정 지갑이 존재하지 않습니다.", WALLET_NOT_FOUND)
      );

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("계정 지갑이 존재하지 않습니다.", exception.getMessage());
        assertEquals(WALLET_NOT_FOUND, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService, never()).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService, never()).createInvitation(anyString(), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("사용자 지갑이 존재하지 않는 경우 예외 발생 테스트")
    void createAcaPyConnection_UserWalletNotFound() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenThrow(
              new BusinessException("계정 지갑이 존재하지 않습니다.", WALLET_NOT_FOUND)
      );


        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("계정 지갑이 존재하지 않습니다.", exception.getMessage());
        assertEquals(WALLET_NOT_FOUND, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService, never()).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("초대장 생성 실패 테스트 - null 응답")
    void createAcaPyConnection_InvitationCreationFailed_NullResponse() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장이 생성에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("초대장 생성 실패 테스트 - null 초대장")
    void createAcaPyConnection_InvitationCreationFailed_NullInvitation() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        CreateInvitationResponse nullInvitationResponse = new CreateInvitationResponse();
        nullInvitationResponse.setInvitation(null);

        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(nullInvitationResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장이 생성에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("초대장 수락 실패 테스트 - null 응답")
    void createAcaPyConnection_InvitationReceiveFailed_NullResponse() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(remoteUserAcaPyService.receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class)))
                .thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장 수락에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_RECEIVE_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService).receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("초대장 수락 실패 테스트 - 잘못된 상태")
    void createAcaPyConnection_InvitationReceiveFailed_InvalidState() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);

        ReceiveInvitationResponse invalidStateResponse = new ReceiveInvitationResponse();
        invalidStateResponse.setState("failed");
        when(remoteUserAcaPyService.receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class)))
                .thenReturn(invalidStateResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장 수락에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_RECEIVE_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService).receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("연결 저장 중 예외 발생 테스트")
    void createAcaPyConnection_SaveConnectionFailed() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(acaPyConnectionRepository.save(any(AcaPyConnection.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("AcaPy간 연결 생성에 실패했습니다.", exception.getMessage());
        assertEquals(CONNECTION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("remoteTenantAcaPyService API 호출 중 예외 발생 테스트")
    void remoteTenantAcaPyService_ExternalApiException() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenThrow(new RuntimeException("외부 API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("AcaPy간 연결 생성에 실패했습니다.", exception.getMessage());
        assertEquals(CONNECTION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

  @Test
    @DisplayName("remoteUserAcaPyService API 호출 중 예외 발생 테스트")
    void remoteUserAcaPyService_ExternalApiException() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
      when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
      when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(CreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(remoteUserAcaPyService.receiveInvitation(eq(TOKEN), any(ReceiveInvitationRequest.class)))
                .thenThrow(new RuntimeException("외부 API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("AcaPy간 연결 생성에 실패했습니다.", exception.getMessage());
        assertEquals(CONNECTION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(CreateInvitationRequest.class));
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService).receiveInvitation(anyString(), any(ReceiveInvitationRequest.class));
    }

    @Test
  @DisplayName("활성화된 연결 조회 성공 테스트")
  void getActiveAcaPyConnectionOrThrow_Success() {
    // Given
    AcaPyConnection connection = AcaPyConnection.builder()
        .connectionId("test-connection-id")
        .build();

    when(acaPyConnectionRepository.findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID))
        .thenReturn(Optional.of(connection));

    // When
    assertDoesNotThrow(() -> acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID));


    // Then
    verify(acaPyConnectionRepository).findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID);
  }

  @Test
  @DisplayName("활성화된 연결이 존재하지 않는 경우 예외 발생 테스트")
  void getActiveAcaPyConnectionOrThrow_ConnectionNotFound() {
    // Given
    when(acaPyConnectionRepository.findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID))
        .thenReturn(Optional.empty());

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () ->
        acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID));

    assertEquals("테넌트 ID: 1 사용자 ID: 2 에 대한 활성화된 연결을 찾을 수 없습니다.", exception.getMessage());
    assertEquals(CONNECTION_NOT_FOUND, exception.getErrorCode());

    verify(acaPyConnectionRepository).findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID);
  }

  @Test
  @DisplayName("연결이 존재하지만 connectionId가 null인 경우 예외 발생 테스트")
  void getActiveAcaPyConnectionOrThrow_ConnectionIdIsNull() {
    // Given
    AcaPyConnection connection = AcaPyConnection.builder()
        .connectionId(null) // connectionId가 null
        .build();

    when(acaPyConnectionRepository.findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID))
        .thenReturn(Optional.of(connection));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () ->
        acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID));

    assertEquals("테넌트 ID: 1 사용자 ID: 2 에 대한 활성화된 연결을 찾을 수 없습니다.", exception.getMessage());
    assertEquals(CONNECTION_NOT_FOUND, exception.getErrorCode());

    verify(acaPyConnectionRepository).findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID);
  }

  @Test
  @DisplayName("연결이 존재하지만 connectionId가 빈 문자열인 경우 예외 발생 테스트")
  void getActiveAcaPyConnectionOrThrow_ConnectionIdIsEmpty() {
    // Given
    AcaPyConnection connection = AcaPyConnection.builder()
        .connectionId("") // connectionId가 빈 문자열
        .build();

    when(acaPyConnectionRepository.findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID))
        .thenReturn(Optional.of(connection));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () ->
        acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID));

    assertEquals("테넌트 ID: 1 사용자 ID: 2 에 대한 활성화된 연결을 찾을 수 없습니다.", exception.getMessage());
    assertEquals(CONNECTION_NOT_FOUND, exception.getErrorCode());

    verify(acaPyConnectionRepository).findByTenantIdAndUserIdAndIsActive(TENANT_ID, USER_ID);
  }
}
