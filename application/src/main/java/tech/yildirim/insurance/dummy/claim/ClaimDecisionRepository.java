package tech.yildirim.insurance.dummy.claim;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for {@link ClaimDecision} entity. */
@Repository
public interface ClaimDecisionRepository extends JpaRepository<ClaimDecision, Long> {

  /**
   * Find the decision for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return Optional containing the claim decision if found.
   */
  Optional<ClaimDecision> findByClaimId(Long claimId);

  /**
   * Check if a decision exists for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return true if a decision exists, false otherwise.
   */
  boolean existsByClaimId(Long claimId);
}
