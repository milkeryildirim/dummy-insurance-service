package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.employee.EmployeeRole;
import tech.yildirim.insurance.dummy.policy.Policy;

@DisplayName("Claim Mapper Unit Tests")
class ClaimMapperTest {

  private ClaimMapper claimMapper;
  private Policy testPolicy;
  private Employee testEmployee;

  @BeforeEach
  void setUp() {
    claimMapper = ClaimMapper.INSTANCE;

    testPolicy = new Policy();
    testPolicy.setId(201L);

    testEmployee = new Employee();
    testEmployee.setId(301L);
    testEmployee.setFirstName("John");
    testEmployee.setLastName("Adjuster");
    testEmployee.setPhoneNumber("+1-555-0123");
    testEmployee.setEmail("john.adjuster@insurance.com");
    testEmployee.setRole(EmployeeRole.CLAIMS_ADJUSTER);
  }

  // ========== AutoClaim Tests ==========

  @Test
  @DisplayName("Should correctly map AutoClaim entity to ClaimDto with all fields")
  void shouldMapAutoClaimEntityToDto() {
    // Given: An AutoClaim entity with all fields populated
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(1L);
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Fender bender accident on highway");
    autoClaim.setDateOfIncident(LocalDate.of(2025, 1, 15));
    autoClaim.setDateReported(ZonedDateTime.now());
    autoClaim.setStatus(ClaimStatus.IN_REVIEW);
    autoClaim.setEstimatedAmount(new BigDecimal("2500.75"));
    autoClaim.setPaidAmount(new BigDecimal("2000.00"));
    autoClaim.setPolicy(testPolicy);
    autoClaim.setAssignedAdjuster(testEmployee);
    // AutoClaim specific fields
    autoClaim.setLicensePlate("BMW-1234");
    autoClaim.setVehicleVin("VIN123456789ABCDEF");
    autoClaim.setAccidentLocation("Highway A10, Exit 15");

    // When: Mapping to DTO
    AutoClaimDto claimDto = claimMapper.toDto(autoClaim);

    // Then: All common fields should be mapped correctly
    assertThat(claimDto).isNotNull();
    assertThat(claimDto.getId()).isEqualTo(1L);
    assertThat(claimDto.getClaimNumber()).isEqualTo("CLM-AUTO-001");
    assertThat(claimDto.getDescription()).isEqualTo("Fender bender accident on highway");
    assertThat(claimDto.getDateOfIncident()).isEqualTo(LocalDate.of(2025, 1, 15));
    assertThat(claimDto.getDateReported()).isNotNull(); // Should be mapped from dateReported
    assertThat(claimDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.IN_REVIEW);
    assertThat(claimDto.getEstimatedAmount()).isEqualTo(new BigDecimal("2500.75"));
    assertThat(claimDto.getPaidAmount()).isEqualTo(new BigDecimal("2000.00"));
    assertThat(claimDto.getPolicyId()).isEqualTo(201L);
    assertThat(claimDto.getAssignedAdjusterId()).isEqualTo(301L);
    assertThat(claimDto.getAssignedAdjusterName()).isEqualTo("John Adjuster");
    assertThat(claimDto.getAssignedAdjusterContact()).isEqualTo("+1-555-0123");

    // And: AutoClaim specific fields should be mapped
    assertThat(claimDto.getLicensePlate()).isEqualTo("BMW-1234");
    assertThat(claimDto.getVehicleVin()).isEqualTo("VIN123456789ABCDEF");
    assertThat(claimDto.getAccidentLocation()).isEqualTo("Highway A10, Exit 15");
  }

  @Test
  @DisplayName("Should correctly map AutoClaim entity to ClaimDto with null adjuster")
  void shouldMapAutoClaimEntityToDtoWithNullAdjuster() {
    // Given: An AutoClaim entity without assigned adjuster
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(2L);
    autoClaim.setPolicy(testPolicy);
    autoClaim.setLicensePlate("TEST-456");
    autoClaim.setAssignedAdjuster(null);

    // When: Mapping to DTO
    AutoClaimDto claimDto = claimMapper.toDto(autoClaim);

    // Then: Adjuster contact should be null
    assertThat(claimDto.getAssignedAdjusterContact()).isNull();
    assertThat(claimDto.getLicensePlate()).isEqualTo("TEST-456");
  }

