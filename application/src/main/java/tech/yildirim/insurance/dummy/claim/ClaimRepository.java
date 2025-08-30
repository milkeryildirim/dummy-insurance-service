package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Claim} entity hierarchy. By extending JpaRepository for
 * the base class, we can query the entire hierarchy.
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

  /**
   * Finds all claims associated with a given policy ID.
   *
   * @param policyId The ID of the policy.
   * @return A list of claims belonging to the policy.
   */
  List<Claim> findByPolicyId(Long policyId);

  @Query(value = "SELECT * FROM claims WHERE claim_type = ?1", nativeQuery = true)
  List<Claim> findClaimByClaimType(String claimType);
}
