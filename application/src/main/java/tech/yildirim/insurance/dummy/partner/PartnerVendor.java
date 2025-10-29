package tech.yildirim.insurance.dummy.partner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing partner vendors contracted with the insurance company. Used for manual
 * validation of customer invoices against approved vendor lists.
 */
@Entity
@Table(name = "partner_vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerVendor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 100)
  @Column(name = "vendor_code", unique = true, nullable = false)
  private String vendorCode;

  @NotBlank
  @Size(max = 255)
  @Column(name = "company_name", nullable = false)
  private String companyName;

  @NotBlank
  @Size(max = 255)
  @Column(name = "contact_person", nullable = false)
  private String contactPerson;

  @Email
  @NotBlank
  @Size(max = 255)
  @Column(name = "email", nullable = false)
  private String email;

  @Size(max = 50)
  @Column(name = "phone_number")
  private String phoneNumber;

  @Size(max = 255)
  @Column(name = "address")
  private String address;

  @Size(max = 100)
  @Column(name = "city")
  private String city;

  @Size(max = 20)
  @Column(name = "postal_code")
  private String postalCode;

  @Size(max = 100)
  @Column(name = "country")
  private String country;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "vendor_type", nullable = false)
  private VendorType vendorType;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private VendorStatus status;

  @Size(max = 500)
  @Column(name = "specialization")
  private String specialization;

  @Size(max = 1000)
  @Column(name = "notes")
  private String notes;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private ZonedDateTime updatedAt;
}