  @Test
  @DisplayName("Should correctly populate AutoClaim entity from ClaimDto")
  void shouldPopulateAutoClaimFromDto() {
    // Given: An empty AutoClaim entity and a DTO with data
    AutoClaim emptyAutoClaim = new AutoClaim();
    AutoClaimDto dto =
        new AutoClaimDto()
            .description("New auto claim description")
            .dateOfIncident(LocalDate.of(2025, 2, 10))
            .estimatedAmount(new BigDecimal("3500.00"))
            .licensePlate("NEW-789")
            .vehicleVin("NEWVIN987654321")
            .accidentLocation("Downtown intersection");

    // When: Populating the entity from the DTO
    claimMapper.populateAutoClaimFromDto(dto, emptyAutoClaim);

    // Then: The entity should be populated with the DTO's data
    assertThat(emptyAutoClaim.getDescription()).isEqualTo("New auto claim description");
    assertThat(emptyAutoClaim.getDateOfIncident()).isEqualTo(LocalDate.of(2025, 2, 10));
    assertThat(emptyAutoClaim.getEstimatedAmount()).isEqualTo(new BigDecimal("3500.00"));
    assertThat(emptyAutoClaim.getLicensePlate()).isEqualTo("NEW-789");
    assertThat(emptyAutoClaim.getVehicleVin()).isEqualTo("NEWVIN987654321");
    assertThat(emptyAutoClaim.getAccidentLocation()).isEqualTo("Downtown intersection");

    // And: Ignored fields should remain null/default
    assertThat(emptyAutoClaim.getId()).isNull();
    assertThat(emptyAutoClaim.getClaimNumber()).isNull();
    assertThat(emptyAutoClaim.getPolicy()).isNull();
    assertThat(emptyAutoClaim.getDateReported()).isNull();
    assertThat(emptyAutoClaim.getStatus()).isNull();
    assertThat(emptyAutoClaim.getPaidAmount()).isNull();
    assertThat(emptyAutoClaim.getAssignedAdjuster()).isNull();
  }

  // ========== HomeClaim Tests ==========

  @Test
  @DisplayName("Should correctly map HomeClaim entity to ClaimDto with all fields")
  void shouldMapHomeClaimEntityToDto() {
    // Given: A HomeClaim entity with all fields populated
    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(3L);
    homeClaim.setClaimNumber("CLM-HOME-002");
    homeClaim.setDescription("Water damage from burst pipe");
    homeClaim.setDateOfIncident(LocalDate.of(2025, 1, 20));
    homeClaim.setDateReported(ZonedDateTime.now());
    homeClaim.setStatus(ClaimStatus.SUBMITTED);
    homeClaim.setEstimatedAmount(new BigDecimal("5000.00"));
    homeClaim.setPaidAmount(new BigDecimal("4500.00"));
    homeClaim.setPolicy(testPolicy);
    homeClaim.setAssignedAdjuster(testEmployee);
    // HomeClaim specific fields
    homeClaim.setTypeOfDamage("Water damage");
    homeClaim.setDamagedItems("Living room carpet, kitchen cabinets, basement walls");

    // When: Mapping to DTO
    HomeClaimDto claimDto = claimMapper.toDto(homeClaim);

    // Then: All common fields should be mapped correctly
    assertThat(claimDto).isNotNull();
    assertThat(claimDto.getId()).isEqualTo(3L);
    assertThat(claimDto.getClaimNumber()).isEqualTo("CLM-HOME-002");
    assertThat(claimDto.getDescription()).isEqualTo("Water damage from burst pipe");
    assertThat(claimDto.getDateOfIncident()).isEqualTo(LocalDate.of(2025, 1, 20));
    assertThat(claimDto.getDateReported()).isNotNull();
    assertThat(claimDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.SUBMITTED);
    assertThat(claimDto.getEstimatedAmount()).isEqualTo(new BigDecimal("5000.00"));
    assertThat(claimDto.getPaidAmount()).isEqualTo(new BigDecimal("4500.00"));
    assertThat(claimDto.getPolicyId()).isEqualTo(201L);
    assertThat(claimDto.getAssignedAdjusterId()).isEqualTo(301L);
    assertThat(claimDto.getAssignedAdjusterName()).isEqualTo("John Adjuster");
    assertThat(claimDto.getAssignedAdjusterContact()).isEqualTo("+1-555-0123");

    // And: HomeClaim specific fields should be mapped
    assertThat(claimDto.getTypeOfDamage()).isEqualTo("Water damage");
    assertThat(claimDto.getDamagedItems())
        .isEqualTo("Living room carpet, kitchen cabinets, basement walls");
  }

