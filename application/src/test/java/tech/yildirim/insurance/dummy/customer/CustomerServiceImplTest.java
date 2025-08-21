package tech.yildirim.insurance.dummy.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yildirim.insurance.api.generated.model.CustomerDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Unit Tests")
class CustomerServiceImplTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerMapper customerMapper;

  @InjectMocks private CustomerServiceImpl customerService;

  @Test
  @DisplayName("Should return customer when found by ID")
  void findCustomerById_whenCustomerExists_shouldReturnCustomer() {
    // Given: A customer entity and its DTO exist
    long customerId = 1L;
    Customer fakeCustomer = new Customer();
    fakeCustomer.setId(customerId);
    fakeCustomer.setFirstName("John");

    CustomerDto fakeCustomerDto = new CustomerDto();
    fakeCustomerDto.setId(customerId);
    fakeCustomerDto.setFirstName("John");

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(fakeCustomer));
    when(customerMapper.toDto(fakeCustomer)).thenReturn(fakeCustomerDto);

    // When: The service method is called
    Optional<CustomerDto> result = customerService.findCustomerById(customerId);

    // Then: The correct DTO should be returned
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(customerId);
    assertThat(result.get().getFirstName()).isEqualTo("John");

    // Verify that the repository and mapper methods were called
    verify(customerRepository, times(1)).findById(customerId);
    verify(customerMapper, times(1)).toDto(fakeCustomer);
  }

  @Test
  @DisplayName("Should return empty optional when customer not found by ID")
  void findCustomerById_whenCustomerDoesNotExist_shouldReturnEmpty() {
    // Given: The repository will not find any customer
    long customerId = 99L;
    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    // When: The service method is called
    Optional<CustomerDto> result = customerService.findCustomerById(customerId);

    // Then: The result should be an empty Optional
    assertThat(result).isNotPresent();

    // Verify that the mapper was never called
    verify(customerMapper, never()).toDto(any(Customer.class));
  }

  @Test
  @DisplayName("Should successfully create and return a new customer")
  void createCustomer_shouldReturnCreatedCustomerDto() {

    CustomerDto inputDto =
        new CustomerDto().firstName("New").lastName("User").dateOfBirth(LocalDate.now());
    Customer entityToSave = new Customer();
    Customer savedEntity = new Customer();
    savedEntity.setId(10L);
    CustomerDto outputDto = new CustomerDto();
    outputDto.setId(10L);

    when(customerMapper.toEntity(inputDto)).thenReturn(entityToSave);
    when(customerRepository.save(entityToSave)).thenReturn(savedEntity);
    when(customerMapper.toDto(savedEntity)).thenReturn(outputDto);

    // When: The service method is called
    CustomerDto result = customerService.createCustomer(inputDto);

    // Then: The final DTO with an ID should be returned
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(10L);

    verify(customerRepository, times(1)).save(entityToSave);
  }

  @Test
  @DisplayName("Should return true when deleting an existing customer")
  void deleteCustomerById_whenCustomerExists_shouldReturnTrue() {
    // Given: The customer exists in the repository
    long customerId = 1L;
    when(customerRepository.existsById(customerId)).thenReturn(true);
    doNothing().when(customerRepository).deleteById(customerId);

    // When: The service method is called
    boolean wasDeleted = customerService.deleteCustomerById(customerId);

    // Then: The method should return true and call deleteById
    assertTrue(wasDeleted);
    verify(customerRepository, times(1)).deleteById(customerId);
  }

  @Test
  @DisplayName("Should return false when attempting to delete a non-existing customer")
  void deleteCustomerById_whenCustomerDoesNotExist_shouldReturnFalse() {
    // Given: The customer does not exist
    long customerId = 99L;
    when(customerRepository.existsById(customerId)).thenReturn(false);

    // When: The service method is called
    boolean wasDeleted = customerService.deleteCustomerById(customerId);

    // Then: The method should return false and NOT call deleteById
    assertFalse(wasDeleted);
    verify(customerRepository, never()).deleteById(anyLong());
  }
}
