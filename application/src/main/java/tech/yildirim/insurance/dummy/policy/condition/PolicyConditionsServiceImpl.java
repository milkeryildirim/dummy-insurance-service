package tech.yildirim.insurance.dummy.policy.condition;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

/**
 * Implementation of the {@link PolicyConditionsService} interface. Manages the single, system-wide
 * set of policy conditions.
 */
@Service
@RequiredArgsConstructor
public class PolicyConditionsServiceImpl implements PolicyConditionsService {

  private static final Long CONDITIONS_ID = 1L;

  private final PolicyConditionsRepository policyConditionsRepository;
  private final PolicyConditionsMapper policyConditionsMapper;

  @Override
  @Transactional(readOnly = true)
  public PolicyConditionsDto getPolicyConditions() {
    PolicyConditions conditions = findActiveConditions();
    return policyConditionsMapper.toDto(conditions);
  }

  @Override
  @Transactional
  public PolicyConditionsDto updatePolicyConditions(PolicyConditionsDto policyConditionsDto) {
    PolicyConditions existingConditions = findActiveConditions();

    policyConditionsMapper.updateEntityFromDto(policyConditionsDto, existingConditions);

    existingConditions
        .getCancellationRules()
        .sort(
            Comparator.comparing(CancellationPenaltyRule::getMonthsRemainingThreshold).reversed());

    PolicyConditions updatedConditions = policyConditionsRepository.save(existingConditions);
    return policyConditionsMapper.toDto(updatedConditions);
  }

  /**
   * Helper method to find the single active set of policy conditions. Throws an exception if it's
   * not found, which would indicate a critical data setup issue.
   *
   * @return The active PolicyConditions entity.
   */
  private PolicyConditions findActiveConditions() {
    return policyConditionsRepository
        .findById(CONDITIONS_ID)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Critical error: PolicyConditions with ID "
                        + CONDITIONS_ID
                        + " not found in the database."));
  }
}
