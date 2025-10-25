package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** Represents an invoice/receipt uploaded by the customer for a claim. */
@Entity
@Table(name = "customer_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInvoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "claim_id", nullable = false)
  private Claim claim;

  @Column(nullable = false, length = 500)
  private String description;

  @Column(precision = 12, scale = 2)
  private BigDecimal invoiceAmount;

  @Column private LocalDate invoiceDate;

  @Column private String vendorName;

  @Column(name = "invoice_pdf_path", length = 500)
  private String invoicePdfPath; // Path to the uploaded PDF file

  @Column(name = "original_pdf_filename")
  private String originalPdfFilename; // Original filename when uploaded

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private ZonedDateTime uploadedAt;
}
