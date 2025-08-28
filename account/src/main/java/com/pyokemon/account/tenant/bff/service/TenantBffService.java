package com.pyokemon.account.tenant.bff.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.tenant.bff.dto.TenantDto;
import com.pyokemon.account.tenant.bff.exception.ResourceNotFoundException;
import com.pyokemon.account.tenant.bff.repository.TenantBffRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantBffService {

  private final TenantBffRepository tenantBffRepository;

  @Transactional(readOnly = true)
  public TenantDto findByTenantId(Long tenantId) {
    return tenantBffRepository.findByTenantId(tenantId).map(TenantDto::from)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
  }

  @Transactional(readOnly = true)
  public List<TenantDto> findTenantsBatch(List<Long> tenantIds) {
      return tenantBffRepository.findTenantsByIdIn(tenantIds);
    }
}
