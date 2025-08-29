package com.pyokemon.did.domain.repository;

import org.springframework.data.repository.CrudRepository;

import com.pyokemon.did.domain.IssuedProof;

import java.util.Optional;

public interface IssuedProofRepository extends CrudRepository<IssuedProof, String> {
    /**
     * Finds an IssuedProof entity by its presExId.
     * This works because the presExId field is annotated with @Indexed.
     *
     * @param presExId The presentation exchange ID to search for.
     * @return An Optional containing the found IssuedProof, or empty if not found.
     */
    Optional<IssuedProof> findByPresExId(String presExId);

    /**
     * Deletes an IssuedProof entity by its presExId.
     * This also requires the @Indexed annotation on the presExId field.
     *
     * @param presExId The presentation exchange ID of the entity to delete.
     * @return The number of entities deleted (0 or 1).
     */
    long deleteByPresExId(String presExId);

}