  @Test
  @DisplayName("Should correctly map HomeClaim entity to ClaimDto with null adjuster")
  void shouldMapHomeClaimEntityToDtoWithNullAdjuster() {
    // Given: A HomeClaim entity without assigned adjuster
    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(6L);
    homeClaim.setPolicy(testPolicy);
    homeClaim.setTypeOfDamage("Fire damage");
    homeClaim.setAssignedAdjuster(null);

    // When: Mapping to DTO
    HomeClaimDto claimDto = claimMapper.toDto(homeClaim);

    // Then: Adjuster fields should be null
    assertThat(claimDto.getAssignedAdjusterId()).isNull();
    assertThat(claimDto.getAssignedAdjusterName()).isNull();
    assertThat(claimDto.getAssignedAdjusterContact()).isNull();
    assertThat(claimDto.getTypeOfDamage()).isEqualTo("Fire damage");
  }

  @Test
  @DisplayName("Should correctly populate HomeClaim entity from ClaimDto")
  void shouldPopulateHomeClaimFromDto() {
    // Given: An empty HomeClaim entity and a DTO with data
    HomeClaim emptyHomeClaim = new HomeClaim();
    HomeClaimDto dto =
        new HomeClaimDto()
            .description("New home claim description")
            .dateOfIncident(LocalDate.of(2025, 3, 5))
            .estimatedAmount(new BigDecimal("7500.50"))
            .typeOfDamage("Fire damage")
            .damagedItems("Roof, attic insulation, electrical wiring");

    // When: Populating the entity from the DTO

    claimMapper.populateHomeClaimFromDto(dto, emptyHomeClaim);

    // Then: The entity should be populated with the DTO's data
    assertThat(emptyHomeClaim.getDescription()).isEqualTo("New home claim description");
    assertThat(emptyHomeClaim.getDateOfIncident()).isEqualTo(LocalDate.of(2025, 3, 5));
    assertThat(emptyHomeClaim.getEstimatedAmount()).isEqualTo(new BigDecimal("7500.50"));
    assertThat(emptyHomeClaim.getTypeOfDamage()).isEqualTo("Fire damage");
    assertThat(emptyHomeClaim.getDamagedItems())
        .isEqualTo("Roof, attic insulation, electrical wiring");

    // And: Ignored fields should remain null/default
    assertThat(emptyHomeClaim.getId()).isNull();
    assertThat(emptyHomeClaim.getClaimNumber()).isNull();
    assertThat(emptyHomeClaim.getPolicy()).isNull();
    assertThat(emptyHomeClaim.getDateReported()).isNull();
    assertThat(emptyHomeClaim.getStatus()).isNull();
    assertThat(emptyHomeClaim.getPaidAmount()).isNull();
    assertThat(emptyHomeClaim.getAssignedAdjuster()).isNull();
  }

  // ========== HealthClaim Tests ==========

