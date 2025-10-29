package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Claim Service Unit Tests")
class ClaimServiceImplTest {

  @Mock private ClaimRepository claimRepository;
  @Mock private PolicyRepository policyRepository;
  @Mock private ClaimMapper claimMapper;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private AdjusterReportRepository adjusterReportRepository;
  @Mock private CustomerInvoiceRepository customerInvoiceRepository;
  @Mock private ClaimDecisionRepository claimDecisionRepository;

  @InjectMocks private ClaimServiceImpl claimService;

  // ==================== AUTO CLAIM TESTS ====================

  @Test
  @DisplayName("Should create an AutoClaim when submitting AutoClaimDto for active AUTO policy")
  void submitClaim_withAutoClaimDto_shouldCreateAutoClaim() {
    // Given: An active AUTO policy and an AutoClaimDto
    long policyId = 1L;
    Policy autoPolicy = createPolicy(policyId, PolicyType.AUTO, PolicyStatus.ACTIVE);

    AutoClaimDto autoClaimDto =
        new AutoClaimDto()
            .policyId(policyId)
            .description("Car accident on highway")
            .dateOfIncident(LocalDate.now().minusDays(1))
            .estimatedAmount(BigDecimal.valueOf(5000.00))
            .licensePlate("ABC123")
            .vehicleVin("1HGBH41JXMN109186")
            .accidentLocation("Highway 101, Mile 45");

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(autoPolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(autoClaimDto);
    doAnswer(
            invocation -> {
              Mappers.getMapper(ClaimMapper.class)
                  .populateAutoClaimFromDto(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(claimMapper)
        .populateAutoClaimFromDto(any(AutoClaimDto.class), any(AutoClaim.class));

    // When: The submitClaim method is called
    ClaimDto result = claimService.submitClaim(policyId, autoClaimDto);

    // Then: Verify that an AutoClaim object was created with correct properties
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    Claim capturedClaim = claimCaptor.getValue();
    assertThat(capturedClaim).isInstanceOf(AutoClaim.class);

    AutoClaim autoClaim = (AutoClaim) capturedClaim;
    assertThat(autoClaim.getPolicy()).isEqualTo(autoPolicy);
    assertThat(autoClaim.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    assertThat(autoClaim.getClaimNumber()).isNotNull();
    assertThat(autoClaim.getDescription()).isEqualTo("Car accident on highway");
    assertThat(autoClaim.getLicensePlate()).isEqualTo("ABC123");
    assertThat(autoClaim.getVehicleVin()).isEqualTo("1HGBH41JXMN109186");
    assertThat(autoClaim.getAccidentLocation()).isEqualTo("Highway 101, Mile 45");
    assertThat(result).isEqualTo(autoClaimDto);
  }

  @Test
  @DisplayName("Should create AutoClaim with minimal required fields")
  void submitClaim_withMinimalAutoClaimDto_shouldCreateAutoClaim() {
    // Given: Minimal AutoClaimDto with only required fields
    long policyId = 1L;
    Policy autoPolicy = createPolicy(policyId, PolicyType.AUTO, PolicyStatus.ACTIVE);

    AutoClaimDto autoClaimDto =
        new AutoClaimDto()
            .policyId(policyId)
            .description("Minor fender bender")
            .dateOfIncident(LocalDate.now())
            .licensePlate("XYZ789");

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(autoPolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(autoClaimDto);
    doAnswer(
            invocation -> {
              Mappers.getMapper(ClaimMapper.class)
                  .populateAutoClaimFromDto(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(claimMapper)
        .populateAutoClaimFromDto(any(AutoClaimDto.class), any(AutoClaim.class));

    // When
    claimService.submitClaim(policyId, autoClaimDto);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    AutoClaim autoClaim = (AutoClaim) claimCaptor.getValue();
    assertThat(autoClaim.getDescription()).isEqualTo("Minor fender bender");
    assertThat(autoClaim.getLicensePlate()).isEqualTo("XYZ789");
    assertThat(autoClaim.getVehicleVin()).isNull();
    assertThat(autoClaim.getAccidentLocation()).isNull();
  }

  // ==================== HOME CLAIM TESTS ====================

  @Test
  @DisplayName("Should create a HomeClaim when submitting HomeClaimDto for active HOME policy")
  void submitClaim_withHomeClaimDto_shouldCreateHomeClaim() {
    // Given: An active HOME policy and a HomeClaimDto
    long policyId = 2L;
    Policy homePolicy = createPolicy(policyId, PolicyType.HOME, PolicyStatus.ACTIVE);

    HomeClaimDto homeClaimDto =
        new HomeClaimDto()
            .policyId(policyId)
            .description("Water damage from burst pipe")
            .dateOfIncident(LocalDate.now().minusDays(2))
            .estimatedAmount(BigDecimal.valueOf(15000.00))
            .typeOfDamage("Water damage")
            .damagedItems("Kitchen cabinets, hardwood floors, appliances");

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(homePolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(HomeClaim.class))).thenReturn(homeClaimDto);
    doAnswer(
            invocation -> {
              Mappers.getMapper(ClaimMapper.class)
                  .populateHomeClaimFromDto(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(claimMapper)
        .populateHomeClaimFromDto(any(HomeClaimDto.class), any(HomeClaim.class));

    // When
    ClaimDto result = claimService.submitClaim(policyId, homeClaimDto);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    Claim capturedClaim = claimCaptor.getValue();
    assertThat(capturedClaim).isInstanceOf(HomeClaim.class);

    HomeClaim homeClaim = (HomeClaim) capturedClaim;
    assertThat(homeClaim.getPolicy()).isEqualTo(homePolicy);
    assertThat(homeClaim.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    assertThat(homeClaim.getTypeOfDamage()).isEqualTo("Water damage");
    assertThat(homeClaim.getDamagedItems())
        .isEqualTo("Kitchen cabinets, hardwood floors, appliances");
    assertThat(result).isEqualTo(homeClaimDto);
  }

  @Test
  @DisplayName("Should create HomeClaim with minimal required fields")
  void submitClaim_withMinimalHomeClaimDto_shouldCreateHomeClaim() {
    // Given
    long policyId = 2L;
    Policy homePolicy = createPolicy(policyId, PolicyType.HOME, PolicyStatus.ACTIVE);

    HomeClaimDto homeClaimDto =
        new HomeClaimDto()
            .policyId(policyId)
            .description("Storm damage")
            .dateOfIncident(LocalDate.now());

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(homePolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(HomeClaim.class))).thenReturn(homeClaimDto);

    // When
    claimService.submitClaim(policyId, homeClaimDto);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    HomeClaim homeClaim = (HomeClaim) claimCaptor.getValue();
    assertThat(homeClaim.getTypeOfDamage()).isNull();
    assertThat(homeClaim.getDamagedItems()).isNull();
  }

  // ==================== HEALTH CLAIM TESTS ====================

  @Test
  @DisplayName(
      "Should create a HealthClaim when submitting HealthClaimDto for active HEALTH policy")
  void submitClaim_withHealthClaimDto_shouldCreateHealthClaim() {
    // Given: An active HEALTH policy and a HealthClaimDto
    long policyId = 3L;
    Policy healthPolicy = createPolicy(policyId, PolicyType.HEALTH, PolicyStatus.ACTIVE);

    HealthClaimDto healthClaimDto =
        new HealthClaimDto()
            .policyId(policyId)
            .description("Emergency room visit")
            .dateOfIncident(LocalDate.now().minusDays(1))
            .estimatedAmount(BigDecimal.valueOf(2500.00))
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99285");

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(healthPolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(HealthClaim.class))).thenReturn(healthClaimDto);
    doAnswer(
            invocation -> {
              Mappers.getMapper(ClaimMapper.class)
                  .populateHealthClaimFromDto(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(claimMapper)
        .populateHealthClaimFromDto(any(HealthClaimDto.class), any(HealthClaim.class));

    // When
    ClaimDto result = claimService.submitClaim(policyId, healthClaimDto);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    Claim capturedClaim = claimCaptor.getValue();
    assertThat(capturedClaim).isInstanceOf(HealthClaim.class);

    HealthClaim healthClaim = (HealthClaim) capturedClaim;
    assertThat(healthClaim.getPolicy()).isEqualTo(healthPolicy);
    assertThat(healthClaim.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    assertThat(healthClaim.getMedicalProvider()).isEqualTo("City General Hospital");
    assertThat(healthClaim.getProcedureCode()).isEqualTo("CPT-99285");
    assertThat(result).isEqualTo(healthClaimDto);
  }

  @Test
  @DisplayName("Should create HealthClaim with minimal required fields")
  void submitClaim_withMinimalHealthClaimDto_shouldCreateHealthClaim() {
    // Given
    long policyId = 3L;
    Policy healthPolicy = createPolicy(policyId, PolicyType.HEALTH, PolicyStatus.ACTIVE);

    HealthClaimDto healthClaimDto =
        new HealthClaimDto()
            .policyId(policyId)
            .description("Routine checkup")
            .dateOfIncident(LocalDate.now());

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(healthPolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(HealthClaim.class))).thenReturn(healthClaimDto);

    // When
    claimService.submitClaim(policyId, healthClaimDto);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    HealthClaim healthClaim = (HealthClaim) claimCaptor.getValue();
    assertThat(healthClaim.getMedicalProvider()).isNull();
    assertThat(healthClaim.getProcedureCode()).isNull();
  }

  // ==================== VALIDATION TESTS ====================

  @Test
  @DisplayName("Should throw IllegalArgumentException when AUTO DTO submitted for HOME policy")
  void submitClaim_withMismatchedDtoAndPolicy_shouldThrowException() {
    // Given: HOME policy but AUTO claim DTO
    long policyId = 1L;
    Policy homePolicy = createPolicy(policyId, PolicyType.HOME, PolicyStatus.ACTIVE);

    AutoClaimDto autoClaimDto =
        new AutoClaimDto()
            .policyId(policyId)
            .description("This should fail")
            .dateOfIncident(LocalDate.now())
            .licensePlate("ABC123");

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(homePolicy));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> claimService.submitClaim(policyId, autoClaimDto));

    assertThat(exception.getMessage()).contains("Policy type HOME does not match claim type");
    verify(claimRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when HOME DTO submitted for HEALTH policy")
  void submitClaim_withHomeClaimDtoForHealthPolicy_shouldThrowException() {
    // Given: HEALTH policy but HOME claim DTO
    long policyId = 1L;
    Policy healthPolicy = createPolicy(policyId, PolicyType.HEALTH, PolicyStatus.ACTIVE);

    HomeClaimDto homeClaimDto =
        new HomeClaimDto()
            .policyId(policyId)
            .description("This should fail")
            .dateOfIncident(LocalDate.now());

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(healthPolicy));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> claimService.submitClaim(policyId, homeClaimDto));

    assertThat(exception.getMessage()).contains("Policy type HEALTH does not match claim type");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when HEALTH DTO submitted for AUTO policy")
  void submitClaim_withHealthClaimDtoForAutoPolicy_shouldThrowException() {
    // Given: AUTO policy but HEALTH claim DTO
    long policyId = 1L;
    Policy autoPolicy = createPolicy(policyId, PolicyType.AUTO, PolicyStatus.ACTIVE);

    HealthClaimDto healthClaimDto =
        new HealthClaimDto()
            .policyId(policyId)
            .description("This should fail")
            .dateOfIncident(LocalDate.now());

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(autoPolicy));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> claimService.submitClaim(policyId, healthClaimDto));

    assertThat(exception.getMessage()).contains("Policy type AUTO does not match claim type");
  }

  // ==================== EXISTING TESTS (Policy Status & Error Cases) ====================

  @Test
  @DisplayName("Should throw IllegalStateException when submitting a claim for an INACTIVE policy")
  void submitClaim_forInactivePolicy_shouldThrowException() {
    // Given: A PENDING (not ACTIVE) policy
    long policyId = 3L;
    Policy pendingPolicy = createPolicy(policyId, PolicyType.AUTO, PolicyStatus.PENDING);

    AutoClaimDto inputDto =
        new AutoClaimDto()
            .policyId(policyId)
            .description("Test claim")
            .dateOfIncident(LocalDate.now())
            .licensePlate("ABC123");

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(pendingPolicy));

    // When & Then: Assert that the correct exception is thrown
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> claimService.submitClaim(policyId, inputDto));

    assertThat(exception.getMessage())
        .contains("Claims can only be submitted for policies with ACTIVE status");
    verify(claimRepository, never()).save(any());
  }

  @Test
  @DisplayName(
      "Should throw ResourceNotFoundException when submitting a claim for a non-existent policy")
  void submitClaim_forNonExistentPolicy_shouldThrowException() {
    // Given: A non-existent policy ID
    long nonExistentPolicyId = 99L;
    when(policyRepository.findById(nonExistentPolicyId)).thenReturn(Optional.empty());

    AutoClaimDto claimDto =
        new AutoClaimDto()
            .policyId(nonExistentPolicyId)
            .description("Test claim")
            .dateOfIncident(LocalDate.now())
            .licensePlate("ABC123");

    // When & Then: Assert that the correct exception is thrown
    assertThrows(
        ResourceNotFoundException.class,
        () -> claimService.submitClaim(nonExistentPolicyId, claimDto));
  }

  // ==================== ADJUSTER ASSIGNMENT TESTS ====================

  @Test
  @DisplayName("Should assign adjuster successfully when claim and adjuster are valid")
  void assignAdjuster_whenSuccessful_shouldUpdateClaim() {
    // Given: A claim and an employee with the correct role
    long claimId = 1L;
    long employeeId = 10L;

    AutoClaim existingClaim = new AutoClaim();
    existingClaim.setId(claimId);
    existingClaim.setStatus(ClaimStatus.SUBMITTED);

    Employee adjuster = new Employee();
    adjuster.setId(employeeId);
    adjuster.setRole(EmployeeRole.CLAIMS_ADJUSTER);
    adjuster.setFirstName("John");
    adjuster.setLastName("Doe");
    adjuster.setPhoneNumber("555-1234");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));
    when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(adjuster));
    when(claimRepository.save(any(Claim.class))).thenReturn(existingClaim);
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(new AutoClaimDto());

    // When: The assignAdjuster method is called
    claimService.assignAdjuster(claimId, employeeId);

    // Then: Verify the claim's adjuster is set and status is updated
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());
    Claim savedClaim = claimCaptor.getValue();

    assertThat(savedClaim.getAssignedAdjuster()).isEqualTo(adjuster);
    assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.IN_REVIEW);
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when assigning to a non-existent claim")
  void assignAdjuster_whenClaimNotFound_shouldThrowException() {
    // Given
    when(claimRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> claimService.assignAdjuster(99L, 10L));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when assigning a non-existent employee")
  void assignAdjuster_whenEmployeeNotFound_shouldThrowException() {
    // Given
    when(claimRepository.findById(anyLong())).thenReturn(Optional.of(new AutoClaim()));
    when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> claimService.assignAdjuster(1L, 99L));
  }

  @Test
  @DisplayName(
      "Should throw IllegalArgumentException when assigned employee is not a CLAIMS_ADJUSTER")
  void assignAdjuster_whenEmployeeHasWrongRole_shouldThrowException() {
    // Given: An employee with the MANAGER role
    AutoClaim existingClaim = new AutoClaim();
    Employee manager = new Employee();
    manager.setRole(EmployeeRole.MANAGER);

    when(claimRepository.findById(1L)).thenReturn(Optional.of(existingClaim));
    when(employeeRepository.findById(10L)).thenReturn(Optional.of(manager));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> claimService.assignAdjuster(1L, 10L));

    assertThat(exception.getMessage()).contains("is not a CLAIMS_ADJUSTER");
  }

  @Test
  @DisplayName("Should update claim status successfully")
  void updateClaimStatus_shouldUpdateSuccessfully() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.SUBMITTED);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(claimRepository.save(any(Claim.class))).thenReturn(claim);
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(new AutoClaimDto());

    // When
    claimService.updateClaimStatus(claimId, ClaimStatus.IN_REVIEW);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());
    Claim savedClaim = claimCaptor.getValue();
    assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.IN_REVIEW);
  }

  @Test
  @DisplayName("Should move claim to approved with amount")
  void moveClaimToApproved_shouldSetStatusAndAmount() {
    // Given
    long claimId = 1L;
    BigDecimal approvedAmount = BigDecimal.valueOf(1000.00);
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.IN_REVIEW);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(claimRepository.save(any(Claim.class))).thenReturn(claim);
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(new AutoClaimDto());

    // When
    claimService.moveClaimToApproved(claimId, approvedAmount);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());
    Claim savedClaim = claimCaptor.getValue();
    assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
    assertThat(savedClaim.getPaidAmount()).isEqualTo(approvedAmount);
  }

  @Test
  @DisplayName("Should move claim to paid with amount")
  void moveClaimToPaid_shouldSetStatusAndAmount() {
    // Given
    long claimId = 1L;
    BigDecimal paidAmount = BigDecimal.valueOf(950.00);
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.APPROVED);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(claimRepository.save(any(Claim.class))).thenReturn(claim);
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(new AutoClaimDto());

    // When
    claimService.moveClaimToPaid(claimId, paidAmount);

    // Then
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());
    Claim savedClaim = claimCaptor.getValue();
    assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.PAID);
    assertThat(savedClaim.getPaidAmount()).isEqualTo(paidAmount);
  }

  @Test
  @DisplayName("Should allow adding adjuster reports when claim is SUBMITTED")
  void canAddAdjusterReports_whenClaimSubmitted_shouldReturnTrue() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.SUBMITTED);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

    // When
    boolean canAdd = claimService.canAddAdjusterReports(claimId);

    // Then
    assertThat(canAdd).isTrue();
  }

  @Test
  @DisplayName("Should allow adding adjuster reports when claim is IN_REVIEW")
  void canAddAdjusterReports_whenClaimInReview_shouldReturnTrue() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.IN_REVIEW);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

    // When
    boolean canAdd = claimService.canAddAdjusterReports(claimId);

    // Then
    assertThat(canAdd).isTrue();
  }

  @Test
  @DisplayName("Should not allow adding adjuster reports when claim is APPROVED")
  void canAddAdjusterReports_whenClaimApproved_shouldReturnFalse() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.APPROVED);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

    // When
    boolean canAdd = claimService.canAddAdjusterReports(claimId);

    // Then
    assertThat(canAdd).isFalse();
  }

  @Test
  @DisplayName("Should not allow adding customer invoices when claim is PAID")
  void canAddCustomerInvoices_whenClaimPaid_shouldReturnFalse() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.PAID);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

    // When
    boolean canAdd = claimService.canAddCustomerInvoices(claimId);

    // Then
    assertThat(canAdd).isFalse();
  }

  @Test
  @DisplayName("Should not allow adding customer invoices when claim is REJECTED")
  void canAddCustomerInvoices_whenClaimRejected_shouldReturnFalse() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.REJECTED);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

    // When
    boolean canAdd = claimService.canAddCustomerInvoices(claimId);

    // Then
    assertThat(canAdd).isFalse();
  }

  @Test
  @DisplayName("Should allow making decision when claim is IN_REVIEW and no decision exists")
  void canMakeDecision_whenClaimInReviewAndNoDecision_shouldReturnTrue() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.IN_REVIEW);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(false);

    // When
    boolean canMake = claimService.canMakeDecision(claimId);

    // Then
    assertThat(canMake).isTrue();
  }

  @Test
  @DisplayName("Should not allow making decision when decision already exists")
  void canMakeDecision_whenDecisionExists_shouldReturnFalse() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.IN_REVIEW);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(claimDecisionRepository.existsByClaimId(claimId)).thenReturn(true);

    // When
    boolean canMake = claimService.canMakeDecision(claimId);

    // Then
    assertThat(canMake).isFalse();
  }

  @Test
  @DisplayName("Should be ready for decision when has submitted reports and invoices")
  void isClaimReadyForDecision_whenHasReportsAndInvoices_shouldReturnTrue() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.IN_REVIEW);

    AdjusterReport submittedReport = new AdjusterReport();
    submittedReport.setStatus(ReportStatus.SUBMITTED);

    CustomerInvoice invoice = new CustomerInvoice();

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(adjusterReportRepository.findByClaimIdAndStatus(claimId, ReportStatus.SUBMITTED))
        .thenReturn(List.of(submittedReport));
    when(customerInvoiceRepository.findByClaimId(claimId)).thenReturn(List.of(invoice));

    // When
    boolean isReady = claimService.isClaimReadyForDecision(claimId);

    // Then
    assertThat(isReady).isTrue();
  }

  @Test
  @DisplayName("Should not be ready for decision when no submitted reports")
  void isClaimReadyForDecision_whenNoSubmittedReports_shouldReturnFalse() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);
    claim.setStatus(ClaimStatus.IN_REVIEW);

    CustomerInvoice invoice = new CustomerInvoice();

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(adjusterReportRepository.findByClaimIdAndStatus(claimId, ReportStatus.SUBMITTED))
        .thenReturn(List.of()); // No submitted reports
    when(customerInvoiceRepository.findByClaimId(claimId)).thenReturn(List.of(invoice));

    // When
    boolean isReady = claimService.isClaimReadyForDecision(claimId);

    // Then
    assertThat(isReady).isFalse();
  }

  @Test
  @DisplayName("Should find auto claim by ID when claim is auto type")
  void findAutoClaimById_whenClaimIsAutoType_shouldReturnClaim() {
    // Given
    long claimId = 1L;
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(claimId);
    autoClaim.setLicensePlate("AUTO123");

    AutoClaimDto autoClaimDto = new AutoClaimDto().id(claimId).licensePlate("AUTO123");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(autoClaim));
    when(claimMapper.toDto(autoClaim)).thenReturn(autoClaimDto);

    // When
    Optional<ClaimDto> result = claimService.findClaimById(claimId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isInstanceOf(AutoClaimDto.class);
    assertThat(((AutoClaimDto) result.get()).getLicensePlate()).isEqualTo("AUTO123");
  }

  @Test
  @DisplayName("Should return empty when claim is not auto type")
  void findAutoClaimById_whenClaimIsNotAutoType_shouldReturnEmpty() {
    // Given
    long claimId = 1L;
    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(claimId);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(homeClaim));

    // When
    Optional<ClaimDto> result = claimService.findClaimById(claimId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should find home claim by ID when claim is home type")
  void findHomeClaimById_whenClaimIsHomeType_shouldReturnClaim() {
    // Given
    long claimId = 2L;
    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(claimId);
    homeClaim.setTypeOfDamage("Water damage");

    HomeClaimDto homeClaimDto = new HomeClaimDto().id(claimId).typeOfDamage("Water damage");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(homeClaim));
    when(claimMapper.toDto(homeClaim)).thenReturn(homeClaimDto);

    // When
    Optional<ClaimDto> result = claimService.findClaimById(claimId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isInstanceOf(HomeClaimDto.class);
    assertThat(((HomeClaimDto) result.get()).getTypeOfDamage()).isEqualTo("Water damage");
  }

  @Test
  @DisplayName("Should find health claim by ID when claim is health type")
  void findHealthClaimById_whenClaimIsHealthType_shouldReturnClaim() {
    // Given
    long claimId = 3L;
    HealthClaim healthClaim = new HealthClaim();
    healthClaim.setId(claimId);
    healthClaim.setMedicalProvider("Hospital ABC");

    HealthClaimDto healthClaimDto =
        new HealthClaimDto().id(claimId).medicalProvider("Hospital ABC");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(healthClaim));
    when(claimMapper.toDto(healthClaim)).thenReturn(healthClaimDto);

    // When
    Optional<ClaimDto> result = claimService.findClaimById(claimId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isInstanceOf(HealthClaimDto.class);
    assertThat(((HealthClaimDto) result.get()).getMedicalProvider()).isEqualTo("Hospital ABC");
  }

  @Test
  @DisplayName("Should load claim with all details including reports, invoices, and decision")
  void findClaimByIdWithDetails_shouldLoadAllRelatedData() {
    // Given
    long claimId = 1L;
    AutoClaim claim = new AutoClaim();
    claim.setId(claimId);

    AdjusterReport report = new AdjusterReport();
    report.setId(1L);

    CustomerInvoice invoice = new CustomerInvoice();
    invoice.setId(1L);

    ClaimDecision decision = new ClaimDecision();
    decision.setId(1L);

    AutoClaimDto claimDto = new AutoClaimDto().id(claimId);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
    when(adjusterReportRepository.findByClaimId(claimId)).thenReturn(List.of(report));
    when(customerInvoiceRepository.findByClaimId(claimId)).thenReturn(List.of(invoice));
    when(claimDecisionRepository.findByClaimId(claimId)).thenReturn(Optional.of(decision));
    when(claimMapper.toDto(claim)).thenReturn(claimDto);

    // When
    Optional<ClaimDto> result = claimService.findClaimByIdWithDetails(claimId);

    // Then
    assertThat(result).isPresent();
    verify(adjusterReportRepository).findByClaimId(claimId);
    verify(customerInvoiceRepository).findByClaimId(claimId);
    verify(claimDecisionRepository).findByClaimId(claimId);
  }

  // UPDATE CLAIM TESTS

  @Test
  @DisplayName("Should update AutoClaim successfully when valid data is provided")
  void updateClaim_withValidAutoClaimDto_shouldUpdateAutoClaim() {
    // Given: An existing AutoClaim and updated AutoClaimDto
    long claimId = 1L;

    AutoClaim existingClaim = new AutoClaim();
    existingClaim.setId(claimId);
    existingClaim.setDescription("Original description");
    existingClaim.setLicensePlate("OLD123");
    existingClaim.setVehicleVin("OLD_VIN");

    AutoClaimDto updateDto =
        new AutoClaimDto()
            .id(claimId)
            .description("Updated description")
            .dateOfIncident(LocalDate.now())
            .licensePlate("NEW456")
            .vehicleVin("NEW_VIN")
            .accidentLocation("Updated location");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));
    when(claimRepository.save(any(Claim.class))).thenReturn(existingClaim);
    when(claimMapper.toDto(any(AutoClaim.class))).thenReturn(updateDto);
    doAnswer(
            invocation -> {
              Mappers.getMapper(ClaimMapper.class)
                  .populateAutoClaimFromDto(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(claimMapper)
        .populateAutoClaimFromDto(any(AutoClaimDto.class), any(AutoClaim.class));

    // When: The updateClaim method is called
    ClaimDto result = claimService.updateClaim(claimId, updateDto);

    // Then: Verify the claim was updated correctly
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    Claim savedClaim = claimCaptor.getValue();
    assertThat(savedClaim).isInstanceOf(AutoClaim.class);
    assertThat(savedClaim.getId()).isEqualTo(claimId);

    AutoClaim updatedAutoClaim = (AutoClaim) savedClaim;
    assertThat(updatedAutoClaim.getDescription()).isEqualTo("Updated description");
    assertThat(updatedAutoClaim.getLicensePlate()).isEqualTo("NEW456");
    assertThat(updatedAutoClaim.getVehicleVin()).isEqualTo("NEW_VIN");
    assertThat(updatedAutoClaim.getAccidentLocation()).isEqualTo("Updated location");
    assertThat(result).isEqualTo(updateDto);
  }

  // DELETE CLAIM TESTS

  @Test
  @DisplayName("Should delete claim successfully when claim exists")
  void deleteClaim_withExistingClaim_shouldDeleteSuccessfully() {
    // Given: An existing claim
    long claimId = 1L;
    AutoClaim existingClaim = new AutoClaim();
    existingClaim.setId(claimId);
    existingClaim.setDescription("Claim to be deleted");
    existingClaim.setLicensePlate("DEL123");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));

    // When: The deleteClaim method is called
    claimService.deleteClaim(claimId);

    // Then: Verify the claim was deleted
    verify(claimRepository).findById(claimId);
    verify(claimRepository).delete(existingClaim);
  }

  // GET ALL CLAIMS BY TYPE TESTS

  @Test
  @DisplayName("Should return all auto claims when requesting AUTO type")
  void getAllClaimsByType_withAutoType_shouldReturnAutoClaims() {
    // Given: Multiple auto claims in the repository
    AutoClaim autoClaim1 = new AutoClaim();
    autoClaim1.setId(1L);
    autoClaim1.setLicensePlate("AUTO001");
    autoClaim1.setVehicleVin("VIN001");

    AutoClaim autoClaim2 = new AutoClaim();
    autoClaim2.setId(2L);
    autoClaim2.setLicensePlate("AUTO002");
    autoClaim2.setVehicleVin("VIN002");

    List<Claim> autoClaims = List.of(autoClaim1, autoClaim2);

    AutoClaimDto autoClaimDto1 =
        new AutoClaimDto().id(1L).licensePlate("AUTO001").vehicleVin("VIN001");

    AutoClaimDto autoClaimDto2 =
        new AutoClaimDto().id(2L).licensePlate("AUTO002").vehicleVin("VIN002");

    when(claimRepository.findClaimByClaimType(AutoClaim.CLAIM_TYPE)).thenReturn(autoClaims);
    when(claimMapper.toDto(autoClaim1)).thenReturn(autoClaimDto1);
    when(claimMapper.toDto(autoClaim2)).thenReturn(autoClaimDto2);

    // When: Requesting all auto claims
    List<ClaimDto> result = claimService.getAllClaimsByType(ClaimTypeEnum.AUTO_CLAIM_DTO);

    // Then: Verify correct claims are returned
    assertThat(result).hasSize(2).containsExactly(autoClaimDto1, autoClaimDto2);
    verify(claimRepository).findClaimByClaimType(AutoClaim.CLAIM_TYPE);
  }

  private Policy createPolicy(Long id, PolicyType type, PolicyStatus status) {
    Policy policy = new Policy();
    policy.setId(id);
    policy.setType(type);
    policy.setStatus(status);
    return policy;
  }
}
