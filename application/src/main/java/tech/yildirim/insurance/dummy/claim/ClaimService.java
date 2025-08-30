package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import java.util.Optional;
import tech.yildirim.insurance.api.generated.model.ClaimDto;

/** Service Interface for managing {@link Claim}. */
public interface ClaimService {

  /**
   * Submits a new claim for a specific policy.
   *
   * @param policyId The ID of the policy to which the claim is being submitted.
   * @param claimDto The DTO containing the claim details.
   * @return The created claim DTO.
   */
  ClaimDto submitClaim(Long policyId, ClaimDto claimDto);

  /**
   * Finds a claim by its unique ID.
   *
   * @param claimId The ID of the claim.
   * @return An Optional containing the found claim, or empty if not found.
   */
  Optional<ClaimDto> findClaimById(Long claimId);

  /**
   * Finds all claims associated with a given policy.
   *
   * @param policyId The ID of the policy.
   * @return A list of claim DTOs for that policy.
   */
  List<ClaimDto> findClaimsByPolicyId(Long policyId);

  /**
   * Assigns an employee to a specific claim.
   *
   * @param claimId The ID of the claim.
   * @param employeeId The ID of the employee to be assigned.
   * @return The updated claim DTO with the assignee information.
   */
  ClaimDto assignAdjuster(Long claimId, Long employeeId);

  /**
   * Updates an existing claim.
   *
   * @param claimId The ID of the claim to update.
   * @param claimDto The DTO containing the updated claim details.
   * @return The updated claim DTO.
   */
  ClaimDto updateClaim(Long claimId, ClaimDto claimDto);
}
