package com.pyokemon.did.service.impl;

import static com.pyokemon.did.domain.IssuedVc.VcStatus.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.did.service.TenantWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TenantWebhookServiceImpl implements TenantWebhookService {

}
