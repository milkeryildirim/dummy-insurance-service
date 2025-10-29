package tech.yildirim.insurance.dummy.claim;

import java.util.Optional;
import tech.yildirim.insurance.api.generated.model.ClaimDecisionDto;

/** Service Interface for managing {@link ClaimDecision}. */
public interface ClaimDecisionService {
  /**
   * Creates a new claim decision.
   *
   * @param claimId The ID of the claim.
   * @param claimDecisionDto The DTO containing the decision data.
   * @return The created claim decision DTO.
   */
  ClaimDecisionDto createClaimDecision(Long claimId, ClaimDecisionDto claimDecisionDto);

  /**
   * Retrieves the decision for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return An Optional containing the found claim decision DTO, or empty if not found.
   */
  Optional<ClaimDecisionDto> findClaimDecisionByClaimId(Long claimId);

  /**
   * Updates an existing claim decision.
   *
   * @param claimId The ID of the claim.
   * @param claimDecisionDto The DTO containing the updated decision data.
   * @return The updated claim decision DTO.
   */
  ClaimDecisionDto updateClaimDecision(Long claimId, ClaimDecisionDto claimDecisionDto);

  /**
   * Checks if a decision exists for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return true if a decision exists, false otherwise.
   */
  boolean existsClaimDecisionByClaimId(Long claimId);

  /**
   * Approves a claim with the specified amount.
   *
   * @param claimId The ID of the claim.
   * @param decisionMakerId The ID of the employee making the decision.
   * @param approvedAmount The amount approved for payout.
   * @param reasoning The reasoning behind the approval.
   * @return The created/updated claim decision DTO.
   */
  ClaimDecisionDto approveClaim(
      Long claimId, Long decisionMakerId, java.math.BigDecimal approvedAmount, String reasoning);

  /**
   * Rejects a claim with the specified reason.
   *
   * @param claimId The ID of the claim.
   * @param decisionMakerId The ID of the employee making the decision.
   * @param rejectionReason The reason for rejection.
   * @param reasoning The detailed reasoning behind the rejection.
   * @return The created/updated claim decision DTO.
   */
  ClaimDecisionDto rejectClaim(
      Long claimId, Long decisionMakerId, String rejectionReason, String reasoning);

  /**
   * Partially approves a claim with the specified amount.
   *
   * @param claimId The ID of the claim.
   * @param decisionMakerId The ID of the employee making the decision.
   * @param approvedAmount The amount approved for payout.
   * @param reasoning The reasoning behind the partial approval.
   * @return The created/updated claim decision DTO.
   */
  ClaimDecisionDto partiallyApproveClaim(
      Long claimId, Long decisionMakerId, java.math.BigDecimal approvedAmount, String reasoning);
}
