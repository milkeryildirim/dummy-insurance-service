package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("HOME")
@Data
@EqualsAndHashCode(callSuper = true)
public class HomeClaim extends Claim {

  private String typeOfDamage;

  @Lob // For potentially long text
  private String damagedItems;
}
