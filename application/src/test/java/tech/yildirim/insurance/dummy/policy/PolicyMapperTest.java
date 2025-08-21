package tech.yildirim.insurance.dummy.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.customer.Customer;

@DisplayName("Policy Mapper Unit Tests")
class PolicyMapperTest {

  private PolicyMapper policyMapper;
  private Customer testCustomer;

  @BeforeEach
  void setUp() {
    policyMapper = PolicyMapper.INSTANCE;
    testCustomer = new Customer();
    testCustomer.setId(100L);
  }

  @Test
  @DisplayName("Should correctly map Entity to DTO, including customer ID")
  void shouldMapEntityToDto() {
    // Given: A Policy entity
    Policy policyEntity =
        new Policy(
            1L,
            "POL-TEST-001",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 12, 31),
            PolicyType.AUTO,
            PolicyStatus.ACTIVE,
            new BigDecimal("1200.50"),
            testCustomer,
            null,
            null,
            null);

    // When: Mapping to DTO
    PolicyDto policyDto = policyMapper.toDto(policyEntity);

    // Then: The DTO should have the correct values
    assertThat(policyDto).isNotNull();
    assertThat(policyDto.getId()).isEqualTo(1L);
    assertThat(policyDto.getPolicyNumber()).isEqualTo("POL-TEST-001");
    assertThat(policyDto.getPremium().doubleValue()).isEqualTo(1200.50);
    assertThat(policyDto.getType().toString()).isEqualTo(PolicyType.AUTO.toString());

    // Crucial check for the customer relationship
    assertThat(policyDto.getCustomerId()).isEqualTo(testCustomer.getId());
  }

  @Test
  @DisplayName("Should correctly map DTO to Entity, ignoring managed fields")
  void shouldMapDtoToEntity() {
    // Given: A Policy DTO
    PolicyDto policyDto =
        new PolicyDto()
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .type(PolicyDto.TypeEnum.HEALTH)
            .premium(new BigDecimal("2500.00"))
            .customerId(100L);

    // When: Mapping to Entity
    Policy policyEntity = policyMapper.toEntity(policyDto);

    // Then: The entity should have correct values and ignore specified fields
    assertThat(policyEntity).isNotNull();
    assertThat(policyEntity.getStartDate()).isEqualTo(policyDto.getStartDate());
    assertThat(policyEntity.getType()).isEqualTo(PolicyType.HEALTH);

    // Check that ignored fields are null as per our mapping rules
    assertThat(policyEntity.getId()).isNull();
    assertThat(policyEntity.getPolicyNumber()).isNull();
    assertThat(policyEntity.getCustomer())
        .isNull();
  }
}
