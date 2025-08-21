package tech.yildirim.insurance.dummy.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tech.yildirim.insurance.dummy.common.Address;

@DataJpaTest
@DisplayName("Customer Repository Integration Tests")
class CustomerRepositoryTest {

  @Autowired private TestEntityManager testEntityManager;

  @Autowired private CustomerRepository customerRepository;

  private Customer customer1;
  private Customer customer2;

  @BeforeEach
  void setUp() {
    customer1 = new Customer();
    customer1.setFirstName("John");
    customer1.setLastName("Doe");
    customer1.setDateOfBirth(LocalDate.of(1985, 5, 20));
    customer1.setPassword("pass1");
    customer1.setEmail("john.doe@example.com");
    customer1.setAddress(new Address("Musterstrasse 1", "11111", "Berlin", "Germany"));

    customer2 = new Customer();
    customer2.setFirstName("Jane");
    customer2.setLastName("Dalton");
    customer2.setDateOfBirth(LocalDate.of(1992, 8, 10));
    customer2.setPassword("pass2");
    customer2.setEmail("jane.dalton@example.com");
    customer2.setAddress(new Address("Musterstrasse 2", "22222", "Munich", "Germany"));

    testEntityManager.persist(customer1);
    testEntityManager.persist(customer2);
    testEntityManager.flush();
  }

  @Test
  @DisplayName("Should find customers by a part of the first name, case-insensitive")
  void shouldFindCustomersByFirstNameContaining() {
    List<Customer> foundCustomers = customerRepository.searchByName("ohn");

    assertThat(foundCustomers).hasSize(1);
    assertThat(foundCustomers.getFirst().getFirstName()).isEqualTo("John");
  }

  @Test
  @DisplayName("Should find customers by a part of the last name, case-insensitive")
  void shouldFindCustomersByLastNameContaining() {
    List<Customer> foundCustomers = customerRepository.searchByName("do");

    assertThat(foundCustomers).hasSize(1);
    assertThat(foundCustomers.getFirst().getLastName()).isEqualTo("Doe");
  }

  @Test
  @DisplayName("Should find multiple customers if search term matches multiple last names")
  void shouldFindMultipleCustomersByLastName() {
    // When: Searching for a common part "d" in last names
    List<Customer> foundCustomers = customerRepository.searchByName("d");

    // Then: It should find both customers ("Doe" and "Dalton")
    assertThat(foundCustomers).hasSize(2);
  }

  @Test
  @DisplayName("Should return an empty list if no customer name matches")
  void shouldReturnEmptyListForNoMatch() {
    // When: Searching for a non-existent name
    List<Customer> foundCustomers = customerRepository.searchByName("nonexistent");

    // Then: The list should be empty
    assertThat(foundCustomers).isEmpty();
  }
}
