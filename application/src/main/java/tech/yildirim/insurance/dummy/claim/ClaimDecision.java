package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.yildirim.insurance.dummy.employee.Employee;

/** Represents the final decision made on a claim by an insurance employee. */
@Entity
@Table(name = "claim_decisions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDecision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "claim_id", nullable = false, unique = true)
  private Claim claim;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "decision_maker_id", nullable = false)
  private Employee decisionMaker;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DecisionType decisionType;

  @Column(precision = 12, scale = 2)
  private BigDecimal approvedAmount;

  @Column(nullable = false, length = 2000)
  private String reasoning; // Why was this decision made

  @Column(length = 1000)
  private String rejectionReason; // Specific reason for rejection (if rejected)

  @Column(length = 1000)
  private String additionalNotes;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private ZonedDateTime decisionDate;

  @UpdateTimestamp
  @Column(nullable = false)
  private ZonedDateTime updatedAt;
}
