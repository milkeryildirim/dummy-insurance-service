package tech.yildirim.insurance.dummy.policy.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.PolicyConditionsApi;
import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

/**
 * REST Controller for managing system-wide policy conditions. Implements the generated {@link
 * PolicyConditionsApi} interface.
 */
@RestController
@RequiredArgsConstructor
public class PolicyConditionsController implements PolicyConditionsApi {

  private final PolicyConditionsService policyConditionsService;

  @Override
  public ResponseEntity<PolicyConditionsDto> getPolicyConditions() {
    PolicyConditionsDto conditions = policyConditionsService.getPolicyConditions();
    return ResponseEntity.ok(conditions);
  }

  @Override
  public ResponseEntity<PolicyConditionsDto> updatePolicyConditions(
      PolicyConditionsDto policyConditionsDto) {
    PolicyConditionsDto updatedConditions =
        policyConditionsService.updatePolicyConditions(policyConditionsDto);
    return ResponseEntity.ok(updatedConditions);
  }
}
