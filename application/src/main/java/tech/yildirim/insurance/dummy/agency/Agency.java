package tech.yildirim.insurance.dummy.agency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.yildirim.insurance.dummy.common.Address;
import tech.yildirim.insurance.dummy.policy.Policy;

/** Represents a third-party agency that sells policies for the insurance company. */
@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String agencyCode;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @Embedded private Address address;

  @NotBlank
  @Column(nullable = false)
  private String contactPerson;

  @Email
  @NotBlank
  @Column(nullable = false)
  private String contactEmail;

  private String contactPhone;

  @OneToMany(
      mappedBy = "agency",
      cascade = CascadeType.PERSIST,
      fetch = FetchType.LAZY
  )
  private List<Policy> policies = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private ZonedDateTime updatedAt;
}
