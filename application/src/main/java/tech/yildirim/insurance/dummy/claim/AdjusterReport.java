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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.yildirim.insurance.dummy.employee.Employee;

/** Represents an adjuster's technical assessment report for a claim. */
@Entity
@Table(name = "adjuster_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjusterReport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "claim_id", nullable = false)
  private Claim claim;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "adjuster_id", nullable = false)
  private Employee adjuster;

  @Column(nullable = false, length = 1000)
  private String summary;

  @Column(length = 2000)
  private String findings;

  @Column(length = 1000)
  private String recommendations;

  @Column(precision = 12, scale = 2)
  private BigDecimal recommendedAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportStatus status;

  @Column(name = "report_pdf_path", length = 500)
  private String reportPdfPath; // Path to the uploaded PDF file

  @Column(name = "original_pdf_filename")
  private String originalPdfFilename; // Original filename when uploaded

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private ZonedDateTime updatedAt;
}
