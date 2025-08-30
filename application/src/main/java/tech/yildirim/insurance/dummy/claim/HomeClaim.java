package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue(HomeClaim.CLAIM_TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class HomeClaim extends Claim {

  public static final String CLAIM_TYPE = "HOME";

  private String typeOfDamage;

  @Lob // For potentially long text
  private String damagedItems;
}