  @Test
  @DisplayName("Should correctly map HealthClaim entity to ClaimDto with all fields")
  void shouldMapHealthClaimEntityToDto() {
    // Given: A HealthClaim entity with all fields populated
    HealthClaim healthClaim = new HealthClaim();
    healthClaim.setId(4L);
    healthClaim.setClaimNumber("CLM-HEALTH-003");
    healthClaim.setDescription("Emergency surgery procedure");
    healthClaim.setDateOfIncident(LocalDate.of(2025, 1, 25));
    healthClaim.setDateReported(ZonedDateTime.now());
    healthClaim.setStatus(ClaimStatus.APPROVED);
    healthClaim.setEstimatedAmount(new BigDecimal("15000.00"));
    healthClaim.setPaidAmount(new BigDecimal("12000.00"));
    healthClaim.setPolicy(testPolicy);
    healthClaim.setAssignedAdjuster(testEmployee);
    // HealthClaim specific fields
    healthClaim.setMedicalProvider("City General Hospital");
    healthClaim.setProcedureCode("CPT-99213");

    // When: Mapping to DTO
    HealthClaimDto claimDto = claimMapper.toDto(healthClaim);

    // Then: All common fields should be mapped correctly
    assertThat(claimDto).isNotNull();
    assertThat(claimDto.getId()).isEqualTo(4L);
    assertThat(claimDto.getClaimNumber()).isEqualTo("CLM-HEALTH-003");
    assertThat(claimDto.getDescription()).isEqualTo("Emergency surgery procedure");
    assertThat(claimDto.getDateOfIncident()).isEqualTo(LocalDate.of(2025, 1, 25));
    assertThat(claimDto.getDateReported()).isNotNull();
    assertThat(claimDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.APPROVED);
    assertThat(claimDto.getEstimatedAmount()).isEqualTo(new BigDecimal("15000.00"));
    assertThat(claimDto.getPaidAmount()).isEqualTo(new BigDecimal("12000.00"));
    assertThat(claimDto.getPolicyId()).isEqualTo(201L);
    assertThat(claimDto.getAssignedAdjusterId()).isEqualTo(301L);
    assertThat(claimDto.getAssignedAdjusterName()).isEqualTo("John Adjuster");
    assertThat(claimDto.getAssignedAdjusterContact()).isEqualTo("+1-555-0123");

    // And: HealthClaim specific fields should be mapped
    assertThat(claimDto.getMedicalProvider()).isEqualTo("City General Hospital");
    assertThat(claimDto.getProcedureCode()).isEqualTo("CPT-99213");
  }

  @Test
  @DisplayName("Should correctly map HealthClaim entity to ClaimDto with null adjuster")
  void shouldMapHealthClaimEntityToDtoWithNullAdjuster() {
    // Given: A HealthClaim entity without assigned adjuster
    HealthClaim healthClaim = new HealthClaim();
    healthClaim.setId(7L);
    healthClaim.setPolicy(testPolicy);
    healthClaim.setMedicalProvider("Emergency Clinic");
    healthClaim.setAssignedAdjuster(null);

    // When: Mapping to DTO
    HealthClaimDto claimDto = claimMapper.toDto(healthClaim);

    // Then: Adjuster fields should be null
    assertThat(claimDto.getAssignedAdjusterId()).isNull();
    assertThat(claimDto.getAssignedAdjusterName()).isNull();
    assertThat(claimDto.getAssignedAdjusterContact()).isNull();
    assertThat(claimDto.getMedicalProvider()).isEqualTo("Emergency Clinic");
  }

  @Test
  @DisplayName("Should correctly populate HealthClaim entity from ClaimDto")
  void shouldPopulateHealthClaimFromDto() {
    // Given: An empty HealthClaim entity and a DTO with data
    HealthClaim emptyHealthClaim = new HealthClaim();
    HealthClaimDto dto =
        new HealthClaimDto()
            .description("New health claim description")
            .dateOfIncident(LocalDate.of(2025, 4, 1))
            .estimatedAmount(new BigDecimal("8500.25"))
            .medicalProvider("Metro Medical Center")
            .procedureCode("CPT-87654");

    // When: Populating the entity from the DTO
    claimMapper.populateHealthClaimFromDto(dto, emptyHealthClaim);

    // Then: The entity should be populated with the DTO's data
    assertThat(emptyHealthClaim.getDescription()).isEqualTo("New health claim description");
    assertThat(emptyHealthClaim.getDateOfIncident()).isEqualTo(LocalDate.of(2025, 4, 1));
    assertThat(emptyHealthClaim.getEstimatedAmount()).isEqualTo(new BigDecimal("8500.25"));
    assertThat(emptyHealthClaim.getMedicalProvider()).isEqualTo("Metro Medical Center");
    assertThat(emptyHealthClaim.getProcedureCode()).isEqualTo("CPT-87654");

    // And: Ignored fields should remain null/default
    assertThat(emptyHealthClaim.getId()).isNull();
    assertThat(emptyHealthClaim.getClaimNumber()).isNull();
    assertThat(emptyHealthClaim.getPolicy()).isNull();
    assertThat(emptyHealthClaim.getDateReported()).isNull();
    assertThat(emptyHealthClaim.getStatus()).isNull();
    assertThat(emptyHealthClaim.getPaidAmount()).isNull();
    assertThat(emptyHealthClaim.getAssignedAdjuster()).isNull();
  }

