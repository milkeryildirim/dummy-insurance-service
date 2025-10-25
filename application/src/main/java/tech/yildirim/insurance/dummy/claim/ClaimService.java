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
   * Finds a claim by its unique ID with all related data (reports, invoices, decision).
   *
   * @param claimId The ID of the claim.
   * @return An Optional containing the found claim with full details, or empty if not found.
   */
  Optional<ClaimDto> findClaimByIdWithDetails(Long claimId);

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

  /**
   * Deletes a claim by its ID.
   *
   * @param claimId The ID of the claim to delete.
   * @throws tech.yildirim.insurance.dummy.common.ResourceNotFoundException if the claim is not
   *     found.
   */
  void deleteClaim(Long claimId);

  /**
   * Retrieves all claims of a specific type.
   *
   * @param claimType The type of claims to retrieve (AUTO, HOME, HEALTH).
   * @return A list of claim DTOs of the specified type.
   */
  List<ClaimDto> getAllClaimsByType(ClaimDto.ClaimTypeEnum claimType);

  /**
   * Updates the status of a claim.
   *
   * @param claimId The ID of the claim.
   * @param newStatus The new status to set.
   * @return The updated claim DTO.
   */
  ClaimDto updateClaimStatus(Long claimId, ClaimStatus newStatus);

  /**
   * Moves a claim to "IN_REVIEW" status when first adjuster report is submitted.
   *
   * @param claimId The ID of the claim.
   * @return The updated claim DTO.
   */
  ClaimDto moveClaimToInReview(Long claimId);

  /**
   * Moves a claim to "APPROVED" status when decision is made.
   *
   * @param claimId The ID of the claim.
   * @param approvedAmount The amount approved for payout.
   * @return The updated claim DTO.
   */
  ClaimDto moveClaimToApproved(Long claimId, java.math.BigDecimal approvedAmount);

  /**
   * Moves a claim to "REJECTED" status when decision is made.
   *
   * @param claimId The ID of the claim.
   * @return The updated claim DTO.
   */
  ClaimDto moveClaimToRejected(Long claimId);

  /**
   * Moves a claim to "PAID" status when payment is processed.
   *
   * @param claimId The ID of the claim.
   * @param paidAmount The amount actually paid out.
   * @return The updated claim DTO.
   */
  ClaimDto moveClaimToPaid(Long claimId, java.math.BigDecimal paidAmount);

  /**
   * Validates if a claim can have adjuster reports added.
   *
   * @param claimId The ID of the claim.
   * @return true if adjuster reports can be added, false otherwise.
   */
  boolean canAddAdjusterReports(Long claimId);

  /**
   * Validates if a claim can have customer invoices added.
   *
   * @param claimId The ID of the claim.
   * @return true if customer invoices can be added, false otherwise.
   */
  boolean canAddCustomerInvoices(Long claimId);

  /**
   * Validates if a claim can have a decision made.
   *
   * @param claimId The ID of the claim.
   * @return true if a decision can be made, false otherwise.
   */
  boolean canMakeDecision(Long claimId);

  /**
   * Checks if a claim is ready for decision (has required reports and invoices).
   *
   * @param claimId The ID of the claim.
   * @return true if the claim is ready for decision, false otherwise.
   */
  boolean isClaimReadyForDecision(Long claimId);
}
