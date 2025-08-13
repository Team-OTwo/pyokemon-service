package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.ConnectionMetadata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ConnectionMetadataRepository {

    int save(ConnectionMetadata connectionMetadata);

    Optional<ConnectionMetadata> findById(Long id);

    Optional<ConnectionMetadata> findByConnId(String connId);

    Optional<ConnectionMetadata> findByTenantId(String tenantId);

    List<ConnectionMetadata> findByUserId(Long userId);

    List<ConnectionMetadata> findByStatus(ConnectionMetadata.ConnectionStatus status);

    List<ConnectionMetadata> findByUserIdAndTenantId(Long userId, Long tenantId);

    int updateStatus(String connId, ConnectionMetadata.ConnectionStatus status);

    boolean existsByConnId(String connId);

    int deleteByConnId(String connId);
}
