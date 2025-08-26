package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.AcaPyConnection;
import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.domain.TenantWallet;
import com.pyokemon.did.domain.repository.AcaPyConnectionRepository;
import com.pyokemon.did.domain.repository.DeviceConnectionRepository;
import com.pyokemon.did.domain.repository.IssuedVcRepository;
import com.pyokemon.did.domain.repository.TenantWalletRepository;
import com.pyokemon.did.remote.tenantAcaPy.RemoteTenantAcaPyService;
import com.pyokemon.did.remote.tenantAcaPy.dto.request.AcaPyIssueCredentialRequest;
import com.pyokemon.did.remote.tenantAcaPy.dto.response.AcaPyIssueCredentialResponse;
import com.pyokemon.did.service.IssuedVcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.pyokemon.did.domain.AcaPyConnection.ConnectionStatus.ACTIVE;
import static com.pyokemon.did.domain.IssuedVc.VcStatus.CREDENTIAL_SENT;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssuedVcServiceImpl implements IssuedVcService {

    private final IssuedVcRepository issuedVcRepository;
    private final TenantWalletRepository tenantWalletRepository;
    private final AcaPyConnectionRepository acaPyConnectionRepository;
    private final DeviceConnectionRepository deviceConnectionRepository;
    private final RemoteTenantAcaPyService remoteTenantAcaPyService;

    @Override
    @Transactional
    public void issueVC(Long userId, Long tenantId, Long bookingId) {
        log.info("VC 발급 시작 - userId: {}, tenantId: {}, bookingId: {}", userId, tenantId, bookingId);


            // 1. 기존 발급된 VC 있는지 확인
            if (isIssuedVC(bookingId)) {
                log.info("VC 이미 발급됨 - bookingId: {}", bookingId);
                return;
            }

            // 2. 필수 데이터 조회
            Optional<AcaPyConnection> connection = acaPyConnectionRepository.findByTenantIdAndUserId(tenantId, userId);
            if (connection.isEmpty() || !ACTIVE.equals(connection.get().getStatus())) {
                throw new BusinessException("활성 연결을 찾을 수 없습니다.", "CONNECTION_NOT_FOUND");
            }
            String connectionId = connection.get().getConnectionId();

            Optional<TenantWallet> wallet = tenantWalletRepository.findByTenantId(tenantId);
            if (wallet.isEmpty()) {
                throw new BusinessException("테넌트 지갑을 찾을 수 없습니다.", "WALLET_NOT_FOUND");
            }
            String authorization = "Bearer " + wallet.get().getToken();
            String tenantPublicDid = wallet.get().getPublicDid();

            Optional<DeviceConnection> deviceConnection = deviceConnectionRepository.findByUserId(userId);
            if (deviceConnection.isEmpty()) {
                throw new BusinessException("사용자 디바이스 연결을 찾을 수 없습니다.", "DEVICE_CONNECTION_NOT_FOUND");
            }
            String userPublicDid = deviceConnection.get().getPublicDid();

        try {
            // 3. VC 발급 요청 전송
            log.info("VC 발급 요청 전송 - bookingId: {}", bookingId);
            AcaPyIssueCredentialRequest request = AcaPyIssueCredentialRequest.of(
                    connectionId, "urn:booking:"+bookingId.toString(), tenantPublicDid, userPublicDid);

            AcaPyIssueCredentialResponse response = remoteTenantAcaPyService.acaPyIssueCredential(authorization, request);

            if (response == null || response.getCredExId() == null) {
                log.error("VC 발급 실패 - bookingId: {}", bookingId);
                throw new BusinessException("VC 발급에 실패했습니다.", "VC_ISSUANCE_FAILED");
            }

            // 4. VC 발급 정보 저장
            IssuedVc issuedVc = IssuedVc.builder()
                    .bookingId(bookingId)
                    .credentialExchangeId(response.getCredExId())
                    .status(CREDENTIAL_SENT)
                    .tenantId(tenantId)
                    .credentialId(null) // webhook 받고 업데이트
                    .build();

            issuedVcRepository.save(issuedVc);
            log.info("VC 발급 완료 - bookingId: {}, credExId: {}", bookingId, response.getCredExId());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("VC 발급 중 오류 발생 - bookingId: {}, error: {}", bookingId, e.getMessage(), e);
            throw new BusinessException("VC 발급 중 오류가 발생했습니다.", "VC_ISSUANCE_FAILED");
        }
    }

    /**
     * 특정 booking에 대해 VC가 이미 발급되었는지 확인
     */
    public Boolean isIssuedVC(Long bookingId) {
        return issuedVcRepository.existsByBookingIdAndIssued(bookingId);
    }
}
