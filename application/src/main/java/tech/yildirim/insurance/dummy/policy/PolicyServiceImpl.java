package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.agency.Agency;
import tech.yildirim.insurance.dummy.agency.AgencyRepository;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.customer.Customer;
import tech.yildirim.insurance.dummy.customer.CustomerRepository;

/** Implementation of the {@link PolicyService} interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

  private final PolicyRepository policyRepository;
  private final CustomerRepository customerRepository;
  private final PolicyMapper policyMapper;
  private final AgencyRepository agencyRepository;

  @Override
  @Transactional
  public PolicyDto createPolicy(PolicyDto policyDto) {
    log.info("Request to create a new policy for customerId: {}", policyDto.getCustomerId());
    Customer customer =
        customerRepository
            .findById(policyDto.getCustomerId())
            .orElseThrow(
                () -> {
                  log.warn(
                      "Cannot create policy. Customer not found with id: {}",
                      policyDto.getCustomerId());
                  return new ResourceNotFoundException(
                      "Customer not found with id: " + policyDto.getCustomerId());
                });
    Policy policy = policyMapper.toEntity(policyDto);
    policy.setCustomer(customer);
    policy.setPolicyNumber(generatePolicyNumber());
    policy.setStatus(PolicyStatus.PENDING);

    if (policyDto.getAgencyId() != null) {
      log.debug("Policy creation includes agencyId: {}", policyDto.getAgencyId());
      Agency agency =
          agencyRepository
              .findById(policyDto.getAgencyId())
              .orElseThrow(
                  () -> {
                    log.warn(
                        "Cannot create policy. Agency not found with id: {}",
                        policyDto.getAgencyId());
                    return new ResourceNotFoundException(
                        "Agency not found with id: " + policyDto.getAgencyId());
                  });
      policy.setAgency(agency);
    }

    Policy savedPolicy = policyRepository.save(policy);
    log.info(
        "Successfully created policy with id {} and number {}",
        savedPolicy.getId(),
        savedPolicy.getPolicyNumber());
    return policyMapper.toDto(savedPolicy);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PolicyDto> findPolicyById(Long id) {
    log.info("Request to find policy by id: {}", id);
    return policyRepository.findById(id).map(policyMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PolicyDto> findAllPolicies() {
    log.info("Request to find all policies");
    List<Policy> policies = policyRepository.findAll();
    log.info("Found {} policies", policies.size());
    return policyMapper.toDtoList(policies);
  }

  @Override
  @Transactional
  public Optional<PolicyDto> updatePolicy(Long id, PolicyDto policyDto) {
    log.info("Request to update policy with id: {}", id);
    return policyRepository
        .findById(id)
        .map(
            existingPolicy -> {
              log.debug("Found policy with id {}, proceeding with update.", id);
              policyMapper.updatePolicyFromDto(policyDto, existingPolicy);
              if (policyDto.getAgencyId() != null) {
                log.debug("Updating policy with agencyId: {}", policyDto.getAgencyId());
                Agency agency =
                    agencyRepository
                        .findById(policyDto.getAgencyId())
                        .orElseThrow(
                            () -> {
                              log.warn(
                                  "Cannot update policy. Agency not found with id: {}",
                                  policyDto.getAgencyId());
                              return new ResourceNotFoundException(
                                  "Agency not found with id: " + policyDto.getAgencyId());
                            });
                existingPolicy.setAgency(agency);
              } else {
                log.debug("Removing agency from policy id: {}", id);
                existingPolicy.setAgency(null);
              }
              policyRepository.save(existingPolicy);
              log.info("Successfully updated policy with id: {}", id);
              return policyMapper.toDto(existingPolicy);
            });
  }

  @Override
  public List<PolicyDto> findPoliciesByCustomerId(Long customerId) {
    log.info("Request to find policies for customer id: {}", customerId);
    if (!customerRepository.existsById(customerId)) {
      log.warn("Cannot find policies. Customer with id: {} not found.", customerId);
      throw new ResourceNotFoundException("Customer not found with id: " + customerId);
    }
    List<Policy> policies = policyRepository.findByCustomerId(customerId);
    log.info("Found {} policies for customer id: {}", policies.size(), customerId);
    return policyMapper.toDtoList(policies);
  }

  /**
   * Generates a simple unique policy number. In a real-world scenario, this could be a more complex
   * sequential number generator.
   *
   * @return A unique policy number string.
   */
  private String generatePolicyNumber() {
    // Example: POL-A81BC2A-43A9
    String randomPart = UUID.randomUUID().toString().substring(0, 13);
    String policyNumber = "POL-" + randomPart.toUpperCase();
    log.debug("Generated new policy number: {}", policyNumber);
    return policyNumber;
  }
}
