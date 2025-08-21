package tech.yildirim.insurance.dummy.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tech.yildirim.insurance.dummy.common.Address;
import tech.yildirim.insurance.dummy.customer.Customer;

@DataJpaTest
class PolicyRepositoryTest {

  @Autowired private TestEntityManager testEntityManager;

  @Autowired private PolicyRepository policyRepository;

  @Test
  @DisplayName("Should save a policy and find it by ID")
  void shouldSaveAndFindPolicy() {
    // Given: A customer to associate the policy with
    Customer customer = new Customer();
    customer.setFirstName("Test");
    customer.setLastName("Customer");
    customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
    customer.setPassword("secret");
    customer.setEmail("test.custom@example.com");

    Address address = new Address();
    address.setStreetAndHouseNumber("Musterstrasse 1");
    address.setCity("Musterstadt");
    address.setPostalCode("65000");
    address.setCountry("Germany");
    customer.setAddress(address);
    Customer persistedCustomer = testEntityManager.persist(customer);

    // And: A new policy
    Policy newPolicy = new Policy();
    newPolicy.setPolicyNumber("POL-REPO-TEST");
    newPolicy.setStartDate(LocalDate.now());
    newPolicy.setEndDate(LocalDate.now().plusYears(1));
    newPolicy.setType(PolicyType.HOME);
    newPolicy.setStatus(PolicyStatus.ACTIVE);
    newPolicy.setPremium(new BigDecimal("500.00"));
    newPolicy.setCustomer(persistedCustomer);

    // When: Saving the policy
    Policy savedPolicy = policyRepository.save(newPolicy);
    testEntityManager.flush();
    testEntityManager.clear();

    // Then: The policy can be found by its ID and has the correct data
    Optional<Policy> foundPolicyOptional = policyRepository.findById(savedPolicy.getId());

    assertThat(foundPolicyOptional).isPresent();
    Policy foundPolicy = foundPolicyOptional.get();
    assertThat(foundPolicy.getPolicyNumber()).isEqualTo("POL-REPO-TEST");
    assertThat(foundPolicy.getCustomer().getId()).isEqualTo(persistedCustomer.getId());
  }

  @Test
  @DisplayName("Should find all policies for a given customer ID")
  void shouldFindByCustomerId() {
    // Given: Two customers and three policies
    Address address = new Address();
    address.setStreetAndHouseNumber("Musterstrasse 1");
    address.setCity("Musterstadt");
    address.setPostalCode("65000");
    address.setCountry("Germany");
    Customer customer1 =
        new Customer(
            null,
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            address,
            "pass1",
            "john.doe@example.com",
            null,
            null);
    Customer customer2 =
        new Customer(
            null,
            "Jane",
            "Smith",
            LocalDate.of(1992, 2, 2),
            address,
            "pass2",
            "jane.smith@example.com",
            null,
            null);
    testEntityManager.persist(customer1);
    testEntityManager.persist(customer2);

    Policy policy1C1 =
        new Policy(
            null,
            "P001",
            LocalDate.now(),
            LocalDate.now(),
            PolicyType.AUTO,
            PolicyStatus.ACTIVE,
            BigDecimal.TEN,
            customer1,
            null,
            null,
            null);
    Policy policy2C1 =
        new Policy(
            null,
            "P002",
            LocalDate.now(),
            LocalDate.now(),
            PolicyType.HOME,
            PolicyStatus.ACTIVE,
            BigDecimal.TEN,
            customer1,
            null,
            null,
            null);
    Policy policy3C2 =
        new Policy(
            null,
            "P003",
            LocalDate.now(),
            LocalDate.now(),
            PolicyType.HEALTH,
            PolicyStatus.ACTIVE,
            BigDecimal.TEN,
            customer2,
            null,
            null,
            null);
    testEntityManager.persist(policy1C1);
    testEntityManager.persist(policy2C1);
    testEntityManager.persist(policy3C2);
    testEntityManager.flush();

    // When: Searching for policies of customer1
    List<Policy> foundPolicies = policyRepository.findByCustomerId(customer1.getId());

    // Then: Exactly two policies should be found
    assertThat(foundPolicies).hasSize(2);
    assertThat(foundPolicies)
        .extracting(Policy::getPolicyNumber)
        .containsExactlyInAnyOrder("P001", "P002");

    // When: Searching for policies of customer2
    foundPolicies = policyRepository.findByCustomerId(customer2.getId());

    // Then: Exactly one policy should be found
    assertThat(foundPolicies).hasSize(1);
    assertThat(foundPolicies.getFirst().getPolicyNumber()).isEqualTo("P003");
  }
}
