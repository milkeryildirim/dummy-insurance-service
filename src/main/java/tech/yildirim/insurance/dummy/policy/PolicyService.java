package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import tech.yildirim.insurance.api.generated.model.PolicyDto;

/** Service Interface for managing {@link Policy}. */
public interface PolicyService {

  /**
   * Creates a new policy for a customer.
   *
   * @param policyDto The DTO containing policy information.
   * @return The created policy DTO.
   */
  PolicyDto createPolicy(PolicyDto policyDto);

  /**
   * Finds a policy by its ID.
   *
   * @param id The ID of the policy.
   * @return An Optional containing the found policy, or empty if not found.
   */
  Optional<PolicyDto> findPolicyById(Long id);

  /**
   * Retrieves all policies in the system.
   *
   * @return A list of all policies.
   */
  List<PolicyDto> findAllPolicies();

  /**
   * Updates an existing policy.
   *
   * @param id The ID of the policy to update.
   * @param policyDto The DTO with updated information.
   * @return The updated policy DTO, or empty if the policy was not found.
   */
  Optional<PolicyDto> updatePolicy(Long id, PolicyDto policyDto);
}