  // ========== Generic Claim Tests ==========

  @Test
  @DisplayName("Should correctly map generic Claim entity to ClaimDto")
  void shouldMapGenericClaimEntityToDto() {
    // Given: A generic Claim entity (using AutoClaim as example)
    AutoClaim claim = new AutoClaim();
    claim.setId(5L);
    claim.setClaimNumber("CLM-GENERIC-001");
    claim.setDescription("Generic claim test");
    claim.setPolicy(testPolicy);
    claim.setAssignedAdjuster(testEmployee);

    // When: Mapping to DTO using generic method
    AutoClaimDto claimDto = claimMapper.toDto(claim);

    // Then: Common fields should be mapped correctly
    assertThat(claimDto).isNotNull();
    assertThat(claimDto.getId()).isEqualTo(5L);
    assertThat(claimDto.getClaimNumber()).isEqualTo("CLM-GENERIC-001");
    assertThat(claimDto.getDescription()).isEqualTo("Generic claim test");
    assertThat(claimDto.getPolicyId()).isEqualTo(201L);
    assertThat(claimDto.getAssignedAdjusterContact()).isEqualTo("+1-555-0123");
  }

  @Test
  @DisplayName("Should correctly map list of Claims to list of ClaimDtos")
  void shouldMapClaimListToDtoList() {
    // Given: A list of different claim types
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(8L);
    autoClaim.setPolicy(testPolicy);
    autoClaim.setLicensePlate("LIST-001");

    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(9L);
    homeClaim.setPolicy(testPolicy);
    homeClaim.setTypeOfDamage("Storm damage");

    HealthClaim healthClaim = new HealthClaim();
    healthClaim.setId(10L);
    healthClaim.setPolicy(testPolicy);
    healthClaim.setMedicalProvider("City Hospital");

    java.util.List<Claim> claims = java.util.Arrays.asList(autoClaim, homeClaim, healthClaim);

    // When: Mapping to DTO list - individual mapping since toDtoList may not exist
    java.util.List<ClaimDto> claimDtos =
        claims.stream()
            .map(
                claim -> {
                  if (claim instanceof AutoClaim) {
                    return claimMapper.toDto((AutoClaim) claim);
                  } else if (claim instanceof HomeClaim) {
                    return claimMapper.toDto((HomeClaim) claim);
                  } else if (claim instanceof HealthClaim) {
                    return claimMapper.toDto((HealthClaim) claim);
                  }
                  throw new IllegalArgumentException("Unknown claim type: " + claim.getClass());
                })
            .toList();

    // Then: List should be mapped correctly
    assertThat(claimDtos).hasSize(3);

    // Check AutoClaim mapping - cast to specific type
    tech.yildirim.insurance.api.generated.model.AutoClaimDto autoClaimDto =
        (tech.yildirim.insurance.api.generated.model.AutoClaimDto) claimDtos.getFirst();
    assertThat(autoClaimDto.getId()).isEqualTo(8L);
    assertThat(autoClaimDto.getLicensePlate()).isEqualTo("LIST-001");

    // Check HomeClaim mapping - cast to specific type
    tech.yildirim.insurance.api.generated.model.HomeClaimDto homeClaimDto =
        (tech.yildirim.insurance.api.generated.model.HomeClaimDto) claimDtos.get(1);
    assertThat(homeClaimDto.getId()).isEqualTo(9L);
    assertThat(homeClaimDto.getTypeOfDamage()).isEqualTo("Storm damage");

    // Check HealthClaim mapping - cast to specific type
    tech.yildirim.insurance.api.generated.model.HealthClaimDto healthClaimDto =
        (tech.yildirim.insurance.api.generated.model.HealthClaimDto) claimDtos.get(2);
    assertThat(healthClaimDto.getId()).isEqualTo(10L);
    assertThat(healthClaimDto.getMedicalProvider()).isEqualTo("City Hospital");
  }

