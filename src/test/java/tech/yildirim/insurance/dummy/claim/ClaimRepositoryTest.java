package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
import tech.yildirim.insurance.dummy.customer.Address;
import tech.yildirim.insurance.dummy.customer.Customer;
import tech.yildirim.insurance.dummy.policy.Policy;
import tech.yildirim.insurance.dummy.policy.PolicyStatus;
import tech.yildirim.insurance.dummy.policy.PolicyType;

@DataJpaTest
@DisplayName("Claim Repository Integration Tests")
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
}
