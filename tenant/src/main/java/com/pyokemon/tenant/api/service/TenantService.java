package com.pyokemon.tenant.api.service;

import com.pyokemon.tenant.api.repository.TenantDidRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.tenant.api.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

  private final TenantRepository tenantRepository;
  private final TenantDidRepository tenantDidRepository;


}
