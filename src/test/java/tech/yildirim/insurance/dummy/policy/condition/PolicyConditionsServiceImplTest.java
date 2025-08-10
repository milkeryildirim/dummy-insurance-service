package tech.yildirim.insurance.dummy.policy.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yildirim.insurance.api.generated.model.CancellationPenaltyRuleDto;
import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Policy Conditions Service Unit Tests")
class PolicyConditionsServiceImplTest {
  @Mock private PolicyConditionsRepository policyConditionsRepository;
  @Mock private PolicyConditionsMapper policyConditionsMapper;

  @InjectMocks private PolicyConditionsServiceImpl policyConditionsService;

  @Test
  @DisplayName("Should get conditions successfully when they exist")
  void getPolicyConditions_whenExists_shouldReturnDto() {
    // Given
    PolicyConditions conditions = new PolicyConditions();
    when(policyConditionsRepository.findById(1L)).thenReturn(Optional.of(conditions));
    when(policyConditionsMapper.toDto(conditions)).thenReturn(new PolicyConditionsDto());

    // When
    policyConditionsService.getPolicyConditions();

    // Then
    verify(policyConditionsRepository, times(1)).findById(1L);
    verify(policyConditionsMapper, times(1)).toDto(conditions);
  }

  @Test
  @DisplayName("Should throw IllegalStateException when conditions are not found")
  void getPolicyConditions_whenNotExists_shouldThrowException() {
    // Given
    when(policyConditionsRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          policyConditionsService.getPolicyConditions();
        });
  }

  @Test
  @DisplayName("Should update conditions and sort cancellation rules")
  void updatePolicyConditions_shouldUpdateAndSort() {
    // Given: An existing entity and a DTO with unsorted rules
    PolicyConditions existingConditions =
        new PolicyConditions(1L, 0, null, new ArrayList<>());

    CancellationPenaltyRuleDto rule1 =
        new CancellationPenaltyRuleDto()
            .monthsRemainingThreshold(1)
            .penaltyPercentage(BigDecimal.ONE);
    CancellationPenaltyRuleDto rule6 =
        new CancellationPenaltyRuleDto()
            .monthsRemainingThreshold(6)
            .penaltyPercentage(BigDecimal.TEN);
    PolicyConditionsDto updateDto =
        new PolicyConditionsDto().cancellationRules(List.of(rule1, rule6));

    when(policyConditionsRepository.findById(1L)).thenReturn(Optional.of(existingConditions));
    when(policyConditionsRepository.save(any(PolicyConditions.class)))
        .thenReturn(existingConditions);
    when(policyConditionsMapper.toDto(existingConditions)).thenReturn(new PolicyConditionsDto());


    doAnswer(
            invocation -> {
              PolicyConditionsDto dtoArg = invocation.getArgument(0);
              PolicyConditions entityArg = invocation.getArgument(1);
              // Simulate the mapper copying the list from DTO to entity
              List<CancellationPenaltyRule> rules =
                  dtoArg.getCancellationRules().stream()
                      .map(
                          r ->
                              new CancellationPenaltyRule(
                                  null, r.getMonthsRemainingThreshold(), r.getPenaltyPercentage()))
                      .toList();
              entityArg.setCancellationRules(new ArrayList<>(rules));
              return null; // for void method
            })
        .when(policyConditionsMapper)
        .updateEntityFromDto(eq(updateDto), eq(existingConditions));

    // When
    policyConditionsService.updatePolicyConditions(updateDto);

    // Then: Verify that the entity passed to save() has its rules sorted
    ArgumentCaptor<PolicyConditions> captor = ArgumentCaptor.forClass(PolicyConditions.class);
    verify(policyConditionsRepository).save(captor.capture());

    PolicyConditions savedConditions = captor.getValue();
    assertThat(savedConditions.getCancellationRules()).hasSize(2);
    // The rule with threshold 6 should now be the first element due to the service's sorting logic
    assertThat(savedConditions.getCancellationRules().get(0).getMonthsRemainingThreshold())
        .isEqualTo(6);
    assertThat(savedConditions.getCancellationRules().get(1).getMonthsRemainingThreshold())
        .isEqualTo(1);
  }
}
