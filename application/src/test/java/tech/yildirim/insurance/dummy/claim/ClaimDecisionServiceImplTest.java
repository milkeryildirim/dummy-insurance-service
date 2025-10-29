package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yildirim.insurance.api.generated.model.ClaimDecisionDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.employee.EmployeeRepository;
import tech.yildirim.insurance.dummy.employee.EmployeeRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("Claim Decision Service Unit Tests")
class ClaimDecisionServiceImplTest {

  @Mock private ClaimDecisionRepository claimDecisionRepository;
  @Mock private ClaimRepository claimRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private ClaimDecisionMapper claimDecisionMapper;
  @Mock private ClaimService claimService;

  @InjectMocks private ClaimDecisionServiceImpl claimDecisionService;

  private Long claimId;
  Long decisionId;
  private Long decisionMakerId;
  private Claim testClaim;
  private Employee testDecisionMaker;
  private ClaimDecision testDecision;
  private ClaimDecisionDto testDecisionDto;

  @BeforeEach
  void setUp() {
    claimId = 1L;
    decisionId = 2L;
    decisionMakerId = 3L;

    // Setup test claim
    testClaim = new AutoClaim();
    testClaim.setId(claimId);
    testClaim.setClaimNumber("CLM-TEST-001");

    // Setup test decision maker
    testDecisionMaker = new Employee();
    testDecisionMaker.setId(decisionMakerId);
    testDecisionMaker.setFirstName("John");
    testDecisionMaker.setLastName("Manager");
    testDecisionMaker.setRole(EmployeeRole.MANAGER);

    // Setup test claim decision
    testDecision = new ClaimDecision();
    testDecision.setId(decisionId);
    testDecision.setClaim(testClaim);
    testDecision.setDecisionMaker(testDecisionMaker);
    testDecision.setDecisionType(DecisionType.APPROVED);
    testDecision.setApprovedAmount(BigDecimal.valueOf(1000.00));
    testDecision.setReasoning("Medical expenses approved");
    testDecision.setDecisionDate(ZonedDateTime.now());

    // Setup test DTO
    testDecisionDto =
        new ClaimDecisionDto()
            .claimId(claimId)
            .decisionMakerId(decisionMakerId)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED)
            .approvedAmount(BigDecimal.valueOf(1000.00))
            .reasoning("Medical expenses approved");
  }

  // ========== Create Decision Tests ==========

  @Test
  @DisplayName("Should create claim decision successfully")
  void createClaimDecision_withValidData_shouldCreateSuccessfully() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);
    when(employeeRepository.findById(decisionMakerId)).thenReturn(Optional.of(testDecisionMaker));
    when(claimDecisionMapper.toEntity(testDecisionDto)).thenReturn(testDecision);
    when(claimDecisionRepository.save(any(ClaimDecision.class))).thenReturn(testDecision);
    when(claimDecisionMapper.toDto(testDecision)).thenReturn(testDecisionDto);

    // When
    ClaimDecisionDto result = claimDecisionService.createClaimDecision(claimId, testDecisionDto);

    // Then
    assertThat(result).isEqualTo(testDecisionDto);
    verify(claimRepository).findById(claimId);
    verify(claimDecisionRepository).existsByClaimId(claimId);
    verify(employeeRepository).findById(decisionMakerId);
    verify(claimDecisionRepository).save(any(ClaimDecision.class));
    verify(claimService).moveClaimToApproved(claimId, BigDecimal.valueOf(1000.00));

    ArgumentCaptor<ClaimDecision> decisionCaptor = ArgumentCaptor.forClass(ClaimDecision.class);
    verify(claimDecisionRepository).save(decisionCaptor.capture());

    ClaimDecision savedDecision = decisionCaptor.getValue();
    assertThat(savedDecision.getClaim()).isEqualTo(testClaim);
    assertThat(savedDecision.getDecisionMaker()).isEqualTo(testDecisionMaker);
  }

  @Test
  @DisplayName("Should throw exception when claim not found")
  void createClaimDecision_withNonExistentClaim_shouldThrowException() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> claimDecisionService.createClaimDecision(claimId, testDecisionDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Claim not found with id: " + claimId);
  }

  @Test
  @DisplayName("Should throw exception when decision already exists")
  void createClaimDecision_withExistingDecision_shouldThrowException() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> claimDecisionService.createClaimDecision(claimId, testDecisionDto))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("A decision already exists for claim: " + claimId);
  }

  @Test
  @DisplayName("Should throw exception when decision maker not found")
  void createClaimDecision_withNonExistentDecisionMaker_shouldThrowException() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);
    when(employeeRepository.findById(decisionMakerId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> claimDecisionService.createClaimDecision(claimId, testDecisionDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Employee not found with id: " + decisionMakerId);
  }

  // ========== Find Decision Tests ==========

  @Test
  @DisplayName("Should find claim decision successfully")
  void findClaimDecisionByClaimId_withExistingDecision_shouldReturnDecision() {
    // Given
    when(claimDecisionRepository.findByClaimId(claimId)).thenReturn(Optional.of(testDecision));
    when(claimDecisionMapper.toDto(testDecision)).thenReturn(testDecisionDto);

    // When
    Optional<ClaimDecisionDto> result = claimDecisionService.findClaimDecisionByClaimId(claimId);

    // Then
    assertThat(result).isPresent();
    assertThat(result).contains(testDecisionDto);
  }

  @Test
  @DisplayName("Should return empty when decision not found")
  void findClaimDecisionByClaimId_withNonExistentDecision_shouldReturnEmpty() {
    // Given
    when(claimDecisionRepository.findByClaimId(claimId)).thenReturn(Optional.empty());

    // When
    Optional<ClaimDecisionDto> result = claimDecisionService.findClaimDecisionByClaimId(claimId);

    // Then
    assertThat(result).isEmpty();
  }

  // ========== Update Decision Tests ==========

  @Test
  @DisplayName("Should update claim decision successfully")
  void updateClaimDecision_withExistingDecision_shouldUpdateSuccessfully() {
    // Given
    ClaimDecisionDto updateDto =
        new ClaimDecisionDto().decisionMakerId(decisionMakerId).reasoning("Updated reasoning");

    when(claimDecisionRepository.findByClaimId(claimId)).thenReturn(Optional.of(testDecision));
    when(claimDecisionRepository.save(testDecision)).thenReturn(testDecision);
    when(claimDecisionMapper.toDto(testDecision)).thenReturn(testDecisionDto);

    // When
    ClaimDecisionDto result = claimDecisionService.updateClaimDecision(claimId, updateDto);

    // Then
    assertThat(result).isEqualTo(testDecisionDto);
    verify(claimDecisionMapper).updateClaimDecisionFromDto(updateDto, testDecision);
    verify(claimDecisionRepository).save(testDecision);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent decision")
  void updateClaimDecision_withNonExistentDecision_shouldThrowException() {
    // Given
    when(claimDecisionRepository.findByClaimId(claimId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> claimDecisionService.updateClaimDecision(claimId, testDecisionDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Claim decision not found for claim: " + claimId);
  }

  // ========== Business Logic Tests ==========

  @Test
  @DisplayName("Should approve claim successfully")
  void approveClaim_withNewDecision_shouldCreateApprovedDecision() {
    // Given
    BigDecimal approvedAmount = BigDecimal.valueOf(1500.00);
    String reasoning = "Claim approved after review";

    ClaimDecision approvedDecision = new ClaimDecision();
    approvedDecision.setId(decisionId);
    approvedDecision.setClaim(testClaim);
    approvedDecision.setDecisionMaker(testDecisionMaker);
    approvedDecision.setDecisionType(DecisionType.APPROVED);
    approvedDecision.setApprovedAmount(approvedAmount);
    approvedDecision.setReasoning(reasoning);
    approvedDecision.setDecisionDate(ZonedDateTime.now());

    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(decisionMakerId)).thenReturn(Optional.of(testDecisionMaker));
    when(claimDecisionMapper.toEntity(any(ClaimDecisionDto.class))).thenReturn(approvedDecision);
    when(claimDecisionRepository.save(any(ClaimDecision.class))).thenReturn(approvedDecision);
    when(claimDecisionMapper.toDto(approvedDecision)).thenReturn(testDecisionDto);

    // When
    ClaimDecisionDto result =
        claimDecisionService.approveClaim(claimId, decisionMakerId, approvedAmount, reasoning);

    // Then
    assertThat(result).isEqualTo(testDecisionDto);
    verify(claimService).moveClaimToApproved(claimId, approvedAmount);

    ArgumentCaptor<ClaimDecisionDto> dtoCaptor = ArgumentCaptor.forClass(ClaimDecisionDto.class);
    verify(claimDecisionMapper).toEntity(dtoCaptor.capture());

    ClaimDecisionDto capturedDto = dtoCaptor.getValue();
    assertThat(capturedDto.getDecisionType()).isEqualTo(ClaimDecisionDto.DecisionTypeEnum.APPROVED);
    assertThat(capturedDto.getApprovedAmount()).isEqualTo(approvedAmount);
    assertThat(capturedDto.getReasoning()).isEqualTo(reasoning);
  }

  @Test
  @DisplayName("Should reject claim successfully")
  void rejectClaim_withNewDecision_shouldCreateRejectedDecision() {
    // Given
    String rejectionReason = "Insufficient documentation";
    String reasoning = "Missing medical reports";

    ClaimDecision rejectedDecision = new ClaimDecision();
    rejectedDecision.setId(decisionId);
    rejectedDecision.setClaim(testClaim);
    rejectedDecision.setDecisionMaker(testDecisionMaker);
    rejectedDecision.setDecisionType(DecisionType.REJECTED);
    rejectedDecision.setRejectionReason(rejectionReason);
    rejectedDecision.setReasoning(reasoning);
    rejectedDecision.setDecisionDate(ZonedDateTime.now());

    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(decisionMakerId)).thenReturn(Optional.of(testDecisionMaker));
    when(claimDecisionMapper.toEntity(any(ClaimDecisionDto.class))).thenReturn(rejectedDecision);
    when(claimDecisionRepository.save(any(ClaimDecision.class))).thenReturn(rejectedDecision);
    when(claimDecisionMapper.toDto(rejectedDecision)).thenReturn(testDecisionDto);

    // When
    ClaimDecisionDto result =
        claimDecisionService.rejectClaim(claimId, decisionMakerId, rejectionReason, reasoning);

    // Then
    assertThat(result).isEqualTo(testDecisionDto);
    verify(claimService).moveClaimToRejected(claimId);

    ArgumentCaptor<ClaimDecisionDto> dtoCaptor = ArgumentCaptor.forClass(ClaimDecisionDto.class);
    verify(claimDecisionMapper).toEntity(dtoCaptor.capture());

    ClaimDecisionDto capturedDto = dtoCaptor.getValue();
    assertThat(capturedDto.getDecisionType()).isEqualTo(ClaimDecisionDto.DecisionTypeEnum.REJECTED);
    assertThat(capturedDto.getRejectionReason()).isEqualTo(rejectionReason);
    assertThat(capturedDto.getReasoning()).isEqualTo(reasoning);
  }

  @Test
  @DisplayName("Should partially approve claim successfully")
  void partiallyApproveClaim_withNewDecision_shouldCreatePartiallyApprovedDecision() {
    // Given
    BigDecimal approvedAmount = BigDecimal.valueOf(750.00);
    String reasoning = "Partial approval due to policy limits";

    ClaimDecision partiallyApprovedDecision = new ClaimDecision();
    partiallyApprovedDecision.setId(decisionId);
    partiallyApprovedDecision.setClaim(testClaim);
    partiallyApprovedDecision.setDecisionMaker(testDecisionMaker);
    partiallyApprovedDecision.setDecisionType(DecisionType.PARTIALLY_APPROVED);
    partiallyApprovedDecision.setApprovedAmount(approvedAmount);
    partiallyApprovedDecision.setReasoning(reasoning);
    partiallyApprovedDecision.setDecisionDate(ZonedDateTime.now());

    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(decisionMakerId)).thenReturn(Optional.of(testDecisionMaker));
    when(claimDecisionMapper.toEntity(any(ClaimDecisionDto.class)))
        .thenReturn(partiallyApprovedDecision);
    when(claimDecisionRepository.save(any(ClaimDecision.class)))
        .thenReturn(partiallyApprovedDecision);
    when(claimDecisionMapper.toDto(partiallyApprovedDecision)).thenReturn(testDecisionDto);

    // When
    ClaimDecisionDto result =
        claimDecisionService.partiallyApproveClaim(
            claimId, decisionMakerId, approvedAmount, reasoning);

    // Then
    assertThat(result).isEqualTo(testDecisionDto);
    verify(claimService).moveClaimToApproved(claimId, approvedAmount);

    ArgumentCaptor<ClaimDecisionDto> dtoCaptor = ArgumentCaptor.forClass(ClaimDecisionDto.class);
    verify(claimDecisionMapper).toEntity(dtoCaptor.capture());

    ClaimDecisionDto capturedDto = dtoCaptor.getValue();
    assertThat(capturedDto.getDecisionType())
        .isEqualTo(ClaimDecisionDto.DecisionTypeEnum.PARTIALLY_APPROVED);
    assertThat(capturedDto.getApprovedAmount()).isEqualTo(approvedAmount);
    assertThat(capturedDto.getReasoning()).isEqualTo(reasoning);
  }

  // ========== Exists Check Tests ==========

  @Test
  @DisplayName("Should return true when decision exists")
  void existsClaimDecisionByClaimId_withExistingDecision_shouldReturnTrue() {
    // Given
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(true);

    // When
    boolean result = claimDecisionService.existsClaimDecisionByClaimId(claimId);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should return false when decision does not exist")
  void existsClaimDecisionByClaimId_withNonExistentDecision_shouldReturnFalse() {
    // Given
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);

    // When
    boolean result = claimDecisionService.existsClaimDecisionByClaimId(claimId);

    // Then
    assertThat(result).isFalse();
  }
}
