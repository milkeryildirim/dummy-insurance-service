package tech.yildirim.insurance.dummy.claim;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.ClaimDecisionDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.employee.EmployeeRepository;

/** Implementation of {@link ClaimDecisionService}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimDecisionServiceImpl implements ClaimDecisionService {

  private final ClaimDecisionRepository claimDecisionRepository;
  private final ClaimRepository claimRepository;
  private final EmployeeRepository employeeRepository;
  private final ClaimDecisionMapper claimDecisionMapper;
  private final ClaimService claimService;

  @Override
  @Transactional
  public ClaimDecisionDto createClaimDecision(Long claimId, ClaimDecisionDto claimDecisionDto) {
    log.info("Creating claim decision for claim {}", claimId);

    Claim claim = validateClaimExists(claimId);
    validateNoExistingDecision(claimId);
    Employee decisionMaker = validateEmployeeExists(claimDecisionDto.getDecisionMakerId());

    ClaimDecision claimDecision = claimDecisionMapper.toEntity(claimDecisionDto);
    claimDecision.setClaim(claim);
    claimDecision.setDecisionMaker(decisionMaker);

    ClaimDecision savedDecision = claimDecisionRepository.save(claimDecision);
    updateClaimStatusBasedOnDecision(claim, claimDecision);

    log.info("Successfully created claim decision with id {}", savedDecision.getId());
    return claimDecisionMapper.toDto(savedDecision);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ClaimDecisionDto> findClaimDecisionByClaimId(Long claimId) {
    log.info("Finding claim decision for claim {}", claimId);

    Optional<ClaimDecision> decision = claimDecisionRepository.findByClaimId(claimId);
    if (decision.isEmpty()) {
      log.warn("No claim decision found for claim {}", claimId);
      return Optional.empty();
    }

    return Optional.of(claimDecisionMapper.toDto(decision.get()));
  }

  @Override
  @Transactional
  public ClaimDecisionDto updateClaimDecision(Long claimId, ClaimDecisionDto claimDecisionDto) {
    log.info("Updating claim decision for claim {}", claimId);

    ClaimDecision existingDecision =
        claimDecisionRepository
            .findByClaimId(claimId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Claim decision not found for claim: " + claimId));

    updateDecisionMakerIfChanged(existingDecision, claimDecisionDto.getDecisionMakerId());
    claimDecisionMapper.updateClaimDecisionFromDto(claimDecisionDto, existingDecision);

    ClaimDecision updatedDecision = claimDecisionRepository.save(existingDecision);
    updateClaimStatusBasedOnDecision(existingDecision.getClaim(), existingDecision);

    log.info("Successfully updated claim decision for claim {}", claimId);
    return claimDecisionMapper.toDto(updatedDecision);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsClaimDecisionByClaimId(Long claimId) {
    log.debug("Checking if claim decision exists for claim {}", claimId);
    return claimDecisionRepository.existsByClaimId(claimId);
  }

  @Override
  @Transactional
  public ClaimDecisionDto approveClaim(
      Long claimId, Long decisionMakerId, BigDecimal approvedAmount, String reasoning) {
    log.info(
        "Approving claim {} with amount {} by decision maker {}",
        claimId,
        approvedAmount,
        decisionMakerId);

    ClaimDecisionDto decisionDto =
        buildDecisionDto(claimId, decisionMakerId, reasoning)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED)
            .approvedAmount(approvedAmount);

    return createOrUpdateDecision(claimId, decisionDto);
  }

  @Override
  @Transactional
  public ClaimDecisionDto rejectClaim(
      Long claimId, Long decisionMakerId, String rejectionReason, String reasoning) {
    log.info(
        "Rejecting claim {} by decision maker {} with reason: {}",
        claimId,
        decisionMakerId,
        rejectionReason);

    ClaimDecisionDto decisionDto =
        buildDecisionDto(claimId, decisionMakerId, reasoning)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.REJECTED)
            .rejectionReason(rejectionReason);

    return createOrUpdateDecision(claimId, decisionDto);
  }

  @Override
  @Transactional
  public ClaimDecisionDto partiallyApproveClaim(
      Long claimId, Long decisionMakerId, BigDecimal approvedAmount, String reasoning) {
    log.info(
        "Partially approving claim {} with amount {} by decision maker {}",
        claimId,
        approvedAmount,
        decisionMakerId);

    ClaimDecisionDto decisionDto =
        buildDecisionDto(claimId, decisionMakerId, reasoning)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.PARTIALLY_APPROVED)
            .approvedAmount(approvedAmount);

    return createOrUpdateDecision(claimId, decisionDto);
  }

  private ClaimDecisionDto buildDecisionDto(Long claimId, Long decisionMakerId, String reasoning) {
    return new ClaimDecisionDto()
        .claimId(claimId)
        .decisionMakerId(decisionMakerId)
        .reasoning(reasoning);
  }

  private ClaimDecisionDto createOrUpdateDecision(Long claimId, ClaimDecisionDto decisionDto) {
    if (claimDecisionRepository.existsByClaimId(claimId)) {
      return updateExistingDecision(claimId, decisionDto);
    } else {
      return createNewDecision(claimId, decisionDto);
    }
  }

  private ClaimDecisionDto updateExistingDecision(Long claimId, ClaimDecisionDto decisionDto) {
    ClaimDecision existingDecision =
        claimDecisionRepository
            .findByClaimId(claimId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Claim decision not found for claim: " + claimId));

    updateDecisionMakerIfChanged(existingDecision, decisionDto.getDecisionMakerId());
    claimDecisionMapper.updateClaimDecisionFromDto(decisionDto, existingDecision);

    ClaimDecision updatedDecision = claimDecisionRepository.save(existingDecision);
    updateClaimStatusBasedOnDecision(existingDecision.getClaim(), existingDecision);

    return claimDecisionMapper.toDto(updatedDecision);
  }

  private ClaimDecisionDto createNewDecision(Long claimId, ClaimDecisionDto decisionDto) {
    Claim claim = validateClaimExists(claimId);
    Employee decisionMaker = validateEmployeeExists(decisionDto.getDecisionMakerId());

    ClaimDecision claimDecision = claimDecisionMapper.toEntity(decisionDto);
    claimDecision.setClaim(claim);
    claimDecision.setDecisionMaker(decisionMaker);

    ClaimDecision savedDecision = claimDecisionRepository.save(claimDecision);
    updateClaimStatusBasedOnDecision(claim, claimDecision);

    return claimDecisionMapper.toDto(savedDecision);
  }

  private Claim validateClaimExists(Long claimId) {
    return claimRepository
        .findById(claimId)
        .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
  }

  private Employee validateEmployeeExists(Long employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
  }

  private void validateNoExistingDecision(Long claimId) {
    if (claimDecisionRepository.existsByClaimId(claimId)) {
      throw new IllegalStateException("A decision already exists for claim: " + claimId);
    }
  }

  private void updateDecisionMakerIfChanged(
      ClaimDecision existingDecision, Long newDecisionMakerId) {
    if (!existingDecision.getDecisionMaker().getId().equals(newDecisionMakerId)) {
      Employee newDecisionMaker = validateEmployeeExists(newDecisionMakerId);
      existingDecision.setDecisionMaker(newDecisionMaker);
    }
  }

  private void updateClaimStatusBasedOnDecision(Claim claim, ClaimDecision decision) {
    switch (decision.getDecisionType()) {
      case APPROVED, PARTIALLY_APPROVED:
        if (decision.getApprovedAmount() != null) {
          claimService.moveClaimToApproved(claim.getId(), decision.getApprovedAmount());
        } else {
          claimService.updateClaimStatus(claim.getId(), ClaimStatus.APPROVED);
        }
        break;

      case REJECTED:
        claimService.moveClaimToRejected(claim.getId());
        break;

      case REQUIRES_MORE_INFO, UNDER_INVESTIGATION:
        claimService.updateClaimStatus(claim.getId(), ClaimStatus.IN_REVIEW);
        break;

      default:
        log.warn("Unknown decision type: {}", decision.getDecisionType());
        break;
    }
  }
}
