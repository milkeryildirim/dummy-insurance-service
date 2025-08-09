package tech.yildirim.insurance.dummy.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tech.yildirim.insurance.dummy.customer.Address;
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
}
