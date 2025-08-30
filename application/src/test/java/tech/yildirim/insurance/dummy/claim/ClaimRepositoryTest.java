package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import tech.yildirim.insurance.dummy.common.Address;
import tech.yildirim.insurance.dummy.customer.Customer;
import tech.yildirim.insurance.dummy.policy.Policy;
import tech.yildirim.insurance.dummy.policy.PolicyStatus;
import tech.yildirim.insurance.dummy.policy.PolicyType;

@DataJpaTest
@DisplayName("Claim Repository Integration Tests")
@ActiveProfiles("test")
class ClaimRepositoryTest {

  @Autowired private TestEntityManager testEntityManager;

  @Autowired private ClaimRepository claimRepository;

  private Policy autoPolicy;
  private Policy homePolicy;

  @BeforeEach
  void setUp() {
    Address address = new Address();
    address.setStreetAndHouseNumber("Musterstrasse 1");
    address.setCity("Musterstadt");
    address.setPostalCode("65000");
    address.setCountry("Germany");
    Customer customer =
        new Customer(
            null,
            "Test",
            "User",
            LocalDate.now(),
            address,
            "pass",
            "test.user@example.com",
            null,
            null);
    testEntityManager.persist(customer);

    autoPolicy =
        new Policy(
            null,
            "POL-AUTO-1",
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            PolicyType.AUTO,
            PolicyStatus.ACTIVE,
            BigDecimal.ZERO,
            customer,
            null,
            null,
            null);
    homePolicy =
        new Policy(
            null,
            "POL-HOME-1",
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            PolicyType.HOME,
            PolicyStatus.ACTIVE,
            BigDecimal.ZERO,
            customer,
            null,
            null,
            null);
    testEntityManager.persist(autoPolicy);
    testEntityManager.persist(homePolicy);
  }

  @Test
  @DisplayName("Should persist and retrieve an AutoClaim correctly")
  void shouldPersistAndRetrieveAutoClaim() {
    // Given: A new AutoClaim
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-001");
    autoClaim.setDescription("Accident on A5");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);
    autoClaim.setLicensePlate("F-AB-123");

    // When: Saving the claim
    Claim savedClaim = claimRepository.save(autoClaim);
    testEntityManager.flush();
    testEntityManager.clear();

    // Then: The saved claim can be found and is of the correct type with correct data
    Optional<Claim> foundClaimOpt = claimRepository.findById(savedClaim.getId());
    assertThat(foundClaimOpt).isPresent();
    Claim foundClaim = foundClaimOpt.get();

