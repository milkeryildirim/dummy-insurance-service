package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.CustomerDto;

/**
 * Implementation of the {@link CustomerService} interface. Contains the business logic for customer
 * management.
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerMapper customerMapper;

  @Override
  public List<CustomerDto> findAllCustomers() {
    List<Customer> customers = customerRepository.findAll();
    return customerMapper.toDtoList(customers);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerDto> findCustomerById(Long id) {
    return customerRepository.findById(id).map(customerMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerDto> findCustomersByName(String name) {
    List<Customer> customers = customerRepository.searchByName(name);
    return customerMapper.toDtoList(customers);
  }

  @Override
  @Transactional
  public CustomerDto createCustomer(CustomerDto customerDto) {
    Customer customer = customerMapper.toEntity(customerDto);
    Customer savedCustomer = customerRepository.save(customer);
    return customerMapper.toDto(savedCustomer);
  }

  @Override
  @Transactional
  public Optional<CustomerDto> updateCustomer(Long id, CustomerDto customerDto) {
    Optional<Customer> existingCustomerOptional = customerRepository.findById(id);

    if (existingCustomerOptional.isEmpty()) {
      return Optional.empty();
    }

    Customer existingCustomer = existingCustomerOptional.get();
    customerMapper.updateCustomerFromDto(customerDto, existingCustomer);
    Customer updatedCustomer = customerRepository.save(existingCustomer);

    return Optional.of(customerMapper.toDto(updatedCustomer));
  }

  @Override
  @Transactional
  public boolean deleteCustomerById(Long id) {
    if (!customerRepository.existsById(id)) {
      return false;
    }
    customerRepository.deleteById(id);
    return true;
  }
}
