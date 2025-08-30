package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue(HealthClaim.CLAIM_TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class HealthClaim extends Claim {

  public static final String CLAIM_TYPE = "HEALTH";

  private String medicalProvider;
  private String procedureCode;
}
