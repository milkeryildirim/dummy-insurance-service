package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class CustomerController implements CustomersApi {

  private final CustomerService customerService;
  private final PolicyService policyService;

  @Override
  public ResponseEntity<CustomerDto> createCustomer(CustomerDto customerDto) {
    CustomerDto createdCustomer = customerService.createCustomer(customerDto);
    return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> deleteCustomer(Long id) {
    boolean wasDeleted = customerService.deleteCustomerById(id);
    if (wasDeleted) {
      return ResponseEntity.noContent().build(); // 204 No Content
    } else {
      return ResponseEntity.notFound().build(); // 404 Not Found
    }
  }

  @Override
  public ResponseEntity<List<CustomerDto>> getAllCustomers(String name) {
    List<CustomerDto> customers;
    if (name != null && !name.isBlank()) {
      customers = customerService.findCustomersByName(name);
    } else {
      customers = customerService.findAllCustomers();
    }
    return ResponseEntity.ok(customers);
  }

  @Override
  public ResponseEntity<CustomerDto> getCustomerById(Long id) {
    return customerService
        .findCustomerById(id)
        .map(ResponseEntity::ok) // If customer is present, wrap it in ResponseEntity.ok()
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Otherwise, return 404
  }

  @Override
  public ResponseEntity<CustomerDto> updateCustomer(Long id, CustomerDto customerDto) {
    return customerService
        .updateCustomer(id, customerDto)
        .map(ResponseEntity::ok)
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Override
  public ResponseEntity<List<PolicyDto>> getPoliciesByCustomerId(Long id) {
    return ResponseEntity.ok(policyService.findPoliciesByCustomerId(id));
  }
}
