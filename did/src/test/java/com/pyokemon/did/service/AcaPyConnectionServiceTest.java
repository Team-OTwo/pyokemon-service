package com.pyokemon.did.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.AcaPyConnection.ConnectionStatus;
import com.pyokemon.did.domain.TenantWallet;
import com.pyokemon.did.domain.UserWallet;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;
import com.pyokemon.did.domain.repository.UserWalletRepository;
import com.pyokemon.did.remote.commonAcaPy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.AcaPyReceiveInvitationResponse;
import com.pyokemon.did.remote.commonAcaPy.dto.response.InvitationResponse.Invitation;
import com.pyokemon.did.remote.tenantAcaPy.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.userAcaPy.RemoteUserAcaPyService;
import com.pyokemon.did.service.impl.AcaPyConnectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.pyokemon.common.exception.code.DidErrorCodes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcaPyConnectionServiceTest {

    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;

    @Mock
    private RemoteUserAcaPyService remoteUserAcaPyService;

    @Mock
    private AcaPyConnectionRepository acaPyConnectionRepository;

    @Mock
    private TenantWalletService tenantWalletService;

    @Mock
    private UserWalletRepository userWalletRepository;

    @InjectMocks
    private AcaPyConnectionServiceImpl acaPyConnectionService;

    private final Long TENANT_ID = 1L;
    private final Long USER_ID = 2L;
    private final String TOKEN = "test-token";
    private final String INVITATION_MSG_ID = "test-invitation-msg";

    private TenantWallet tenantWallet;
    private UserWallet userWallet;
    private AcaPyCreateInvitationResponse createInvitationResponse;
    private AcaPyReceiveInvitationResponse receivedInvitationResponse;

    @BeforeEach
    void setUp() {
        // 테넌트 지갑 설정
        tenantWallet = TenantWallet.builder()
                .tenantId(TENANT_ID)
                .token(TOKEN)
                .publicDid("test-public-did")
                .publicVerkey("test-public-verkey")
                .build();

        // 사용자 지갑 설정
        userWallet = UserWallet.builder()
                .userId(USER_ID)
                .token(TOKEN)
                .build();

        // 초대장 응답 설정
        Invitation invitation = new Invitation();
        String INVITATION_ID = "test-invitation-id";
        invitation.setId(INVITATION_ID);
        invitation.setLabel("Invitation to USER AcaPy from TENANT AcaPy");

        createInvitationResponse = new AcaPyCreateInvitationResponse();
        createInvitationResponse.setInvitation(invitation);
        createInvitationResponse.setInviMsgId(INVITATION_MSG_ID);

        // 초대장 수락 응답 설정
        receivedInvitationResponse = new AcaPyReceiveInvitationResponse();
        receivedInvitationResponse.setState("deleted");
    }

    @Test
    @DisplayName("AcaPy 연결 생성 성공 테스트")
    void createAcaPyConnection_Success() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(userWalletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(remoteUserAcaPyService.acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class)))
                .thenReturn(receivedInvitationResponse);

        // When
        assertDoesNotThrow(() -> acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        // Then
        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(userWalletRepository).findByUserId(USER_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));

        ArgumentCaptor<AcaPyConnection> acaPyConnectionCaptor = ArgumentCaptor.forClass(AcaPyConnection.class);
        verify(acaPyConnectionRepository).save(acaPyConnectionCaptor.capture());

        AcaPyConnection savedConnection = acaPyConnectionCaptor.getValue();
        assertEquals(INVITATION_MSG_ID, savedConnection.getInviMsgId());
        assertEquals(TENANT_ID, savedConnection.getTenantId());
        assertEquals(USER_ID, savedConnection.getUserId());
        assertEquals(ConnectionStatus.PENDING, savedConnection.getStatus());
        assertNull(savedConnection.getConnectionId());

        verify(remoteUserAcaPyService).acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class));
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
        verify(tenantWalletService, never()).getWalletByTenantId(anyLong());
        verify(userWalletRepository, never()).findByUserId(anyLong());
        verify(remoteTenantAcaPyService, never()).createInvitation(anyString(), any(AcaPyCreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(anyString(), any(Invitation.class));
    }

    @Test
    @DisplayName("테넌트 지갑이 존재하지 않는 경우 예외 발생 테스트")
    void createAcaPyConnection_TenantWalletNotFound() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("테넌트 지갑이 존재하지 않습니다.", exception.getMessage());
        assertEquals(WALLET_NOT_FOUND, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(userWalletRepository, never()).findByUserId(anyLong());
        verify(remoteTenantAcaPyService, never()).createInvitation(anyString(), any(AcaPyCreateInvitationRequest.class));
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(anyString(), any(Invitation.class));
    }

    @Test
    @DisplayName("초대장 생성 실패 테스트 - null 응답")
    void createAcaPyConnection_InvitationCreationFailed_NullResponse() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장이 생성에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository, never()).findByUserId(anyLong());
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(anyString(), any(Invitation.class));
    }

    @Test
    @DisplayName("초대장 생성 실패 테스트 - null 초대장")
    void createAcaPyConnection_InvitationCreationFailed_NullInvitation() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        
        AcaPyCreateInvitationResponse nullInvitationResponse = new AcaPyCreateInvitationResponse();
        nullInvitationResponse.setInvitation(null);
        
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(nullInvitationResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장이 생성에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository, never()).findByUserId(anyLong());
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(anyString(), any(Invitation.class));
    }

    @Test
    @DisplayName("사용자 지갑이 존재하지 않는 경우 예외 발생 테스트")
    void createAcaPyConnection_UserWalletNotFound() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(userWalletRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("사용자 지갑이 존재하지 않습니다.", exception.getMessage());
        assertEquals(WALLET_NOT_FOUND, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository).findByUserId(USER_ID);
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(anyString(), any(Invitation.class));
    }

    @Test
    @DisplayName("초대장 수락 실패 테스트 - null 응답")
    void createAcaPyConnection_InvitationReceiveFailed_NullResponse() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(userWalletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        when(remoteUserAcaPyService.acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class)))
                .thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장 수락에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_RECEIVE_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository).findByUserId(USER_ID);
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService).acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class));
    }

    @Test
    @DisplayName("초대장 수락 실패 테스트 - 잘못된 상태")
    void createAcaPyConnection_InvitationReceiveFailed_InvalidState() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(userWalletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        
        AcaPyReceiveInvitationResponse invalidStateResponse = new AcaPyReceiveInvitationResponse();
        invalidStateResponse.setState("failed");
        
        when(remoteUserAcaPyService.acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class)))
                .thenReturn(invalidStateResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("초대장 수락에 실패했습니다.", exception.getMessage());
        assertEquals(INVITATION_RECEIVE_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository).findByUserId(USER_ID);
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService).acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class));
    }

    @Test
    @DisplayName("연결 저장 중 예외 발생 테스트")
    void createAcaPyConnection_SaveConnectionFailed() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(createInvitationResponse);
        when(userWalletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        when(acaPyConnectionRepository.save(any(AcaPyConnection.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("AcaPy간 연결 생성에 실패했습니다.", exception.getMessage());
        assertEquals(CONNECTION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository).findByUserId(USER_ID);
        verify(acaPyConnectionRepository).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(eq(TOKEN), any(Invitation.class));
    }

    @Test
    @DisplayName("외부 API 호출 중 예외 발생 테스트")
    void createAcaPyConnection_ExternalApiException() {
        // Given
        when(acaPyConnectionRepository.existsByTenantIdAndUserId(TENANT_ID, USER_ID)).thenReturn(false);
        when(tenantWalletService.getWalletByTenantId(TENANT_ID)).thenReturn(Optional.of(tenantWallet));
        when(remoteTenantAcaPyService.createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class)))
                .thenThrow(new RuntimeException("외부 API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                acaPyConnectionService.createAcaPyConnection(TENANT_ID, USER_ID));

        assertEquals("AcaPy간 연결 생성에 실패했습니다.", exception.getMessage());
        assertEquals(CONNECTION_CREATION_FAILED, exception.getErrorCode());

        verify(acaPyConnectionRepository).existsByTenantIdAndUserId(TENANT_ID, USER_ID);
        verify(tenantWalletService).getWalletByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq(TOKEN), any(AcaPyCreateInvitationRequest.class));
        verify(userWalletRepository, never()).findByUserId(anyLong());
        verify(acaPyConnectionRepository, never()).save(any(AcaPyConnection.class));
        verify(remoteUserAcaPyService, never()).acaPyReceiveInvitation(anyString(), any(Invitation.class));
    }
}
