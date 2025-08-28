package com.pyokemon.account.tenant.bff.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.account.tenant.bff.dto.TenantDto;
import com.pyokemon.account.tenant.bff.service.TenantBffService;
import com.pyokemon.common.dto.IdsRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bff/tenants")
@RequiredArgsConstructor
public class TenantBffController {

  private final TenantBffService tenantBffService;

  @GetMapping("/{tenantId}")
  public ResponseEntity<TenantDto> getTenantById(@PathVariable Long tenantId) {
    TenantDto responseDto = tenantBffService.findByTenantId(tenantId);
    return ResponseEntity.ok(responseDto);
  }

  @PostMapping("/_batch")
  public List<TenantDto> findTenantsBatch(@RequestBody IdsRequest req) {
    return tenantBffService.findTenantsBatch(req.getIds());
  }
}
