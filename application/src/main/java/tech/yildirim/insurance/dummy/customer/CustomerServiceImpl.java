package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.CustomerDto;
import tech.yildirim.insurance.dummy.policy.PolicyRepository;

/**
 * Implementation of the {@link CustomerService} interface. Contains the business logic for customer
 * management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerMapper customerMapper;
  private final PolicyRepository policyRepository;

  @Override
  public List<CustomerDto> findAllCustomers() {
    log.info("Request to find all customers");
    List<Customer> customers = customerRepository.findAll();
    log.info("Found {} customers", customers.size());
    return customerMapper.toDtoList(customers);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerDto> findCustomerById(Long id) {
    log.info("Request to find customer by id: {}", id);
    return customerRepository.findById(id).map(customerMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerDto> findCustomersByName(String name) {
    log.info("Request to find customers with name: {}", name);
    List<Customer> customers = customerRepository.searchByName(name);
    log.info("Found {} customers with name: {}", customers.size(), name);
    return customerMapper.toDtoList(customers);
  }

  @Override
  public Optional<CustomerDto> findCustomerByPolicyNumber(String policyNumber) {
    log.info("Request to find customer by policy number: {}", policyNumber);
    return policyRepository
        .findByPolicyNumber(policyNumber)
        .map(policy -> customerMapper.toDto(policy.getCustomer()));
  }

  @Override
  @Transactional
  public CustomerDto createCustomer(CustomerDto customerDto) {
    log.info("Request to create customer: {}", customerDto.getEmail());
    Customer customer = customerMapper.toEntity(customerDto);
    Customer savedCustomer = customerRepository.save(customer);
    log.info("Successfully created customer with id {}", savedCustomer.getId());
    return customerMapper.toDto(savedCustomer);
  }

  @Override
  @Transactional
  public Optional<CustomerDto> updateCustomer(Long id, CustomerDto customerDto) {
    log.info("Request to update customer with id: {}", id);
    Optional<Customer> existingCustomerOptional = customerRepository.findById(id);

    if (existingCustomerOptional.isEmpty()) {
      log.warn("Failed to update. Customer with id: {} not found.", id);
      return Optional.empty();
    }

    Customer existingCustomer = existingCustomerOptional.get();
    customerMapper.updateCustomerFromDto(customerDto, existingCustomer);
    Customer updatedCustomer = customerRepository.save(existingCustomer);
    log.info("Successfully updated customer with id: {}", id);

    return Optional.of(customerMapper.toDto(updatedCustomer));
  }

  @Override
  @Transactional
  public boolean deleteCustomerById(Long id) {
    log.info("Request to delete customer with id: {}", id);
    if (!customerRepository.existsById(id)) {
      log.warn("Could not delete customer with id: {}. It was not found.", id);
      return false;
    }
    customerRepository.deleteById(id);
    log.info("Successfully deleted customer with id: {}", id);
    return true;
  }
}
