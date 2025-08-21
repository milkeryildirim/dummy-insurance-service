package tech.yildirim.insurance.dummy.policy.condition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "policy_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyConditions {

  @Id private Long id;

  @Column(nullable = false)
  private Integer freeCancellationDays;

  @Column(nullable = false, precision = 5, scale = 4)
  private BigDecimal noClaimBonusPercentage;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "policy_conditions_id")
  @OrderBy("monthsRemainingThreshold DESC")
  private List<CancellationPenaltyRule> cancellationRules = new ArrayList<>();
}
