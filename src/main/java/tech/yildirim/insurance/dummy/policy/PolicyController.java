package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.PoliciesApi;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.claim.ClaimService;

/**
 * REST Controller for managing policies. Implements the generated {@link PoliciesApi} interface.
 */
@RestController
@RequiredArgsConstructor
public class PolicyController implements PoliciesApi {

  private final PolicyService policyService;
  private final ClaimService claimService;

  @Override
  public ResponseEntity<PolicyDto> createPolicy(PolicyDto policyDto) {
    return new ResponseEntity<>(policyService.createPolicy(policyDto), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<PolicyDto>> getAllPolicies() {
    return ResponseEntity.ok(policyService.findAllPolicies());
  }

  @Override
  public ResponseEntity<PolicyDto> getPolicyById(Long id) {
    return policyService
        .findPolicyById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<PolicyDto> updatePolicy(Long id, PolicyDto policyDto) {
    return policyService
        .updatePolicy(id, policyDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<List<ClaimDto>> getClaimsByPolicyId(Long id) {
    return ResponseEntity.ok(claimService.findClaimsByPolicyId(id));
  }

  @Override
  public ResponseEntity<ClaimDto> submitClaim(Long id, ClaimDto claimDto) {
    return new ResponseEntity<>(claimService.submitClaim(id, claimDto), HttpStatus.CREATED);
  }
}
