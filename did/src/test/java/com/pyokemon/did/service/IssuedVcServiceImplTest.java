package com.pyokemon.did.service;

import static com.pyokemon.common.exception.code.DidErrorCodes.VC_ISSUANCE_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.IssuedProof;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.IssuedVc.VcStatus;
import com.pyokemon.did.domain.Wallet;
import com.pyokemon.did.domain.repository.IssuedProofRepository;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;
import com.pyokemon.did.remote.acapy.common.dto.request.CreateInvitationRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.IssueCredentialRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.PresentProofRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.CreateInvitationResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.IssueCredentialResponse;
import com.pyokemon.did.remote.acapy.common.dto.response.PresentProofResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.impl.IssuedVcServiceImpl;
import org.springframework.retry.RetryException;

@ExtendWith(MockitoExtension.class)
class IssuedVcServiceImplTest {

  @Mock
  private IssuedVcRepository issuedVcRepository;

  @Mock
  private IssuedProofRepository issuedProofRepository;

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

  private static final Long TENANT_ID = 1L;
  private static final Long USER_ID = 2L;
  private static final Long BOOKING_ID = 3L;
  private static final Long EVENT_SCHEDULE_ID = 4L;
  private static final Long SEAT_ID = 5L;

  @BeforeEach
  void setUp() {
    bookingEvent = new BookingEvent();
    bookingEvent.setAccountId(USER_ID);
    bookingEvent.setTenantId(TENANT_ID);
    bookingEvent.setBookingId(BOOKING_ID);
    bookingEvent.setEventScheduleId(EVENT_SCHEDULE_ID);
    bookingEvent.setSeatId(SEAT_ID);

    tenantWallet = new Wallet();
    tenantWallet.setAccountId(TENANT_ID);
    tenantWallet.setToken("tenant-token");
    tenantWallet.setPublicDid("tenant-did");

    userWallet = new Wallet();
    userWallet.setAccountId(USER_ID);
    userWallet.setToken("user-token");
    userWallet.setPublicDid("user-did");

    connection = new AcaPyConnection();
    connection.setConnectionId("test-connection-id");
  }

