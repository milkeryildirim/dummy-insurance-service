package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.employee.EmployeeRepository;
import tech.yildirim.insurance.dummy.employee.EmployeeRole;
import tech.yildirim.insurance.dummy.policy.Policy;
import tech.yildirim.insurance.dummy.policy.PolicyRepository;
import tech.yildirim.insurance.dummy.policy.PolicyStatus;
import tech.yildirim.insurance.dummy.policy.PolicyType;

/** Implementation of the {@link ClaimService} interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

  private final ClaimRepository claimRepository;
  private final PolicyRepository policyRepository;
  private final ClaimMapper claimMapper;
  private final EmployeeRepository employeeRepository;

  @Override
  @Transactional
  public ClaimDto submitClaim(Long policyId, ClaimDto claimDto) {
    log.info("Attempting to submit a new claim for policyId: {}", policyId);
    Policy policy =
        policyRepository
            .findById(policyId)
            .orElseThrow(
                () -> {
                  log.warn("Policy not found with id: {}. Cannot submit claim.", policyId);
                  return new ResourceNotFoundException("Policy not found with id: " + policyId);
                });

    if (policy.getStatus() != PolicyStatus.ACTIVE) {
      log.error(
          "Attempted to submit claim for a non-active policy. PolicyId: {}, Status: {}",
          policyId,
          policy.getStatus());
      throw new IllegalStateException(
          "Claims can only be submitted for policies with ACTIVE status. Current status: "
              + policy.getStatus());
    }

    // Validate DTO type matches policy type
    validateDtoMatchesPolicyType(claimDto, policy.getType());

    Claim claim = createClaimShellForPolicyType(policy.getType());
    log.debug("Created a new {} shell for the claim.", policy.getType());

    // Populate claim based on its specific type
    switch (claim) {
      case AutoClaim autoClaim ->
          claimMapper.populateAutoClaimFromDto((AutoClaimDto) claimDto, autoClaim);
      case HomeClaim homeClaim ->
          claimMapper.populateHomeClaimFromDto((HomeClaimDto) claimDto, homeClaim);
      case HealthClaim healthClaim ->
          claimMapper.populateHealthClaimFromDto((HealthClaimDto) claimDto, healthClaim);
      default -> throw new UnsupportedOperationException("Claim type not supported: " + claim);
    }

    claim.setPolicy(policy);
    claim.setClaimNumber(generateClaimNumber());
    claim.setStatus(ClaimStatus.SUBMITTED);

    Claim savedClaim = claimRepository.save(claim);
    log.info(
        "Successfully submitted and saved claim with id {} and number {}",
        savedClaim.getId(),
        savedClaim.getClaimNumber());

    // Return DTO based on claim specific type
    return toDto(savedClaim);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ClaimDto> findClaimById(Long claimId) {
    log.info("Request to find claim with id: {}", claimId);
    return claimRepository.findById(claimId).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimDto> findClaimsByPolicyId(Long policyId) {
    log.info("Request to find all claims for policyId: {}", policyId);
    if (!policyRepository.existsById(policyId)) {
      log.warn("Policy with id: {} not found. Cannot retrieve claims.", policyId);
      throw new ResourceNotFoundException("Policy not found with id: " + policyId);
    }
    List<Claim> claims = claimRepository.findByPolicyId(policyId);
    log.info("Found {} claims for policyId: {}", claims.size(), policyId);
    return toDtoList(claims);
  }

  @Override
  @Transactional
  public ClaimDto assignAdjuster(Long claimId, Long employeeId) {
    log.info("Attempting to assign employee {} to claim {}", employeeId, claimId);
    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> {
                  log.warn("Cannot assign adjuster. Claim with id {} not found.", claimId);
                  return new ResourceNotFoundException("Claim not found with id: " + claimId);
                });

    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> {
                  log.warn("Cannot assign adjuster. Employee with id {} not found.", employeeId);
                  return new ResourceNotFoundException("Employee not found with id: " + employeeId);
                });

    if (employee.getRole() != EmployeeRole.CLAIMS_ADJUSTER) {
      log.error(
          "Attempted to assign an employee who is not a CLAIMS_ADJUSTER. EmployeeId: {}, Role: {}",
          employeeId,
          employee.getRole());
      throw new IllegalArgumentException(
          "Employee with id " + employeeId + " is not a CLAIMS_ADJUSTER");
    }

    claim.setAssignedAdjuster(employee);
    log.debug("Assigned adjuster {} to claim {}", employee.getId(), claim.getId());

    if (claim.getStatus() == ClaimStatus.SUBMITTED) {
      claim.setStatus(ClaimStatus.IN_REVIEW);
      log.info("Claim {} status changed from SUBMITTED to IN_REVIEW.", claim.getId());
    }

    Claim updatedClaim = claimRepository.save(claim);
    log.info("Successfully updated claim {} with assigned adjuster.", updatedClaim.getId());
    return toDto(updatedClaim);
  }

  /**
   * Helper method to instantiate the correct Claim subclass based on PolicyType. This is the core
   * of our polymorphic handling for claim creation.
   */
  private Claim createClaimShellForPolicyType(PolicyType policyType) {
    return switch (policyType) {
      case AUTO -> new AutoClaim();
      case HOME -> new HomeClaim();
      case HEALTH -> new HealthClaim();
      default -> {
        log.error("Unsupported policy type for claim submission: {}", policyType);
        throw new UnsupportedOperationException(
            "Claim submission for policy type " + policyType + " is not supported.");
      }
    };
  }

  /** Generates a simple unique claim number. */
  private String generateClaimNumber() {
    // This is simple but good enough for a dummy service.
    return "CLM-" + UUID.randomUUID().toString().toUpperCase().substring(0, 13);
  }

  private ClaimDto toDto(Claim claim) {
    return switch (claim) {
      case AutoClaim autoClaim -> claimMapper.toDto(autoClaim);
      case HomeClaim homeClaim -> claimMapper.toDto(homeClaim);
      case HealthClaim healthClaim -> claimMapper.toDto(healthClaim);
      default -> throw new UnsupportedOperationException("Claim type not supported: " + claim);
    };
  }

  private List<ClaimDto> toDtoList(List<Claim> claims) {
    return claims.stream().map(this::toDto).toList();
  }

  /**
   * Validates that the given DTO matches the expected type for the policy. Throws an
   * IllegalArgumentException if there is a mismatch.
   */
  private void validateDtoMatchesPolicyType(ClaimDto claimDto, PolicyType policyType) {
    boolean isValid =
        switch (policyType) {
          case AUTO -> claimDto instanceof AutoClaimDto;
          case HOME -> claimDto instanceof HomeClaimDto;
          case HEALTH -> claimDto instanceof HealthClaimDto;
          default -> {
            log.error("Unsupported policy type for validation: {}", policyType);
            throw new UnsupportedOperationException(
                "Validation not supported for policy type " + policyType);
          }
        };

    if (!isValid) {
      log.error(
          "DTO type mismatch. Expected DTO for policy type {}, but got {}",
          policyType,
          claimDto.getClass().getSimpleName());
      throw new IllegalArgumentException(
          "Policy type "
              + policyType
              + " does not match claim type "
              + claimDto.getClass().getSimpleName());
    }

    log.debug("DTO validation passed for policy type {}", policyType);
  }
}