  @Test
  @DisplayName("Should handle null values gracefully in mappings")
  void shouldHandleNullValuesGracefully() {
    // Given: Claims with minimal data (nulls for optional fields)
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(11L);
    autoClaim.setPolicy(testPolicy);
    autoClaim.setLicensePlate("NULL-TEST");
    // vehicleVin, accidentLocation, assignedAdjuster are null

    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(12L);
    homeClaim.setPolicy(testPolicy);
    // typeOfDamage, damagedItems, assignedAdjuster are null

    HealthClaim healthClaim = new HealthClaim();
    healthClaim.setId(13L);
    healthClaim.setPolicy(testPolicy);
    // medicalProvider, procedureCode, assignedAdjuster are null

    // When: Mapping to DTOs
    AutoClaimDto autoClaimDto = claimMapper.toDto(autoClaim);
    HomeClaimDto homeClaimDto = claimMapper.toDto(homeClaim);
    HealthClaimDto healthClaimDto = claimMapper.toDto(healthClaim);

    // Then: Should handle nulls gracefully for AutoClaim
    assertThat(autoClaimDto).isNotNull();
    assertThat(autoClaimDto.getId()).isEqualTo(11L);
    assertThat(autoClaimDto.getLicensePlate()).isEqualTo("NULL-TEST");
    assertThat(autoClaimDto.getVehicleVin()).isNull();
    assertThat(autoClaimDto.getAccidentLocation()).isNull();
    assertThat(autoClaimDto.getAssignedAdjusterId()).isNull();
    assertThat(autoClaimDto.getAssignedAdjusterName()).isNull();
    assertThat(autoClaimDto.getAssignedAdjusterContact()).isNull();

    // Then: Should handle nulls gracefully for HomeClaim
    assertThat(homeClaimDto).isNotNull();
    assertThat(homeClaimDto.getId()).isEqualTo(12L);
    assertThat(homeClaimDto.getTypeOfDamage()).isNull();
    assertThat(homeClaimDto.getDamagedItems()).isNull();
    assertThat(homeClaimDto.getAssignedAdjusterContact()).isNull();

    // Then: Should handle nulls gracefully for HealthClaim
    assertThat(healthClaimDto).isNotNull();
    assertThat(healthClaimDto.getId()).isEqualTo(13L);
    assertThat(healthClaimDto.getMedicalProvider()).isNull();
    assertThat(healthClaimDto.getProcedureCode()).isNull();
    assertThat(healthClaimDto.getAssignedAdjusterContact()).isNull();
  }

  @Test
  @DisplayName("Should correctly map all ClaimStatus enum values")
  void shouldMapAllClaimStatusEnumValues() {
    // Test all possible ClaimStatus enum values to ensure proper mapping
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(14L);
    autoClaim.setPolicy(testPolicy);
    autoClaim.setLicensePlate("STATUS-TEST");

    // Test SUBMITTED status
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    AutoClaimDto submittedDto = claimMapper.toDto(autoClaim);
    assertThat(submittedDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.SUBMITTED);

    // Test IN_REVIEW status
    autoClaim.setStatus(ClaimStatus.IN_REVIEW);
    AutoClaimDto inReviewDto = claimMapper.toDto(autoClaim);
    assertThat(inReviewDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.IN_REVIEW);

    // Test APPROVED status
    autoClaim.setStatus(ClaimStatus.APPROVED);
    AutoClaimDto approvedDto = claimMapper.toDto(autoClaim);
    assertThat(approvedDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.APPROVED);

    // Test REJECTED status
    autoClaim.setStatus(ClaimStatus.REJECTED);
    AutoClaimDto rejectedDto = claimMapper.toDto(autoClaim);
    assertThat(rejectedDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.REJECTED);

    // Test PAID status
    autoClaim.setStatus(ClaimStatus.PAID);
    AutoClaimDto paidDto = claimMapper.toDto(autoClaim);
    assertThat(paidDto.getStatus()).isEqualTo(ClaimDto.StatusEnum.PAID);
  }
}
