package tech.yildirim.insurance.dummy.policy.condition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cancellation_penalty_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPenaltyRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer monthsRemainingThreshold;

  @Column(nullable = false, precision = 5, scale = 4)
  private BigDecimal penaltyPercentage;
}
