package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("HEALTH")
@Data
@EqualsAndHashCode(callSuper = true)
public class HealthClaim extends Claim {

  private String medicalProvider;
  private String procedureCode;
}