  @Test
    @DisplayName("issueCredential 성공 플로우")
    void issueCredential_success() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);

        PresentProofResponse proofResp = new PresentProofResponse();
        proofResp.setPresExId("pres-ex-1");
        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(proofResp);

        CreateInvitationResponse inviResp = new CreateInvitationResponse();
        inviResp.setInvitationUrl("http://verify.example/invitation");
        when(remoteTenantAcaPyService.createInvitation(anyString(), any(CreateInvitationRequest.class)))
                .thenReturn(inviResp);

        assertDoesNotThrow(() -> issuedVcService.issueCredential(bookingEvent));

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(remoteTenantAcaPyService).presentProof(anyString(), any(PresentProofRequest.class));
        verify(remoteTenantAcaPyService).createInvitation(anyString(), any(CreateInvitationRequest.class));
        verify(issuedProofRepository).save(any(IssuedProof.class));
        verify(issuedVcRepository).save(any(IssuedVc.class));
    }

  @Test
    @DisplayName("이미 발급된 VC가 있으면 조기 종료")
    void issueCredential_alreadyIssued() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(true);

        assertDoesNotThrow(() -> issuedVcService.issueCredential(bookingEvent));

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verifyNoMoreInteractions(acaPyConnectionService, walletService, remoteTenantAcaPyService);
        verify(issuedProofRepository, never()).save(any());
        verify(issuedVcRepository, never()).save(any());
    }

  @Test
    @DisplayName("requestCredentialIssuance 응답 null이면 예외")
    void issueCredential_nullIssueResponse() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verifyNoMoreInteractions(remoteTenantAcaPyService);
        verify(issuedProofRepository, never()).save(any());
        verify(issuedVcRepository, never()).save(any());
    }

  @Test
    @DisplayName("requestCredentialIssuance 응답의 credExId null이면 예외")
    void issueCredential_nullCredExId() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId(null);
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verifyNoMoreInteractions(remoteTenantAcaPyService);
        verify(issuedProofRepository, never()).save(any());
        verify(issuedVcRepository, never()).save(any());
    }

  @Test
    @DisplayName("requestPresentProof 응답 null이면 예외")
    void issueCredential_nullPresentProofResponse() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);

        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(remoteTenantAcaPyService).presentProof(anyString(), any(PresentProofRequest.class));
        verifyNoMoreInteractions(remoteTenantAcaPyService);
        verify(issuedProofRepository, never()).save(any());
        verify(issuedVcRepository, never()).save(any());
    }

  @Test
    @DisplayName("requestPresentProof 응답의 presExId null이면 예외")
    void issueCredential_nullPresExId() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);

        PresentProofResponse proofResp = new PresentProofResponse();
        proofResp.setPresExId(null);
        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(proofResp);

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(remoteTenantAcaPyService).presentProof(anyString(), any(PresentProofRequest.class));
        verifyNoMoreInteractions(remoteTenantAcaPyService);
        verify(issuedProofRepository, never()).save(any());
        verify(issuedVcRepository, never()).save(any());
    }

  @Test
    @DisplayName("requestInvitation 응답 null이면 예외")
    void issueCredential_nullCreateInvitationResponse() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);
        PresentProofResponse proofResp = new PresentProofResponse();
        proofResp.setPresExId("pres-ex-1");
        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(proofResp);

        when(remoteTenantAcaPyService.createInvitation(anyString(), any(CreateInvitationRequest.class)))
                .thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(remoteTenantAcaPyService).presentProof(anyString(), any(PresentProofRequest.class));
        verify(remoteTenantAcaPyService).createInvitation(anyString(), any(CreateInvitationRequest.class));
        verify(issuedProofRepository, never()).save(any(IssuedProof.class));
        verify(issuedVcRepository, never()).save(any(IssuedVc.class));
    }

  @Test
    @DisplayName("requestInvitation 응답의 invitationUrl 없으면 예외")
    void issueCredential_emptyInvitationUrl() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);
        PresentProofResponse proofResp = new PresentProofResponse();
        proofResp.setPresExId("pres-ex-1");
        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(proofResp);

        CreateInvitationResponse inviResp = new CreateInvitationResponse();
        inviResp.setInvitationUrl("");
        when(remoteTenantAcaPyService.createInvitation(anyString(), any(CreateInvitationRequest.class)))
                .thenReturn(inviResp);

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급에 실패했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());

        verify(issuedVcRepository).existsByBookingIdAndIssued(BOOKING_ID);
        verify(acaPyConnectionService).getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID);
        verify(walletService).getWalletByAccountIdOrThrow(TENANT_ID);
        verify(walletService).getWalletByAccountIdOrThrow(USER_ID);
        verify(remoteTenantAcaPyService).issueCredential(anyString(), any(IssueCredentialRequest.class));
        verify(remoteTenantAcaPyService).presentProof(anyString(), any(PresentProofRequest.class));
        verify(remoteTenantAcaPyService).createInvitation(anyString(), any(CreateInvitationRequest.class));
        verify(issuedProofRepository, never()).save(any(IssuedProof.class));
        verify(issuedVcRepository, never()).save(any(IssuedVc.class));
    }

  @Test
    @DisplayName("외부 API 호출 예외는 비즈니스 예외로 변환")
    void issueCredential_apiThrowsWrapped() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenThrow(new RuntimeException("API 실패"));

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급 중 오류가 발생했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());
    }

  @Test
    @DisplayName("redis 예외는 비즈니스 예외로 변환")
    void issueCredential_redisThrowsWrapped() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);

        PresentProofResponse proofResp = new PresentProofResponse();
        proofResp.setPresExId("pres-ex-1");
        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(proofResp);

        CreateInvitationResponse inviResp = new CreateInvitationResponse();
        inviResp.setInvitationUrl("http://verify.example/invitation");
        when(remoteTenantAcaPyService.createInvitation(anyString(), any(CreateInvitationRequest.class)))
                .thenReturn(inviResp);

        when(issuedProofRepository.save(any(IssuedProof.class)))
                .thenThrow(new RuntimeException("Redis Error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급 중 오류가 발생했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());
    }

  @Test
    @DisplayName("DB 예외는 비즈니스 예외로 변환")
    void issueCredential_dbThrowsWrapped() {
        when(issuedVcRepository.existsByBookingIdAndIssued(BOOKING_ID)).thenReturn(false);
        when(acaPyConnectionService.getActiveAcaPyConnectionOrThrow(TENANT_ID, USER_ID)).thenReturn(connection);
        when(walletService.getWalletByAccountIdOrThrow(TENANT_ID)).thenReturn(tenantWallet);
        when(walletService.getWalletByAccountIdOrThrow(USER_ID)).thenReturn(userWallet);

        IssueCredentialResponse issueResp = new IssueCredentialResponse();
        issueResp.setCredExId("cred-ex-1");
        when(remoteTenantAcaPyService.issueCredential(anyString(), any(IssueCredentialRequest.class)))
                .thenReturn(issueResp);

        PresentProofResponse proofResp = new PresentProofResponse();
        proofResp.setPresExId("pres-ex-1");
        when(remoteTenantAcaPyService.presentProof(anyString(), any(PresentProofRequest.class)))
                .thenReturn(proofResp);

        CreateInvitationResponse inviResp = new CreateInvitationResponse();
        inviResp.setInvitationUrl("http://verify.example/invitation");
        when(remoteTenantAcaPyService.createInvitation(anyString(), any(CreateInvitationRequest.class)))
                .thenReturn(inviResp);

        when(issuedVcRepository.save(any(IssuedVc.class)))
                .thenThrow(new RuntimeException("DB error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> issuedVcService.issueCredential(bookingEvent));
        assertEquals("VC 발급 중 오류가 발생했습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateCredExId 성공")
    void updateCredExId_success() {
        IssuedVc issued = IssuedVc.builder().verifyInviUrl("http://verify.example/inv")
                .presExId("pres-ex-xyz").status(VcStatus.PENDING).build();

        when(issuedVcRepository.findByBookingId(anyLong())).thenReturn(Optional.of(issued));
        when(issuedVcRepository.update(any(IssuedVc.class))).thenReturn(1);

        assertDoesNotThrow(() -> issuedVcService.updateCredExId(BOOKING_ID, "test-cred-ex-id"));

        verify(issuedVcRepository).findByBookingId(BOOKING_ID);
        verify(issuedVcRepository).update(issued);
    }

    @Test
    @DisplayName("updateCredExId 실패 - findByBookingId throws ")
    void updateCredExId_repositoryThrows() {
        when(issuedVcRepository.findByBookingId(anyLong())).thenThrow(new RetryException("retry - vc not found"));

        RetryException ex = assertThrows(RetryException.class, () -> issuedVcService.updateCredExId(BOOKING_ID, "test-cred-ex-id"));

        assertEquals(ex.getMessage(), "retry - vc not found");
        verify(issuedVcRepository).findByBookingId(BOOKING_ID);
    }

    @Test
    @DisplayName("updateCredExId 실패 - status != pending")
    void updateCredExId_status_invalid() {
        IssuedVc issued = IssuedVc.builder().verifyInviUrl("http://verify.example/inv")
                .presExId("pres-ex-xyz").status(VcStatus.ISSUED).build();

        when(issuedVcRepository.findByBookingId(anyLong())).thenReturn(Optional.of(issued));

        assertDoesNotThrow(() -> issuedVcService.updateCredExId(BOOKING_ID, "test-cred-ex-id"));

        verify(issuedVcRepository).findByBookingId(BOOKING_ID);
        verify(issuedVcRepository, never()).update(issued);
    }

    @Test
    @DisplayName("updateCredExId 실패 - update throws")
    void updateCredExId_update_fails() {
        IssuedVc issued = IssuedVc.builder().verifyInviUrl("http://verify.example/inv")
                .presExId("pres-ex-xyz").status(VcStatus.PENDING).build();

        when(issuedVcRepository.findByBookingId(anyLong())).thenReturn(Optional.of(issued));
        when(issuedVcRepository.update(any(IssuedVc.class))).thenThrow(new RuntimeException("db fails"));

        RetryException ex = assertThrows(RetryException.class, () -> issuedVcService.updateCredExId(BOOKING_ID, "test-cred-ex-id"));

        assertEquals(ex.getMessage(), "retry - fail to update vc");
        verify(issuedVcRepository).findByBookingId(BOOKING_ID);
        verify(issuedVcRepository).update(issued);
    }


  @Test
  @DisplayName("sendVerifiyInviUrlOrThrow 성공")
  void sendVerifiyInviUrlOrThrow_success() {
    IssuedVc issued = IssuedVc.builder().verifyInviUrl("http://verify.example/inv")
        .presExId("pres-ex-xyz").status(VcStatus.ISSUED).build();

    when(issuedVcRepository.findByUserIdAndTenantIdAndBookingIdAndStatus(USER_ID, TENANT_ID,
        BOOKING_ID, VcStatus.ISSUED)).thenReturn(Optional.of(issued));

    Map<String, String> result =
        issuedVcService.sendVerifiyInviUrlOrThrow(USER_ID, TENANT_ID, BOOKING_ID);

    assertEquals("http://verify.example/inv", result.get("verifyInviUrl"));
    assertEquals("pres-ex-xyz", result.get("presExId"));
  }

  @Test
    @DisplayName("sendVerifiyInviUrlOrThrow 미발견시 예외")
    void sendVerifiyInviUrlOrThrow_notFound() {
        when(issuedVcRepository.findByUserIdAndTenantIdAndBookingIdAndStatus(USER_ID, TENANT_ID, BOOKING_ID, VcStatus.ISSUED))
                .thenReturn(Optional.empty());

        com.pyokemon.common.exception.BusinessException ex = assertThrows(com.pyokemon.common.exception.BusinessException.class,
                () -> issuedVcService.sendVerifiyInviUrlOrThrow(USER_ID, TENANT_ID, BOOKING_ID));
        assertEquals("발급된 VC를 찾을 수 없습니다.", ex.getMessage());
        assertEquals(VC_ISSUANCE_FAILED, ex.getErrorCode());
    }
}
