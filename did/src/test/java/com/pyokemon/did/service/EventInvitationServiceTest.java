package com.pyokemon.did.service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.did.domain.WalletMetadata;
import com.pyokemon.did.domain.dto.request.EventInvitationRequest.CreateEventInvitationRequest;
import com.pyokemon.did.domain.repository.WalletMetadataRepository;
import com.pyokemon.did.remote.tenantacapy.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.tenantacapy.dto.request.InvitationRequest.AcaPyCreateInvitationRequest;
import com.pyokemon.did.remote.common.InvitationResponse.AcaPyCreateInvitationResponse;
import com.pyokemon.did.remote.common.InvitationResponse.Invitation;
import com.pyokemon.did.service.impl.TenantInvitationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventInvitationServiceTest {

    private static final Logger log = LoggerFactory.getLogger(EventInvitationServiceTest.class);
    @Mock
    private RemoteTenantAcaPyService remoteTenantAcaPyService;

    @Mock
    private WalletMetadataRepository walletMetadataRepository;

    @Mock
    private EventInvitationRepository eventInvitationRepository;

    @InjectMocks
    private TenantInvitationServiceImpl eventInvitationService;

    private static final Long TENANT_ID = 1L;
    private static final Long EVENT_ID = 100L;
    private static final String WALLET_KEY = "test-wallet-key";
    private static final String TOKEN = "test-token";
    private static final String OOB_ID = "test-oob-id";
    private static final String INVI_MSG_ID = "test-invi-msg-id";
    private static final String INVITATION_URL = "https://example.com/invitation";

    private CreateEventInvitationRequest request;
    private WalletMetadata walletMetadata;
    private AcaPyCreateInvitationResponse invitationResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventInvitationService, "walletKey", WALLET_KEY);

        // 요청 DTO 설정
        request = CreateEventInvitationRequest.builder()
                .tenantId(TENANT_ID)
                .eventId(EVENT_ID)
                .build();

        // 지갑 메타데이터 설정
        walletMetadata = WalletMetadata.builder()
                .tenantId(TENANT_ID)
                .key("wallet-key")
                .token(TOKEN)
                .build();

        // 초대장 응답 설정
        Invitation invitation = Invitation.builder()
                .type("https://didcomm.org/out-of-band/1.0/invitation")
                .id("test-id")
                .label("invitation:" + EVENT_ID)
                .handshakeProtocols(Collections.singletonList("https://didcomm.org/didexchange/1.0"))
                .services(Collections.singletonList("test-service"))
                .build();

        invitationResponse = AcaPyCreateInvitationResponse.builder()
                .oobId(OOB_ID)
                .inviMsgId(INVI_MSG_ID)
                .invitationUrl(INVITATION_URL)
                .state("initial")
                .trace(false)
                .invitation(invitation)
                .build();
    }

    @Test
    @DisplayName("이벤트 초대장 프로비저닝 성공 테스트")
    void createEventInvitation_Success() {
        // Given
        when(walletMetadataRepository.findByTenantId(TENANT_ID)).thenReturn(Optional.of(walletMetadata));
        when(remoteTenantAcaPyService.createInvitation(anyString(), any(AcaPyCreateInvitationRequest.class)))
                .thenReturn(invitationResponse);
        when(eventInvitationRepository.save(any(EventInvitation.class))).thenReturn(1);

        // When
        eventInvitationService.createEventInvitation(request);

        // Then
        verify(walletMetadataRepository).findByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(eq("Bearer " + TOKEN), any(AcaPyCreateInvitationRequest.class));
        
        // EventInvitation 저장 검증
        ArgumentCaptor<EventInvitation> eventInvitationCaptor = ArgumentCaptor.forClass(EventInvitation.class);
        verify(eventInvitationRepository).save(eventInvitationCaptor.capture());

        EventInvitation savedInvitation = eventInvitationCaptor.getValue();
        log.info("isValid 기본값: {}", savedInvitation.isValid());
        assertEquals(EVENT_ID, savedInvitation.getEventId());
        assertEquals(TENANT_ID, savedInvitation.getTenantId());
        assertEquals(OOB_ID, savedInvitation.getOobId());
        assertEquals(INVITATION_URL, savedInvitation.getInvitationUrl());
        assertTrue(savedInvitation.isValid(), "isValid 필드는 기본값으로 true여야 합니다");
    }

    @Test
    @DisplayName("테넌트 지갑이 존재하지 않는 경우 예외 발생 테스트")
    void createEventInvitation_WalletNotFound() {
        // Given
        when(walletMetadataRepository.findByTenantId(TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventInvitationService.createEventInvitation(request);
        });

        assertEquals(DidErrorCodes.WALLET_NOTFOUND, exception.getErrorCode());
        assertEquals("테넌트 지갑이 존재하지 않습니다.", exception.getMessage());
        
        verify(walletMetadataRepository).findByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService, never()).createInvitation(anyString(), any(AcaPyCreateInvitationRequest.class));
        verify(eventInvitationRepository, never()).save(any(EventInvitation.class));
    }

    @Test
    @DisplayName("ACA-PY 클라이언트 예외 처리 테스트")
    void createEventInvitation_AcapyClientException() {
        // Given
        when(walletMetadataRepository.findByTenantId(TENANT_ID)).thenReturn(Optional.of(walletMetadata));
        when(remoteTenantAcaPyService.createInvitation(anyString(), any(AcaPyCreateInvitationRequest.class)))
                .thenThrow(new RuntimeException("ACA-PY 클라이언트 오류"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventInvitationService.createEventInvitation(request);
        });

        // 예외 검증
        assertEquals(DidErrorCodes.INVITATION_CREATION_FAILED, exception.getErrorCode());
        assertEquals("초대장 생성에 실패했습니다.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("ACA-PY 클라이언트 오류", exception.getCause().getMessage());

        verify(walletMetadataRepository).findByTenantId(TENANT_ID);
        verify(remoteTenantAcaPyService).createInvitation(anyString(), any(AcaPyCreateInvitationRequest.class));
        verify(eventInvitationRepository, never()).save(any(EventInvitation.class));
    }
}
