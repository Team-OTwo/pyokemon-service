package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.AcaPyConnection;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AcaPyConnectionRepository {

    /**
     * AcaPyConnection을 저장합니다.
     *
     * @param acaPyConnection 저장할 AcaPyConnection
     * @return 저장된 AcaPyConnection의 ID
     */
    Long save(AcaPyConnection acaPyConnection);

    /**
     * ID로 AcaPyConnection을 조회합니다.
     *
     * @param id 조회할 AcaPyConnection의 ID
     * @return AcaPyConnection (Optional)
     */
    Optional<AcaPyConnection> findById(Long id);

    /**
     * connection_id로 AcaPyConnection을 조회합니다.
     *
     * @param connectionId 조회할 connection_id
     * @return AcaPyConnection (Optional)
     */
    Optional<AcaPyConnection> findByConnectionId(String connectionId);

    /**
     * tenant_id로 AcaPyConnection 목록을 조회합니다.
     *
     * @param tenantId 조회할 tenant_id
     * @return AcaPyConnection 목록
     */
    List<AcaPyConnection> findByTenantId(Long tenantId);

    /**
     * user_id로 AcaPyConnection 목록을 조회합니다.
     *
     * @param userId 조회할 user_id
     * @return AcaPyConnection 목록
     */
    List<AcaPyConnection> findByUserId(Long userId);

    /**
     * status로 AcaPyConnection 목록을 조회합니다.
     *
     * @param status 조회할 status
     * @return AcaPyConnection 목록
     */
    List<AcaPyConnection> findByStatus(AcaPyConnection.ConnectionStatus status);

    /**
     * AcaPyConnection을 업데이트합니다.
     *
     * @param acaPyConnection 업데이트할 AcaPyConnection
     * @return 업데이트된 행 수
     */
    int update(AcaPyConnection acaPyConnection);

    /**
     * ID로 AcaPyConnection을 삭제합니다.
     *
     * @param id 삭제할 AcaPyConnection의 ID
     * @return 삭제된 행 수
     */
    int deleteById(Long id);

    /**
     * connection_id로 AcaPyConnection을 삭제합니다.
     *
     * @param connectionId 삭제할 connection_id
     * @return 삭제된 행 수
     */
    int deleteByConnectionId(String connectionId);
}
