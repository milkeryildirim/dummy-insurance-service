package tech.yildirim.insurance.dummy.policy.condition;

import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

/** Service Interface for managing the system-wide {@link PolicyConditions}. */
public interface PolicyConditionsService {

  /**
   * Retrieves the current set of policy conditions.
   *
   * @return The active policy conditions.
   */
  PolicyConditionsDto getPolicyConditions();

  /**
   * Updates the system-wide set of policy conditions.
   *
   * @param policyConditionsDto The DTO with the new set of conditions.
   * @return The updated policy conditions.
   */
  PolicyConditionsDto updatePolicyConditions(PolicyConditionsDto policyConditionsDto);
}
