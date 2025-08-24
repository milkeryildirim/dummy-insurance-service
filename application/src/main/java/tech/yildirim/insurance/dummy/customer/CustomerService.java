package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import java.util.Optional;
import tech.yildirim.insurance.api.generated.model.CustomerDto;

/**
 * Service Interface for managing {@link Customer}. Defines the business operations for the customer
 * feature.
 */
public interface CustomerService {

  /**
   * Retrieve all customers.
   *
   * @return A list of all customers.
   */
  List<CustomerDto> findAllCustomers();

  /**
   * Find a customer by their ID.
   *
   * @param id The ID of the customer to find.
   * @return An Optional containing the found customer, or empty if not found.
   */
  Optional<CustomerDto> findCustomerById(Long id);

  /**
   * Find customers by their first or last name.
   *
   * @param name The name to search for.
   * @return A list of customers matching the name.
   */
  List<CustomerDto> findCustomersByName(String name);

  /**
   * Finds a customer by the policy number of one of their policies.
   *
   * @param policyNumber The policy number to search with.
   * @return An Optional containing the customer DTO, or empty if not found.
   */
  Optional<CustomerDto> findCustomerByPolicyNumber(String policyNumber);

  /**
   * Create a new customer.
   *
   * @param customerDto The customer to create.
   * @return The created customer.
   */
  CustomerDto createCustomer(CustomerDto customerDto);

  /**
   * Update an existing customer.
   *
   * @param id The ID of the customer to update.
   * @param customerDto The new data for the customer.
   * @return The updated customer.
   */
  Optional<CustomerDto> updateCustomer(Long id, CustomerDto customerDto);

  /**
   * Delete a customer by their ID.
   *
   * @param id The ID of the customer to delete.
   */
  boolean deleteCustomerById(Long id);
}
