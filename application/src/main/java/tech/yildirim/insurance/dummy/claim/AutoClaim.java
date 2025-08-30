package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue(AutoClaim.CLAIM_TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class AutoClaim extends Claim {

  public static final String CLAIM_TYPE = "AUTO";

  @NotBlank(message = "License plate cannot be blank for an auto claim.")
  private String licensePlate;

  private String vehicleVin;
  private String accidentLocation;
}
