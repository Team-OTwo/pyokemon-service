package com.pyokemon.did.service;

import com.pyokemon.did.api.backend.dto.IssueCredentialWebhookDto;
import com.pyokemon.did.api.backend.dto.LdProofWebhookDto;

public interface TenantWebhookService {

  void handleIssueCredentialWebhook(IssueCredentialWebhookDto webhookDto);

  void handleLdProofWebhook(LdProofWebhookDto webhookDto);

}
