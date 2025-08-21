package tech.yildirim.insurance.dummy.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.customer.Customer;
import tech.yildirim.insurance.dummy.customer.CustomerRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Policy Service Unit Tests")
class PolicyServiceImplTest {

  @Mock private PolicyRepository policyRepository;

  @Mock private CustomerRepository customerRepository;

  @Mock private PolicyMapper policyMapper;

  @InjectMocks private PolicyServiceImpl policyService;

  @Test
  @DisplayName("Should create policy successfully when customer exists")
  void createPolicy_whenCustomerExists_shouldCreateSuccessfully() {
    // Given: A DTO for policy creation and an existing customer
    long customerId = 1L;
    PolicyDto inputDto =
        new PolicyDto()
            .customerId(customerId)
            .type(PolicyDto.TypeEnum.AUTO)
            .premium(BigDecimal.TEN);
    Customer existingCustomer = new Customer();
    existingCustomer.setId(customerId);

    Policy policyToSave = new Policy();
    Policy savedPolicy = new Policy();
    savedPolicy.setId(101L);
    savedPolicy.setPolicyNumber("POL-generated-123");

    PolicyDto finalDto = new PolicyDto();
    finalDto.setId(101L);
    finalDto.setPolicyNumber("POL-generated-123");

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
    when(policyMapper.toEntity(inputDto)).thenReturn(policyToSave);
    when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);
    when(policyMapper.toDto(savedPolicy)).thenReturn(finalDto);

    // When: The createPolicy method is called
    PolicyDto resultDto = policyService.createPolicy(inputDto);

    // Then: The policy should be created and returned as a DTO
    assertThat(resultDto).isNotNull();
    assertThat(resultDto.getId()).isEqualTo(101L);
    assertThat(resultDto.getPolicyNumber()).isEqualTo("POL-generated-123");

    // And: Verify that the policy passed to save() has the correct customer and status
    ArgumentCaptor<Policy> policyArgumentCaptor = ArgumentCaptor.forClass(Policy.class);
    verify(policyRepository).save(policyArgumentCaptor.capture());

    Policy capturedPolicy = policyArgumentCaptor.getValue();
    assertThat(capturedPolicy.getCustomer()).isEqualTo(existingCustomer);
    assertThat(capturedPolicy.getStatus()).isEqualTo(PolicyStatus.PENDING);
  }

  @Test
  @DisplayName(
      "Should throw ResourceNotFoundException when creating a policy for a non-existent customer")
  void createPolicy_whenCustomerDoesNotExist_shouldThrowException() {
    // Given: A DTO with a non-existent customer ID
    long nonExistentCustomerId = 99L;
    PolicyDto inputDto = new PolicyDto().customerId(nonExistentCustomerId);

    when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

    // When & Then: Assert that a ResourceNotFoundException is thrown
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> {
              policyService.createPolicy(inputDto);
            });

    assertThat(exception.getMessage())
        .isEqualTo("Customer not found with id: " + nonExistentCustomerId);

    // And: Verify that the policy repository's save method was never called
    verify(policyRepository, never()).save(any(Policy.class));
  }

  @Test
  @DisplayName("Should update policy successfully when it exists")
  void updatePolicy_whenPolicyExists_shouldUpdateAndReturnDto() {
    // Given: An existing policy and a DTO with update data
    long policyId = 1L;
    PolicyDto updateDto = new PolicyDto().status(PolicyDto.StatusEnum.ACTIVE);
    Policy existingPolicy = new Policy();
    existingPolicy.setId(policyId);
    existingPolicy.setStatus(PolicyStatus.PENDING); // Initial status

    PolicyDto finalDto = new PolicyDto();
    finalDto.setId(policyId);
    finalDto.setStatus(PolicyDto.StatusEnum.ACTIVE);

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(existingPolicy));
    when(policyRepository.save(any(Policy.class))).thenReturn(existingPolicy);
    when(policyMapper.toDto(existingPolicy)).thenReturn(finalDto);

    // When: The update method is called
    Optional<PolicyDto> result = policyService.updatePolicy(policyId, updateDto);

    // Then: The updated DTO should be returned
    assertThat(result).isPresent();
    assertThat(result.get().getStatus()).isEqualTo(PolicyDto.StatusEnum.ACTIVE);

    // And: Verify the correct methods were called
    verify(policyMapper).updatePolicyFromDto(updateDto, existingPolicy);
    verify(policyRepository).save(existingPolicy);
  }

  @Test
  @DisplayName("Should return policies when customer exists")
  void findPoliciesByCustomerId_whenCustomerExists_shouldReturnPolicies() {
    // Given: An existing customer and their policies
    long customerId = 1L;
    Policy policy1 = new Policy();
    policy1.setId(101L);
    List<Policy> policies = List.of(policy1);
    PolicyDto dto1 = new PolicyDto();
    dto1.setId(101L);
    List<PolicyDto> dtoList = List.of(dto1);

    // --- Mocking ---
    when(customerRepository.existsById(customerId)).thenReturn(true);
    when(policyRepository.findByCustomerId(customerId)).thenReturn(policies);
    when(policyMapper.toDtoList(policies)).thenReturn(dtoList);

    // When: The service method is called
    List<PolicyDto> result = policyService.findPoliciesByCustomerId(customerId);

    // Then: The list of DTOs should be returned
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(101L);
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when customer does not exist")
  void findPoliciesByCustomerId_whenCustomerDoesNotExist_shouldThrowException() {
    // Given: A non-existent customer ID
    long nonExistentCustomerId = 99L;
    when(customerRepository.existsById(nonExistentCustomerId)).thenReturn(false);

    // When & Then: Assert that an exception is thrown
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          policyService.findPoliciesByCustomerId(nonExistentCustomerId);
        });

    // And: Verify that the policy repository was never queried
    verify(policyRepository, never()).findByCustomerId(anyLong());
  }
}
