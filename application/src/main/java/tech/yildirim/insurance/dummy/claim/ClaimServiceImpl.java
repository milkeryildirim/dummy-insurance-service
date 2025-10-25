package tech.yildirim.insurance.dummy.claim;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
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
  private final AdjusterReportRepository adjusterReportRepository;
  private final CustomerInvoiceRepository customerInvoiceRepository;
  private final ClaimDecisionRepository claimDecisionRepository;

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
  public Optional<ClaimDto> findClaimByIdWithDetails(Long claimId) {
    log.info("Request to find claim with id: {} including all details", claimId);
    return claimRepository
        .findById(claimId)
        .map(
            claim -> {
              ClaimDto claimDto = toDto(claim);

              // Load related data
              List<AdjusterReport> reports = adjusterReportRepository.findByClaimId(claimId);
              List<CustomerInvoice> invoices = customerInvoiceRepository.findByClaimId(claimId);
              Optional<ClaimDecision> decision = claimDecisionRepository.findByClaimId(claimId);

              log.debug(
                  "Found {} adjuster reports, {} customer invoices, and {} decision for claim {}",
                  reports.size(),
                  invoices.size(),
                  decision.isPresent() ? 1 : 0,
                  claimId);

              return claimDto;
            });
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

  @Override
  @Transactional
  public ClaimDto updateClaim(Long claimId, ClaimDto claimDto) {
    log.info("Attempting to update claim with id: {}", claimId);

    Claim existingClaim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> {
                  log.warn("Cannot update claim. Claim with id {} not found.", claimId);
                  return new ResourceNotFoundException("Claim not found with id: " + claimId);
                });

    // Validate that the DTO type matches the existing claim type
    validateDtoMatchesClaimType(claimDto, existingClaim);

    // Update the claim based on its specific type
    switch (existingClaim) {
      case AutoClaim autoClaim ->
          claimMapper.populateAutoClaimFromDto((AutoClaimDto) claimDto, autoClaim);
      case HomeClaim homeClaim ->
          claimMapper.populateHomeClaimFromDto((HomeClaimDto) claimDto, homeClaim);
      case HealthClaim healthClaim ->
          claimMapper.populateHealthClaimFromDto((HealthClaimDto) claimDto, healthClaim);
      default ->
          throw new UnsupportedOperationException("Claim type not supported: " + existingClaim);
    }

    Claim updatedClaim = claimRepository.save(existingClaim);
    log.info("Successfully updated claim with id: {}", updatedClaim.getId());

    return toDto(updatedClaim);
  }

  @Override
  @Transactional
  public void deleteClaim(Long claimId) {
    log.info("Attempting to delete claim with id: {}", claimId);

    Claim existingClaim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> {
                  log.warn("Cannot delete claim. Claim with id {} not found.", claimId);
                  return new ResourceNotFoundException("Claim not found with id: " + claimId);
                });

    claimRepository.delete(existingClaim);
    log.info("Successfully deleted claim with id: {}", claimId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimDto> getAllClaimsByType(ClaimDto.ClaimTypeEnum claimType) {
    log.info("Request to find all claims of type: {}", claimType);

    String cType =
        switch (claimType) {
          case ClaimTypeEnum.AUTO_CLAIM_DTO -> AutoClaim.CLAIM_TYPE;
          case ClaimTypeEnum.HOME_CLAIM_DTO -> HomeClaim.CLAIM_TYPE;
          case ClaimTypeEnum.HEALTH_CLAIM_DTO -> HealthClaim.CLAIM_TYPE;
        };

    List<Claim> claims = claimRepository.findClaimByClaimType(cType);
    log.info("Found {} claims of type: {}", claims.size(), claimType);

    return toDtoList(claims);
  }

  @Override
  @Transactional
  public ClaimDto updateClaimStatus(Long claimId, ClaimStatus newStatus) {
    log.info("Updating claim {} status to {}", claimId, newStatus);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    ClaimStatus oldStatus = claim.getStatus();
    claim.setStatus(newStatus);

    Claim updatedClaim = claimRepository.save(claim);
    log.info("Successfully updated claim {} status from {} to {}", claimId, oldStatus, newStatus);

    return toDto(updatedClaim);
  }

  @Override
  @Transactional
  public ClaimDto moveClaimToInReview(Long claimId) {
    log.info("Moving claim {} to IN_REVIEW status", claimId);
    return updateClaimStatus(claimId, ClaimStatus.IN_REVIEW);
  }

  @Override
  @Transactional
  public ClaimDto moveClaimToApproved(Long claimId, BigDecimal approvedAmount) {
    log.info("Moving claim {} to APPROVED status with amount {}", claimId, approvedAmount);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    claim.setStatus(ClaimStatus.APPROVED);
    claim.setPaidAmount(approvedAmount);

    Claim updatedClaim = claimRepository.save(claim);
    log.info("Successfully approved claim {} with amount {}", claimId, approvedAmount);

    return toDto(updatedClaim);
  }

  @Override
  @Transactional
  public ClaimDto moveClaimToRejected(Long claimId) {
    log.info("Moving claim {} to REJECTED status", claimId);
    return updateClaimStatus(claimId, ClaimStatus.REJECTED);
  }

  @Override
  @Transactional
  public ClaimDto moveClaimToPaid(Long claimId, BigDecimal paidAmount) {
    log.info("Moving claim {} to PAID status with amount {}", claimId, paidAmount);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    claim.setStatus(ClaimStatus.PAID);
    claim.setPaidAmount(paidAmount);

    Claim updatedClaim = claimRepository.save(claim);
    log.info("Successfully marked claim {} as paid with amount {}", claimId, paidAmount);

    return toDto(updatedClaim);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canAddAdjusterReports(Long claimId) {
    log.debug("Checking if claim {} can have adjuster reports added", claimId);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    // Adjuster reports can be added if claim is SUBMITTED or IN_REVIEW
    boolean canAdd =
        claim.getStatus() == ClaimStatus.SUBMITTED || claim.getStatus() == ClaimStatus.IN_REVIEW;
    log.debug("Claim {} can add adjuster reports: {}", claimId, canAdd);

    return canAdd;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canAddCustomerInvoices(Long claimId) {
    log.debug("Checking if claim {} can have customer invoices added", claimId);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    // Customer invoices can be added if claim is not PAID or REJECTED
    boolean canAdd =
        claim.getStatus() != ClaimStatus.PAID && claim.getStatus() != ClaimStatus.REJECTED;
    log.debug("Claim {} can add customer invoices: {}", claimId, canAdd);

    return canAdd;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canMakeDecision(Long claimId) {
    log.debug("Checking if claim {} can have a decision made", claimId);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    // Decision can be made if claim is IN_REVIEW and no decision exists yet
    boolean canMake =
        claim.getStatus() == ClaimStatus.IN_REVIEW
            && !claimDecisionRepository.existsByClaimId(claimId);
    log.debug("Claim {} can make decision: {}", claimId, canMake);

    return canMake;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isClaimReadyForDecision(Long claimId) {
    log.debug("Checking if claim {} is ready for decision", claimId);

    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    // Check if claim has at least one submitted adjuster report
    List<AdjusterReport> submittedReports =
        adjusterReportRepository.findByClaimIdAndStatus(claimId, ReportStatus.SUBMITTED);

    // Check if claim has at least one customer invoice
    List<CustomerInvoice> invoices = customerInvoiceRepository.findByClaimId(claimId);

    boolean isReady =
        claim.getStatus() == ClaimStatus.IN_REVIEW
            && !submittedReports.isEmpty()
            && !invoices.isEmpty();

    log.debug(
        "Claim {} is ready for decision: {} (reports: {}, invoices: {})",
        claimId,
        isReady,
        submittedReports.size(),
        invoices.size());

    return isReady;
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

  /**
   * Validates that the given DTO matches the existing claim's type. Throws an
   * IllegalArgumentException if there is a mismatch.
   */
  private void validateDtoMatchesClaimType(ClaimDto claimDto, Claim claim) {
    boolean isValid =
        switch (claim) {
          case AutoClaim autoClaim -> claimDto instanceof AutoClaimDto;
          case HomeClaim homeClaim -> claimDto instanceof HomeClaimDto;
          case HealthClaim healthClaim -> claimDto instanceof HealthClaimDto;
          default -> {
            log.error("Unsupported claim type for validation: {}", claim);
            throw new UnsupportedOperationException(
                "Validation not supported for claim type " + claim);
          }
        };

    if (!isValid) {
      log.error(
          "DTO type mismatch. Expected DTO for claim type {}, but got {}",
          claim.getClass().getSimpleName(),
          claimDto.getClass().getSimpleName());
      throw new IllegalArgumentException(
          "Claim type "
              + claim.getClass().getSimpleName()
              + " does not match provided DTO type "
              + claimDto.getClass().getSimpleName());
    }

    log.debug("DTO validation passed for claim type {}", claim.getClass().getSimpleName());
  }
}
