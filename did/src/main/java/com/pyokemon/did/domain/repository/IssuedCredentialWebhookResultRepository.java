package com.pyokemon.did.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.pyokemon.did.domain.IssuedCredentialWebhookResult;

@Repository
public interface IssuedCredentialWebhookResultRepository
    extends CrudRepository<IssuedCredentialWebhookResult, Long> {

  Optional<IssuedCredentialWebhookResult> findByCredExId(String credExId);
}
