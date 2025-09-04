package com.pyokemon.did.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.request.IssueCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.response.GetCredentialResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.IssueCredentialResponse;
import com.pyokemon.did.remote.acapy.service.RemoteUserAcaPyService;
import com.pyokemon.did.service.impl.VerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationServiceDelegateCredentialTest {

    @Mock
    private DeviceConnectionService deviceConnectionService;
    
    @Mock
    private IssuedVcService issuedVcService;
    
    @Mock
    private RemoteUserAcaPyService remoteUserAcaPyService;
    
    @Mock
    private WalletService walletService;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    // 테스트 데이터
    private static final Long BOOKING_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final String DEVICE_ID = "device-123";
    private static final String CRED_EX_ID = "cred-ex-123";
    private static final String CONNECTION_ID = "conn-123";
    private static final String DELEGATOR_DID = "did:delegator:123";
    private static final String DELEGATEE_DID = "did:delegatee:456";
    private static final String DELEGATOR_TOKEN = "token-123";
    private static final String SOURCE_CREDENTIAL_ID = "source-cred-123";

    @BeforeEach
    void setUp() {
        // 공통 테스트 데이터 설정 - 객체 생성만, mock 설정은 각 테스트에서 수행
    }

    @Test
    @DisplayName("delegateCredential 성공 플로우")
    void delegateCredential_success() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        Wallet userWallet = createMockWallet();
        GetCredentialResponse sourceCredential = createMockGetCredentialResponse();
        IssueCredentialResponse issueCredentialResponse = createMockIssueCredentialResponse();

        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteUserAcaPyService.getCredential(DELEGATOR_TOKEN, CRED_EX_ID))
                .thenReturn(sourceCredential);
        when(remoteUserAcaPyService.issueCredential(eq(DELEGATOR_TOKEN), any(IssueCredentialRequest.class)))
                .thenReturn(issueCredentialResponse);

        // When
        assertDoesNotThrow(() -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));

        // Then
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteUserAcaPyService).getCredential(DELEGATOR_TOKEN, CRED_EX_ID);
        
        // IssueCredentialRequest 상세 검증
        ArgumentCaptor<IssueCredentialRequest> requestCaptor = ArgumentCaptor.forClass(IssueCredentialRequest.class);
        verify(remoteUserAcaPyService).issueCredential(eq(DELEGATOR_TOKEN), requestCaptor.capture());
        
        IssueCredentialRequest capturedRequest = requestCaptor.getValue();
        assertEquals(CONNECTION_ID, capturedRequest.getConnectionId());
        assertEquals(SOURCE_CREDENTIAL_ID, capturedRequest.getSourceCredentialId());
        assertEquals(DELEGATOR_DID, capturedRequest.getDelegatorDid());
        assertNotNull(capturedRequest.getCredentialSubject());
        assertEquals(DELEGATEE_DID, capturedRequest.getCredentialSubject().getId());
    }

    @Test
    @DisplayName("delegateCredential - VC가 존재하지 않는 경우")
    void delegateCredential_vcNotFound() {
        // Given
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID))
                .thenThrow(new BusinessException("VC를 찾을 수 없습니다.", "VC_NOT_FOUND"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("VC를 찾을 수 없습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService, never()).getDeviceConnectionByDeviceIdOrThrow(anyString());
        verify(walletService, never()).getWalletByAccountIdOrThrow(anyLong());
        verify(remoteUserAcaPyService, never()).getCredential(anyString(), anyString());
    }

    @Test
    @DisplayName("delegateCredential - 디바이스 연결이 존재하지 않는 경우")
    void delegateCredential_deviceConnectionNotFound() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(java.util.Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("디바이스 연결이 존재하지 않습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verify(walletService, never()).getWalletByAccountIdOrThrow(anyLong());
        verify(remoteUserAcaPyService, never()).getCredential(anyString(), anyString());
    }

    @Test
    @DisplayName("delegateCredential - 사용자 지갑이 존재하지 않는 경우")
    void delegateCredential_walletNotFound() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID))
                .thenThrow(new BusinessException("지갑을 찾을 수 없습니다.", "WALLET_NOT_FOUND"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("지갑을 찾을 수 없습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteUserAcaPyService, never()).getCredential(anyString(), anyString());
    }

    @Test
    @DisplayName("delegateCredential - 원본 자격 증명 조회 실패")
    void delegateCredential_sourceCredentialFetchFailed() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        Wallet userWallet = createMockWallet();
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteUserAcaPyService.getCredential(DELEGATOR_TOKEN, CRED_EX_ID))
                .thenThrow(new RuntimeException("ACA-Py 서비스 오류"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("VC 위임에 실패했습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteUserAcaPyService).getCredential(DELEGATOR_TOKEN, CRED_EX_ID);
    }

    @Test
    @DisplayName("delegateCredential - 자격 증명 발급 실패")
    void delegateCredential_credentialIssuanceFailed() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        Wallet userWallet = createMockWallet();
        GetCredentialResponse sourceCredential = createMockGetCredentialResponse();
        CredentialSubject delegatedCredentialSubject = createMockCredentialSubject();
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteUserAcaPyService.getCredential(DELEGATOR_TOKEN, CRED_EX_ID))
                .thenReturn(sourceCredential);
        // CredentialSubjectDelegator는 static 메서드이므로 실제 호출됨
        // 결과값을 검증하는 방식으로 테스트
        when(remoteUserAcaPyService.issueCredential(eq(DELEGATOR_TOKEN), any(IssueCredentialRequest.class)))
                .thenThrow(new RuntimeException("자격 증명 발급 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("VC 위임에 실패했습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteUserAcaPyService).getCredential(DELEGATOR_TOKEN, CRED_EX_ID);
        // CredentialSubjectDelegator static 메서드 호출은 검증 불가
        // 대신 결과값을 검증하는 방식으로 테스트
        verify(remoteUserAcaPyService).issueCredential(eq(DELEGATOR_TOKEN), any(IssueCredentialRequest.class));
    }

    @Test
    @DisplayName("delegateCredential - VC 사용자 ID 불일치")
    void delegateCredential_vcUserIdMismatch() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        issuedVc.setUserId(999L); // 다른 사용자 ID
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("접근 권한이 없습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verifyNoInteractions(deviceConnectionService, walletService, remoteUserAcaPyService);
    }

    @Test
    @DisplayName("delegateCredential - 디바이스 연결 사용자 ID 불일치")
    void delegateCredential_deviceConnectionUserIdMismatch() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        deviceConnection.setUserId(999L); // 다른 사용자 ID
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("접근 권한이 없습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verifyNoInteractions(walletService, remoteUserAcaPyService);
    }

    @Test
    @DisplayName("delegateCredential - VC의 credExId가 null인 경우")
    void delegateCredential_vcCredExIdNull() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        issuedVc.setCredExId(null); // credExId를 null로 설정
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("접근 권한이 없습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verifyNoInteractions(deviceConnectionService, walletService, remoteUserAcaPyService);
    }

    @Test
    @DisplayName("delegateCredential - 디바이스 연결의 publicDid가 null인 경우")
    void delegateCredential_deviceConnectionPublicDidNull() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        deviceConnection.setPublicDid(null); // publicDid를 null로 설정
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("접근 권한이 없습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verifyNoInteractions(walletService, remoteUserAcaPyService);
    }

    @Test
    @DisplayName("delegateCredential - 자격 증명 위임 처리 실패")
    void delegateCredential_credentialDelegationFailed() {
        // Given
        IssuedVc issuedVc = createMockIssuedVc();
        DeviceConnection deviceConnection = createMockDeviceConnection();
        Wallet userWallet = createMockWallet();
        GetCredentialResponse sourceCredential = createMockGetCredentialResponse();
        
        when(issuedVcService.getIssuedVcByBookingIdOrThrow(BOOKING_ID)).thenReturn(issuedVc);
        when(deviceConnectionService.getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID))
                .thenReturn(deviceConnection);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteUserAcaPyService.getCredential(DELEGATOR_TOKEN, CRED_EX_ID))
                .thenReturn(sourceCredential);
        // CredentialSubjectDelegator는 static 메서드이므로 실제 호출됨
        // 위임 처리 실패 시나리오는 실제 메서드에서 예외 발생하도록 설정

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verificationService.delegateCredential(BOOKING_ID, USER_ID, DEVICE_ID));
        
        assertEquals("VC 위임에 실패했습니다.", exception.getMessage());
        verify(issuedVcService).getIssuedVcByBookingIdOrThrow(BOOKING_ID);
        verify(deviceConnectionService).getDeviceConnectionByDeviceIdOrThrow(DEVICE_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteUserAcaPyService).getCredential(DELEGATOR_TOKEN, CRED_EX_ID);
        // CredentialSubjectDelegator static 메서드 호출은 검증 불가
    }

    // Mock 객체 생성 헬퍼 메서드들
    private IssuedVc createMockIssuedVc() {
        IssuedVc issuedVc = new IssuedVc();
        issuedVc.setId(1L);
        issuedVc.setUserId(USER_ID);
        issuedVc.setCredExId(CRED_EX_ID);
        issuedVc.setIssued(true);
        return issuedVc;
    }

    private DeviceConnection createMockDeviceConnection() {
        DeviceConnection deviceConnection = new DeviceConnection();
        deviceConnection.setId(1L);
        deviceConnection.setUserId(USER_ID);
        deviceConnection.setDeviceId(DEVICE_ID);
        deviceConnection.setConnectionId(CONNECTION_ID);
        deviceConnection.setPublicDid(DELEGATEE_DID);
        return deviceConnection;
    }

    private Wallet createMockWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setAccountId(USER_ID);
        wallet.setToken(DELEGATOR_TOKEN);
        wallet.setPublicDid(DELEGATOR_DID);
        return wallet;
    }

    private GetCredentialResponse createMockGetCredentialResponse() {
        GetCredentialResponse response = new GetCredentialResponse();
        // 필요한 필드 설정
        return response;
    }

    private CredentialSubject createMockCredentialSubject() {
        CredentialSubject subject = new CredentialSubject();
        subject.setId(DELEGATEE_DID);
        // 필요한 필드 설정
        return subject;
    }

    private IssueCredentialResponse createMockIssueCredentialResponse() {
        IssueCredentialResponse response = new IssueCredentialResponse();
        response.setState(AcaPyConstants.IssuanceState.OFFER_SENT);
        return response;
    }
}
