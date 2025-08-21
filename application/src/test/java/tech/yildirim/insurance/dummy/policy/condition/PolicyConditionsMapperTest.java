package tech.yildirim.insurance.dummy.policy.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.CancellationPenaltyRuleDto;
import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

@DisplayName("Policy Conditions Mapper Unit Tests")
class PolicyConditionsMapperTest {

  private PolicyConditionsMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(PolicyConditionsMapper.class);
  }

  @Test
  @DisplayName("Should correctly map Entity to DTO, including nested list")
  void shouldMapEntityToDto() {
    // Given: An entity with a nested list of rules
    CancellationPenaltyRule rule1 = new CancellationPenaltyRule(1L, 6, new BigDecimal("0.20"));
    CancellationPenaltyRule rule2 = new CancellationPenaltyRule(2L, 1, new BigDecimal("0.05"));
    PolicyConditions entity =
        new PolicyConditions(1L, 14, new BigDecimal("0.05"), new ArrayList<>(List.of(rule1, rule2)));

    // When: Mapping to DTO
    PolicyConditionsDto dto = mapper.toDto(entity);

    // Then: The DTO and its nested list should be correctly mapped
    assertThat(dto).isNotNull();
    assertThat(dto.getFreeCancellationDays()).isEqualTo(14);
    assertThat(dto.getNoClaimBonusPercentage()).isEqualTo(new BigDecimal("0.05"));
    assertThat(dto.getCancellationRules()).hasSize(2);
    assertThat(dto.getCancellationRules().getFirst().getMonthsRemainingThreshold()).isEqualTo(6);
    assertThat(dto.getCancellationRules().getFirst().getPenaltyPercentage())
        .isEqualTo(new BigDecimal("0.20"));
  }

  @Test
  @DisplayName("Should correctly update an existing Entity from a DTO")
  void shouldUpdateEntityFromDto() {
    // Given: An existing entity and a DTO with new data
    PolicyConditions existingEntity =
        new PolicyConditions(1L, 14, new BigDecimal("0.05"), new ArrayList<>());

    CancellationPenaltyRuleDto newRuleDto =
        new CancellationPenaltyRuleDto()
            .monthsRemainingThreshold(12)
            .penaltyPercentage(new BigDecimal("0.30"));
    PolicyConditionsDto updateDto =
        new PolicyConditionsDto()
            .freeCancellationDays(30)
            .noClaimBonusPercentage(new BigDecimal("0.07"))
            .cancellationRules(List.of(newRuleDto));

    // When: Updating the entity from the DTO
    mapper.updateEntityFromDto(updateDto, existingEntity);

    // Then: The entity's fields should be updated
    assertThat(existingEntity.getId()).isEqualTo(1L); // ID should not be changed
    assertThat(existingEntity.getFreeCancellationDays()).isEqualTo(30);
    assertThat(existingEntity.getNoClaimBonusPercentage()).isEqualTo(new BigDecimal("0.07"));
    assertThat(existingEntity.getCancellationRules()).hasSize(1);
    assertThat(existingEntity.getCancellationRules().getFirst().getMonthsRemainingThreshold())
        .isEqualTo(12);
  }
}
