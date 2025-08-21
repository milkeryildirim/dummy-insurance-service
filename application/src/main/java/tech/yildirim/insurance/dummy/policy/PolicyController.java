package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PolicyController implements PoliciesApi {

  private final PolicyService policyService;
  private final ClaimService claimService;

  @Override
  public ResponseEntity<PolicyDto> createPolicy(PolicyDto policyDto) {
    log.info("REST request to create policy for customerId {}", policyDto.getCustomerId());
    PolicyDto createdPolicy = policyService.createPolicy(policyDto);
    log.info(
        "Successfully created policy with id {} and number {}",
        createdPolicy.getId(),
        createdPolicy.getPolicyNumber());
    return new ResponseEntity<>(createdPolicy, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<PolicyDto>> getAllPolicies() {
    log.info("REST request to get all policies");
    List<PolicyDto> policies = policyService.findAllPolicies();
    log.debug("Returning {} policies", policies.size());
    return ResponseEntity.ok(policies);
  }

  @Override
  public ResponseEntity<PolicyDto> getPolicyById(Long id) {
    log.info("REST request to get policy with id: {}", id);
    return policyService
        .findPolicyById(id)
        .map(
            policy -> {
              log.info("Found policy with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok(policy);
            })
        .orElseGet(
            () -> {
              log.warn("Policy with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<PolicyDto> updatePolicy(Long id, PolicyDto policyDto) {
    log.info("REST request to update policy with id: {}", id);
    return policyService
        .updatePolicy(id, policyDto)
        .map(
            policy -> {
              log.info("Successfully updated policy with id: {}", id);
              return ResponseEntity.ok(policy);
            })
        .orElseGet(
            () -> {
              log.warn("Failed to update. Policy with id: {} not found.", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<List<ClaimDto>> getClaimsByPolicyId(Long id) {
    log.info("REST request to get claims for policy with id: {}", id);
    List<ClaimDto> claims = claimService.findClaimsByPolicyId(id);
    log.debug("Found {} claims for policy with id {}", claims.size(), id);
    return ResponseEntity.ok(claims);
  }

  @Override
  public ResponseEntity<ClaimDto> submitClaim(Long id, ClaimDto claimDto) {
    log.info("REST request to submit a claim for policy with id: {}", id);
    ClaimDto submittedClaim = claimService.submitClaim(id, claimDto);
    log.info(
        "Successfully submitted claim with id {} for policy with id {}",
        submittedClaim.getId(),
        id);
    return new ResponseEntity<>(submittedClaim, HttpStatus.CREATED);
  }
}
