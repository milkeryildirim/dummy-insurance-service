package tech.yildirim.insurance.dummy.claim;

import static tech.yildirim.insurance.dummy.policy.PolicyType.AUTO;
import static tech.yildirim.insurance.dummy.policy.PolicyType.HEALTH;
import static tech.yildirim.insurance.dummy.policy.PolicyType.HOME;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
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
public class ClaimServiceImpl implements ClaimService {

  private final ClaimRepository claimRepository;
  private final PolicyRepository policyRepository;
  private final ClaimMapper claimMapper;
  private final EmployeeRepository employeeRepository;

  @Override
  @Transactional
  public ClaimDto submitClaim(Long policyId, ClaimDto claimDto) {
    Policy policy =
        policyRepository
            .findById(policyId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Policy not found with id: " + policyId));

    if (policy.getStatus() != PolicyStatus.ACTIVE) {
      throw new IllegalStateException(
          "Claims can only be submitted for policies with ACTIVE status. Current status: "
              + policy.getStatus());
    }

    Claim claim = createClaimShellForPolicyType(policy.getType());

    claimMapper.populateClaimFromDto(claimDto, claim);

    claim.setPolicy(policy);
    claim.setClaimNumber(generateClaimNumber());
    claim.setStatus(ClaimStatus.SUBMITTED);

    return claimMapper.toDto(claimRepository.save(claim));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ClaimDto> findClaimById(Long claimId) {
    return claimRepository.findById(claimId).map(claimMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimDto> findClaimsByPolicyId(Long policyId) {
    if (!policyRepository.existsById(policyId)) {
      throw new ResourceNotFoundException("Policy not found with id: " + policyId);
    }
    return claimMapper.toDtoList(claimRepository.findByPolicyId(policyId));
  }

  @Override
  @Transactional
  public ClaimDto assignAdjuster(Long claimId, Long employeeId) {
    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

    if (employee.getRole() != EmployeeRole.CLAIMS_ADJUSTER) {
      throw new IllegalArgumentException(
          "Employee with id " + employeeId + " is not a CLAIMS_ADJUSTER");
    }

    claim.setAssignedAdjuster(employee);
    if (claim.getStatus() == ClaimStatus.SUBMITTED) {
      claim.setStatus(ClaimStatus.IN_REVIEW);
    }

    return claimMapper.toDto(claimRepository.save(claim));
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
      default ->
          throw new UnsupportedOperationException(
              "Claim submission for policy type " + policyType + " is not supported.");
    };
  }

  /** Generates a simple unique claim number. */
  private String generateClaimNumber() {
    return "CLM-" + UUID.randomUUID().toString().toUpperCase().substring(0, 13);
  }
}
