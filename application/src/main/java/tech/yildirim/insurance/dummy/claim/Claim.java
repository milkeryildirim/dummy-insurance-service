package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.policy.Policy;

@Entity
@Table(name = "claims")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "claim_type", discriminatorType = DiscriminatorType.STRING)
@Data
public abstract class Claim {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String claimNumber;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private LocalDate dateOfIncident;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClaimStatus status;

  @Column(precision = 12, scale = 2)
  private BigDecimal estimatedAmount;

  @Column(precision = 12, scale = 2)
  private BigDecimal paidAmount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id", nullable = false)
  private Policy policy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_adjuster_id")
  private Employee assignedAdjuster;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private ZonedDateTime dateReported;
}
