package com.pyokemon.did.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.pyokemon.did.domain.IssuedProof;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuedProofRepository extends CrudRepository<IssuedProof, Long> {
  /**
   * Finds an IssuedProof entity by its presExId. This works because the presExId field is annotated
   * with @Indexed.
   *
   * @param presExId The presentation exchange ID to search for.
   * @return An Optional containing the found IssuedProof, or empty if not found.
   */
  Optional<IssuedProof> findByPresExId(String presExId);

  /**
   * Deletes an IssuedProof entity by its presExId. This also requires the @Indexed annotation on
   * the presExId field.
   *
   * @param presExId The presentation exchange ID of the entity to delete.
   */
  //void deleteByPresExId(String presExId);
}
