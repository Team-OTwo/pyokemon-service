package com.pyokemon.did.service;

import static com.pyokemon.common.exception.code.DidErrorCodes.VC_ISSUANCE_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;
import com.pyokemon.did.remote.acapy.common.dto.request.IssueCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.response.IssueCredentialResponse;
import com.pyokemon.did.remote.acapy.service.tenant.RemoteTenantAcaPyService;
import com.pyokemon.did.service.impl.IssuedVcServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssuedVcServiceTest {

    @Mock
    private IssuedVcRepository issuedVcRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private AcaPyConnectionService acaPyConnectionService;

    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;

    @InjectMocks
    private IssuedVcServiceImpl issuedVcService;

    private BookingEvent bookingEvent;
    private Wallet tenantWallet;
    private Wallet userWallet;
    private AcaPyConnection connection;
    private IssueCredentialResponse credentialResponse;

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long BOOKING_ID = 3L;
    private static final Long EVENT_SCHEDULE_ID = 4L;
    private static final Long SEAT_ID = 5L;

    @BeforeEach
    void setUp() {
        // BookingEvent 설정
        bookingEvent = new BookingEvent();
        bookingEvent.setAccountId(USER_ID);
        bookingEvent.setTenantId(TENANT_ID);
        bookingEvent.setBookingId(BOOKING_ID);
        bookingEvent.setEventScheduleId(EVENT_SCHEDULE_ID);
        bookingEvent.setSeatId(SEAT_ID);

        // 테넌트 지갑 설정
        tenantWallet = new Wallet();
        tenantWallet.setAccountId(TENANT_ID);
        tenantWallet.setToken("tenant-token");
        tenantWallet.setPublicDid("tenant-did");

        // 사용자 지갑 설정
        userWallet = new Wallet();
        userWallet.setAccountId(USER_ID);
        userWallet.setToken("user-token");
        userWallet.setPublicDid("user-did");

        // 연결 설정
        connection = new AcaPyConnection();
        connection.setConnectionId("test-connection-id");

        // 자격 증명 응답 설정
        credentialResponse = new IssueCredentialResponse();
        credentialResponse.setCredExId("test-cred-ex-id");
    }

    @Test
    @DisplayName("자격 증명 발급 성공 테스트")
    void issueCredential_Success() throws BusinessException {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(credentialResponse);

        // When
        assertDoesNotThrow(() -> issuedVcService.issueCredential(bookingEvent));

        // Then
        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService, times(2)).getWalletByAccountIdOrThrow(anyLong());
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository).save(any());
    }

    @Test
    @DisplayName("이미 발급된 자격 증명이 있는 경우 테스트")
    void issueCredential_AlreadyIssued() throws BusinessException {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> issuedVcService.issueCredential(bookingEvent));

        // Then
        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService, never()).getActiveAcaPyConnectionOrThrow(anyLong(), anyLong());
        verify(walletService, never()).getWalletByAccountIdOrThrow(anyLong());
        verify(remoteTenantAcaPyService, never()).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository, never()).save(any());
    }

    @Test
    @DisplayName("자격 증명 발급 응답이 null인 경우 예외 발생 테스트")
    void issueCredential_NullResponse() {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", exception.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, exception.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService, times(2)).getWalletByAccountIdOrThrow(anyLong());
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository, never()).save(any());
    }

    @Test
    @DisplayName("자격 증명 발급 응답의 credExId가 null인 경우 예외 발생 테스트")
    void issueCredential_NullCredExId() {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        
        IssueCredentialResponse nullCredExIdResponse = new IssueCredentialResponse();
        nullCredExIdResponse.setCredExId(null);
        
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(nullCredExIdResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", exception.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, exception.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService, times(2)).getWalletByAccountIdOrThrow(anyLong());
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository, never()).save(any());
    }

    @Test
    @DisplayName("연결 조회 중 예외 발생 테스트")
    void issueCredential_ConnectionException() {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID))
                .thenThrow(new BusinessException("연결을 찾을 수 없습니다.", "CONNECTION_NOT_FOUND"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("연결을 찾을 수 없습니다.", exception.getMessage());
        assertEquals("CONNECTION_NOT_FOUND", exception.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService, never()).getWalletByAccountIdOrThrow(anyLong());
        verify(remoteTenantAcaPyService, never()).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository, never()).save(any());
    }

    @Test
    @DisplayName("지갑 조회 중 예외 발생 테스트")
    void issueCredential_WalletException() {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID))
                .thenThrow(new BusinessException("지갑을 찾을 수 없습니다.", "WALLET_NOT_FOUND"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("지갑을 찾을 수 없습니다.", exception.getMessage());
        assertEquals("WALLET_NOT_FOUND", exception.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService, never()).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService, never()).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository, never()).save(any());
    }

    @Test
    @DisplayName("자격 증명 발급 API 호출 중 예외 발생 테스트")
    void issueCredential_ApiException() {
        // Given
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급 중 오류가 발생했습니다.", exception.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, exception.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService, times(2)).getWalletByAccountIdOrThrow(anyLong());
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(issuedVcRepository, never()).save(any());
    }
}