    assertThat(foundClaim).isInstanceOf(AutoClaim.class);
    AutoClaim foundAutoClaim = (AutoClaim) foundClaim;
    assertThat(foundAutoClaim.getLicensePlate()).isEqualTo("F-AB-123");
    assertThat(foundAutoClaim.getDescription()).isEqualTo("Accident on A5");
  }

  @Test
  @DisplayName("Should find all claims for a specific policy ID")
  void shouldFindByPolicyId() {
    // Given: Multiple claims for different policies
    AutoClaim claim1 = new AutoClaim();
    claim1.setClaimNumber("CLM-A1");
    claim1.setDescription("Desc A1");
    claim1.setLicensePlate("F-AB-123");
    claim1.setDateOfIncident(LocalDate.now());
    claim1.setStatus(ClaimStatus.SUBMITTED);
    claim1.setPolicy(autoPolicy);

    AutoClaim claim2 = new AutoClaim();
    claim2.setClaimNumber("CLM-A2");
    claim2.setDescription("Desc A2");
    claim2.setLicensePlate("F-AB-456");
    claim2.setDateOfIncident(LocalDate.now());
    claim2.setStatus(ClaimStatus.SUBMITTED);
    claim2.setPolicy(autoPolicy);

    HomeClaim claim3 = new HomeClaim();
    claim3.setClaimNumber("CLM-H1");
    claim3.setDescription("Desc H1");
    claim3.setDateOfIncident(LocalDate.now());
    claim3.setStatus(ClaimStatus.SUBMITTED);
    claim3.setPolicy(homePolicy);

    testEntityManager.persist(claim1);
    testEntityManager.persist(claim2);
    testEntityManager.persist(claim3);
    testEntityManager.flush();

    // When: Searching for claims of the auto policy
    List<Claim> claimsForAutoPolicy = claimRepository.findByPolicyId(autoPolicy.getId());

    // Then: Exactly two claims should be found
    assertThat(claimsForAutoPolicy).hasSize(2);
    assertThat(claimsForAutoPolicy)
        .extracting(Claim::getClaimNumber)
        .containsExactlyInAnyOrder("CLM-A1", "CLM-A2");

    // When: Searching for claims of the home policy
    List<Claim> claimsForHomePolicy = claimRepository.findByPolicyId(homePolicy.getId());

    // Then: Exactly one claim should be found
    assertThat(claimsForHomePolicy).hasSize(1);
    assertThat(claimsForHomePolicy.getFirst().getClaimNumber()).isEqualTo("CLM-H1");
  }

  // ==================== FIND CLAIMS BY TYPE TESTS ====================

  @Test
  @DisplayName("Should find only auto claims when filtering by AUTO claim type")
  void shouldFindClaimsByClaimType_Auto() {
    // Given: Mixed claim types in the database
    AutoClaim autoClaim1 = new AutoClaim();
    autoClaim1.setClaimNumber("CLM-AUTO-001");
    autoClaim1.setDescription("Car accident claim 1");
    autoClaim1.setLicensePlate("F-AUTO-001");
    autoClaim1.setVehicleVin("VIN12345678901");
    autoClaim1.setDateOfIncident(LocalDate.now());
    autoClaim1.setStatus(ClaimStatus.SUBMITTED);
    autoClaim1.setPolicy(autoPolicy);

    AutoClaim autoClaim2 = new AutoClaim();
    autoClaim2.setClaimNumber("CLM-AUTO-002");
    autoClaim2.setDescription("Car accident claim 2");
    autoClaim2.setLicensePlate("F-AUTO-002");
    autoClaim2.setVehicleVin("VIN12345678902");
    autoClaim2.setDateOfIncident(LocalDate.now());
    autoClaim2.setStatus(ClaimStatus.IN_REVIEW);
    autoClaim2.setPolicy(autoPolicy);

    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setClaimNumber("CLM-HOME-001");
    homeClaim.setDescription("House fire claim");
    homeClaim.setTypeOfDamage("Fire damage");
    homeClaim.setDamagedItems("Furniture, electronics");
    homeClaim.setDateOfIncident(LocalDate.now());
    homeClaim.setStatus(ClaimStatus.SUBMITTED);
    homeClaim.setPolicy(homePolicy);

    testEntityManager.persist(autoClaim1);
    testEntityManager.persist(autoClaim2);
    testEntityManager.persist(homeClaim);
    testEntityManager.flush();

    // When: Filtering by AUTO claim type
    List<Claim> autoClaims = claimRepository.findClaimByClaimType("AUTO");

    // Then: Only auto claims should be returned
    assertThat(autoClaims).hasSize(2).allMatch(AutoClaim.class::isInstance);
    assertThat(autoClaims)
        .extracting(Claim::getClaimNumber)
        .containsExactlyInAnyOrder("CLM-AUTO-001", "CLM-AUTO-002");

    // Verify specific auto claim properties
    AutoClaim foundAutoClaim1 =
        (AutoClaim)
            autoClaims.stream()
                .filter(claim -> "CLM-AUTO-001".equals(claim.getClaimNumber()))
                .findFirst()
                .orElseThrow();
    assertThat(foundAutoClaim1.getLicensePlate()).isEqualTo("F-AUTO-001");
    assertThat(foundAutoClaim1.getVehicleVin()).isEqualTo("VIN12345678901");
  }

  @Test
  @DisplayName("Should find only home claims when filtering by HOME claim type")
  void shouldFindClaimsByClaimType_Home() {
    // Given: Mixed claim types in the database
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Car accident claim");
    autoClaim.setLicensePlate("F-AUTO-001");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);

    HomeClaim homeClaim1 = new HomeClaim();
    homeClaim1.setClaimNumber("CLM-HOME-001");
    homeClaim1.setDescription("Water damage claim");
    homeClaim1.setTypeOfDamage("Water damage");
    homeClaim1.setDamagedItems("Kitchen cabinets, flooring");
    homeClaim1.setDateOfIncident(LocalDate.now());
    homeClaim1.setStatus(ClaimStatus.SUBMITTED);
    homeClaim1.setPolicy(homePolicy);

    HomeClaim homeClaim2 = new HomeClaim();
    homeClaim2.setClaimNumber("CLM-HOME-002");
    homeClaim2.setDescription("Storm damage claim");
    homeClaim2.setTypeOfDamage("Storm damage");
    homeClaim2.setDamagedItems("Roof, windows");
    homeClaim2.setDateOfIncident(LocalDate.now());
    homeClaim2.setStatus(ClaimStatus.IN_REVIEW);
    homeClaim2.setPolicy(homePolicy);

    testEntityManager.persist(autoClaim);
    testEntityManager.persist(homeClaim1);
    testEntityManager.persist(homeClaim2);
    testEntityManager.flush();

    // When: Filtering by HOME claim type
    List<Claim> homeClaims = claimRepository.findClaimByClaimType("HOME");

    // Then: Only home claims should be returned
    assertThat(homeClaims).hasSize(2).allMatch(HomeClaim.class::isInstance);
    assertThat(homeClaims)
        .extracting(Claim::getClaimNumber)
        .containsExactlyInAnyOrder("CLM-HOME-001", "CLM-HOME-002");

    // Verify specific home claim properties
    HomeClaim foundHomeClaim1 =
        (HomeClaim)
            homeClaims.stream()
                .filter(claim -> "CLM-HOME-001".equals(claim.getClaimNumber()))
                .findFirst()
                .orElseThrow();
    assertThat(foundHomeClaim1.getTypeOfDamage()).isEqualTo("Water damage");
    assertThat(foundHomeClaim1.getDamagedItems()).isEqualTo("Kitchen cabinets, flooring");
  }

  @Test
  @DisplayName("Should find only health claims when filtering by HEALTH claim type")
  void shouldFindClaimsByClaimType_Health() {
    // Given: Mixed claim types in the database including health claims
    // First create a health policy
    Policy healthPolicy =
        new Policy(
            null,
            "POL-HEALTH-1",
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            PolicyType.HEALTH,
            PolicyStatus.ACTIVE,
            BigDecimal.ZERO,
            autoPolicy.getCustomer(), // Use the same customer
            null,
            null,
            null);
    testEntityManager.persist(healthPolicy);

    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Car accident claim");
    autoClaim.setLicensePlate("F-AUTO-001");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);

    HealthClaim healthClaim1 = new HealthClaim();
    healthClaim1.setClaimNumber("CLM-HEALTH-001");
    healthClaim1.setDescription("Emergency room visit");
    healthClaim1.setMedicalProvider("City General Hospital");
    healthClaim1.setProcedureCode("CPT-99285");
    healthClaim1.setDateOfIncident(LocalDate.now());
    healthClaim1.setStatus(ClaimStatus.SUBMITTED);
    healthClaim1.setPolicy(healthPolicy);

    HealthClaim healthClaim2 = new HealthClaim();
    healthClaim2.setClaimNumber("CLM-HEALTH-002");
    healthClaim2.setDescription("Routine checkup");
    healthClaim2.setMedicalProvider("Downtown Clinic");
    healthClaim2.setProcedureCode("CPT-99213");
    healthClaim2.setDateOfIncident(LocalDate.now());
    healthClaim2.setStatus(ClaimStatus.APPROVED);
    healthClaim2.setPolicy(healthPolicy);

    testEntityManager.persist(autoClaim);
    testEntityManager.persist(healthClaim1);
    testEntityManager.persist(healthClaim2);
    testEntityManager.flush();

    // When: Filtering by HEALTH claim type
    List<Claim> healthClaims = claimRepository.findClaimByClaimType("HEALTH");

    // Then: Only health claims should be returned
    assertThat(healthClaims).hasSize(2).allMatch(HealthClaim.class::isInstance);
    assertThat(healthClaims)
        .extracting(Claim::getClaimNumber)
        .containsExactlyInAnyOrder("CLM-HEALTH-001", "CLM-HEALTH-002");

    // Verify specific health claim properties
    HealthClaim foundHealthClaim1 =
        (HealthClaim)
            healthClaims.stream()
                .filter(claim -> "CLM-HEALTH-001".equals(claim.getClaimNumber()))
                .findFirst()
                .orElseThrow();
    assertThat(foundHealthClaim1.getMedicalProvider()).isEqualTo("City General Hospital");
    assertThat(foundHealthClaim1.getProcedureCode()).isEqualTo("CPT-99285");
  }

  @Test
  @DisplayName("Should return empty list when no claims of specified type exist")
  void shouldReturnEmptyListWhenNoClaimsOfTypeExist() {
    // Given: Only auto claims in the database
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Car accident claim");
    autoClaim.setLicensePlate("F-AUTO-001");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);

    testEntityManager.persist(autoClaim);
    testEntityManager.flush();

    // When: Filtering by HOME claim type (no home claims exist)
    List<Claim> homeClaims = claimRepository.findClaimByClaimType("HOME");

    // Then: Empty list should be returned
    assertThat(homeClaims).isEmpty();

    // When: Filtering by HEALTH claim type (no health claims exist)
    List<Claim> healthClaims = claimRepository.findClaimByClaimType("HEALTH");

    // Then: Empty list should be returned
    assertThat(healthClaims).isEmpty();
  }

  @Test
  @DisplayName("Should return empty list for invalid claim type")
  void shouldReturnEmptyListForInvalidClaimType() {
    // Given: Some claims in the database
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Car accident claim");
    autoClaim.setLicensePlate("F-AUTO-001");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);

    testEntityManager.persist(autoClaim);
    testEntityManager.flush();

    // When: Filtering by invalid claim type
    List<Claim> claims = claimRepository.findClaimByClaimType("INVALID_TYPE");

    // Then: Empty list should be returned
    assertThat(claims).isEmpty();
  }

  @Test
  @DisplayName("Should correctly filter mixed claim types and return only requested type")
  void shouldFilterMixedClaimTypesCorrectly() {
    // Given: All three types of claims in the database
    // Create health policy first
    Policy healthPolicy =
        new Policy(
            null,
            "POL-HEALTH-1",
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            PolicyType.HEALTH,
            PolicyStatus.ACTIVE,
            BigDecimal.ZERO,
            autoPolicy.getCustomer(),
            null,
            null,
            null);
    testEntityManager.persist(healthPolicy);

    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Car accident claim");
    autoClaim.setLicensePlate("F-AUTO-001");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);

    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setClaimNumber("CLM-HOME-001");
    homeClaim.setDescription("Fire damage claim");
    homeClaim.setTypeOfDamage("Fire damage");
    homeClaim.setDateOfIncident(LocalDate.now());
    homeClaim.setStatus(ClaimStatus.SUBMITTED);
    homeClaim.setPolicy(homePolicy);

    HealthClaim healthClaim = new HealthClaim();
    healthClaim.setClaimNumber("CLM-HEALTH-001");
    healthClaim.setDescription("Medical treatment claim");
    healthClaim.setMedicalProvider("Hospital");
    healthClaim.setDateOfIncident(LocalDate.now());
    healthClaim.setStatus(ClaimStatus.SUBMITTED);
    healthClaim.setPolicy(healthPolicy);

    testEntityManager.persist(autoClaim);
    testEntityManager.persist(homeClaim);
    testEntityManager.persist(healthClaim);
    testEntityManager.flush();

    // When & Then: Test each filter separately
    List<Claim> autoResults = claimRepository.findClaimByClaimType("AUTO");
    assertThat(autoResults).hasSize(1);
    assertThat(autoResults.get(0)).isInstanceOf(AutoClaim.class);
    assertThat(autoResults.get(0).getClaimNumber()).isEqualTo("CLM-AUTO-001");

    List<Claim> homeResults = claimRepository.findClaimByClaimType("HOME");
    assertThat(homeResults).hasSize(1);
    assertThat(homeResults.get(0)).isInstanceOf(HomeClaim.class);
    assertThat(homeResults.get(0).getClaimNumber()).isEqualTo("CLM-HOME-001");

    List<Claim> healthResults = claimRepository.findClaimByClaimType("HEALTH");
    assertThat(healthResults).hasSize(1);
    assertThat(healthResults.get(0)).isInstanceOf(HealthClaim.class);
    assertThat(healthResults.get(0).getClaimNumber()).isEqualTo("CLM-HEALTH-001");

    // Verify that each result contains only the correct type
    assertThat(autoResults)
        .noneMatch(claim -> claim instanceof HomeClaim || claim instanceof HealthClaim);
    assertThat(homeResults)
        .noneMatch(claim -> claim instanceof AutoClaim || claim instanceof HealthClaim);
    assertThat(healthResults)
        .noneMatch(claim -> claim instanceof AutoClaim || claim instanceof HomeClaim);
  }

  @Test
  @DisplayName("Should handle case-sensitive claim type filtering correctly")
  void shouldHandleCaseSensitiveClaimTypeFiltering() {
    // Given: An auto claim in the database
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Car accident claim");
    autoClaim.setLicensePlate("F-AUTO-001");
    autoClaim.setDateOfIncident(LocalDate.now());
    autoClaim.setStatus(ClaimStatus.SUBMITTED);
    autoClaim.setPolicy(autoPolicy);

    testEntityManager.persist(autoClaim);
    testEntityManager.flush();

    // When & Then: Test different case variations
    List<Claim> upperCaseResults = claimRepository.findClaimByClaimType("AUTO");
    assertThat(upperCaseResults).hasSize(1);

    List<Claim> lowerCaseResults = claimRepository.findClaimByClaimType("auto");
    assertThat(lowerCaseResults).isEmpty(); // Should be case-sensitive

    List<Claim> mixedCaseResults = claimRepository.findClaimByClaimType("Auto");
    assertThat(mixedCaseResults).isEmpty(); // Should be case-sensitive
  }
}
