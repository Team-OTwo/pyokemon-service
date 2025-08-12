package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.EventInvitation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 이벤트 초대장 정보에 대한 데이터 액세스 인터페이스
 */
@Mapper
public interface EventInvitationRepository {

    /**
     * 이벤트 초대장 정보를 저장합니다.
     *
     * @param eventInvitation 저장할 이벤트 초대장 정보
     * @return 저장된 행 수
     */
    int save(EventInvitation eventInvitation);

    /**
     * 유효한(is_valid=true) 이벤트 초대장 정보를 이벤트 ID로 조회합니다.
     *
     * @param eventId 이벤트 ID
     * @return 조회된 이벤트 초대장 정보 (없을 경우 빈 Optional)
     */
    Optional<EventInvitation> findValidByEventId(Long eventId);
    
    /**
     * 유효한(is_valid=true) 이벤트 초대장 정보 목록을 테넌트 ID로 조회합니다.
     *
     * @param tenantId 테넌트 ID
     * @return 조회된 이벤트 초대장 정보 목록
     */
    List<EventInvitation> findValidByTenantId(Long tenantId);

    /**
     * 이벤트 ID에 해당하는 이벤트 초대장의 유효성을 false로 변경합니다.
     *
     * @param eventId 이벤트 ID
     * @return 업데이트된 행 수
     */
    int invalidateByEventId(Long eventId);
}