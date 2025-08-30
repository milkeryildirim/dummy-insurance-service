package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.PoliciesApi;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;
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
  public ResponseEntity<PolicyDto> getPolicyByPolicyNumber(String policyNumber) {
    log.info("REST request to get policy by policy number: {}", policyNumber);
    return policyService
        .findPolicyByPolicyNumber(policyNumber)
        .map(ResponseEntity::ok)
        .orElseGet(
            () -> {
              log.warn(
                  "Policy not found for policy number: {}, returning 404 NOT FOUND", policyNumber);
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
  public ResponseEntity<List<AutoClaimDto>> getAutoClaimsByPolicyId(
      Long policyId, Integer page, Integer size, String status) {
    log.info("REST request to get auto claims for policy id: {}", policyId);

    List<ClaimDto> allClaims = claimService.findClaimsByPolicyId(policyId);
    List<AutoClaimDto> autoClaims =
        allClaims.stream()
            .filter(AutoClaimDto.class::isInstance)
            .map(AutoClaimDto.class::cast)
            .toList();

    log.debug("Found {} auto claims for policy id: {}", autoClaims.size(), policyId);
    return ResponseEntity.ok(autoClaims);
  }

  @Override
  public ResponseEntity<List<HomeClaimDto>> getHomeClaimsByPolicyId(
      Long policyId, Integer page, Integer size, String status) {
    log.info("REST request to get home claims for policy id: {}", policyId);

    List<ClaimDto> allClaims = claimService.findClaimsByPolicyId(policyId);
    List<HomeClaimDto> homeClaims =
        allClaims.stream()
            .filter(HomeClaimDto.class::isInstance)
            .map(HomeClaimDto.class::cast)
            .toList();

    log.debug("Found {} home claims for policy id: {}", homeClaims.size(), policyId);
    return ResponseEntity.ok(homeClaims);
  }

  @Override
  public ResponseEntity<List<HealthClaimDto>> getHealthClaimsByPolicyId(
      Long policyId, Integer page, Integer size, String status) {
    log.info("REST request to get health claims for policy id: {}", policyId);

    List<ClaimDto> allClaims = claimService.findClaimsByPolicyId(policyId);
    List<HealthClaimDto> healthClaims =
        allClaims.stream()
            .filter(HealthClaimDto.class::isInstance)
            .map(HealthClaimDto.class::cast)
            .toList();

    log.debug("Found {} health claims for policy id: {}", healthClaims.size(), policyId);
    return ResponseEntity.ok(healthClaims);
  }
}
