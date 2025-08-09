package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.customer.Customer;
import tech.yildirim.insurance.dummy.customer.CustomerRepository;

/** Implementation of the {@link PolicyService} interface. */
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

  private final PolicyRepository policyRepository;
  private final CustomerRepository customerRepository;
  private final PolicyMapper policyMapper;

  @Override
  @Transactional
  public PolicyDto createPolicy(PolicyDto policyDto) {
    Customer customer =
        customerRepository
            .findById(policyDto.getCustomerId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Customer not found with id: " + policyDto.getCustomerId()));
    Policy policy = policyMapper.toEntity(policyDto);
    policy.setCustomer(customer);
    policy.setPolicyNumber(generatePolicyNumber());
    policy.setStatus(PolicyStatus.PENDING);

    Policy savedPolicy = policyRepository.save(policy);
    return policyMapper.toDto(savedPolicy);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PolicyDto> findPolicyById(Long id) {
    return policyRepository.findById(id).map(policyMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PolicyDto> findAllPolicies() {
    return policyMapper.toDtoList(policyRepository.findAll());
  }

  @Override
  @Transactional
  public Optional<PolicyDto> updatePolicy(Long id, PolicyDto policyDto) {
    return policyRepository
        .findById(id)
        .map(
            existingPolicy -> {
              policyMapper.updatePolicyFromDto(policyDto, existingPolicy);
              policyRepository.save(existingPolicy);
              return policyMapper.toDto(existingPolicy);
            });
  }

  /**
   * Generates a simple unique policy number.
   * In a real-world scenario, this could be a more complex sequential number generator.
   * @return A unique policy number string.
   */
  private String generatePolicyNumber() {
    // Example: POL-A81BC2A-43A9
    String randomPart = UUID.randomUUID().toString().substring(0, 13);
    return "POL-" + randomPart.toUpperCase();
  }
}
