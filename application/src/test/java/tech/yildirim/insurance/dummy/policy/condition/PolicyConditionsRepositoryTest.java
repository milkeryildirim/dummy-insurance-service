package tech.yildirim.insurance.dummy.policy.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@DisplayName("Policy Conditions Repository Integration Tests")
class PolicyConditionsRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private PolicyConditionsRepository repository;

  @Test
  @DisplayName("Should save and retrieve policy conditions with nested rules")
  void shouldSaveAndRetrieveConditions() {
    // Given: A new set of conditions with cancellation rules
    CancellationPenaltyRule rule1 = new CancellationPenaltyRule(null, 6, new BigDecimal("0.20"));
    CancellationPenaltyRule rule2 = new CancellationPenaltyRule(null, 1, new BigDecimal("0.05"));
    PolicyConditions conditions =
        new PolicyConditions(1L, 14, new BigDecimal("0.05"), List.of(rule1, rule2));

    // When: Saving the conditions
    repository.save(conditions);
    entityManager.flush();
    entityManager.clear();

    // Then: The conditions can be found by ID and the nested rules are present
    Optional<PolicyConditions> foundOpt = repository.findById(1L);

    assertThat(foundOpt).isPresent();
    PolicyConditions found = foundOpt.get();
    assertThat(found.getFreeCancellationDays()).isEqualTo(14);
    assertThat(found.getCancellationRules()).hasSize(2);
    assertThat(found.getCancellationRules().getFirst().getMonthsRemainingThreshold()).isEqualTo(6);
  }
}
