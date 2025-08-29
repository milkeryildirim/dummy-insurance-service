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

  // ==================== HELPER METHODS ====================

  private Policy createPolicy(Long id, PolicyType type, PolicyStatus status) {
    Policy policy = new Policy();
    policy.setId(id);
    policy.setType(type);
    policy.setStatus(status);
    return policy;
  }
}
