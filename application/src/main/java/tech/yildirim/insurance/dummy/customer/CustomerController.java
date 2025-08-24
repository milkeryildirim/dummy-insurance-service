package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.CustomersApi;
import tech.yildirim.insurance.api.generated.model.CustomerDto;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.policy.PolicyService;

/**
 * REST Controller for managing customers. This class implements the generated {@link CustomersApi}
 * interface, ensuring that the API contract is respected.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CustomerController implements CustomersApi {

  private final CustomerService customerService;
  private final PolicyService policyService;

  @Override
  public ResponseEntity<CustomerDto> createCustomer(CustomerDto customerDto) {
    log.info("REST request to create customer with email: {}", customerDto.getEmail());
    CustomerDto createdCustomer = customerService.createCustomer(customerDto);
    log.info("Successfully created customer with id {}", createdCustomer.getId());
    return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> deleteCustomer(Long id) {
    log.info("REST request to delete customer with id: {}", id);
    boolean wasDeleted = customerService.deleteCustomerById(id);
    if (wasDeleted) {
      log.info("Successfully deleted customer with id: {}. Returning 204 No Content.", id);
      return ResponseEntity.noContent().build(); // 204 No Content
    } else {
      log.warn("Could not delete customer with id: {}. It was not found. Returning 404.", id);
      return ResponseEntity.notFound().build(); // 404 Not Found
    }
  }

  @Override
  public ResponseEntity<List<CustomerDto>> getAllCustomers(String name) {
    if (name != null && !name.isBlank()) {
      log.info("REST request to get all customers with name containing: {}", name);
      List<CustomerDto> customers = customerService.findCustomersByName(name);
      log.debug("Found {} customers with name containing '{}'", customers.size(), name);
      return ResponseEntity.ok(customers);
    } else {
      log.info("REST request to get all customers");
      List<CustomerDto> customers = customerService.findAllCustomers();
      log.debug("Found {} customers", customers.size());
      return ResponseEntity.ok(customers);
    }
  }

  @Override
  public ResponseEntity<CustomerDto> getCustomerById(Long id) {
    log.info("REST request to get customer by id: {}", id);
    return customerService
        .findCustomerById(id)
        .map(
            customer -> {
              log.info("Found customer with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok(customer);
            })
        .orElseGet(
            () -> {
              log.warn("Customer with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            });
  }

  @Override
  public ResponseEntity<CustomerDto> getCustomerByPolicyNumber(String policyNumber) {
    log.info("REST request to get customer by policy number: {}", policyNumber);
    return customerService
        .findCustomerByPolicyNumber(policyNumber)
        .map(ResponseEntity::ok)
        .orElseGet(
            () -> {
              log.warn(
                  "Customer not found for policy number: {}, returning 404 NOT FOUND",
                  policyNumber);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<CustomerDto> updateCustomer(Long id, CustomerDto customerDto) {
    log.info("REST request to update customer with id: {}", id);
    return customerService
        .updateCustomer(id, customerDto)
        .map(
            customer -> {
              log.info("Successfully updated customer with id: {}", id);
              return ResponseEntity.ok(customer);
            })
        .orElseGet(
            () -> {
              log.warn("Failed to update. Customer with id: {} not found.", id);
              return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            });
  }

  @Override
  public ResponseEntity<List<PolicyDto>> getPoliciesByCustomerId(Long id) {
    log.info("REST request to get policies for customer with id: {}", id);
    List<PolicyDto> policies = policyService.findPoliciesByCustomerId(id);
    log.debug("Found {} policies for customer with id: {}", policies.size(), id);
    return ResponseEntity.ok(policies);
  }
}
