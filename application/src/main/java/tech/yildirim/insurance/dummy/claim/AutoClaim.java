package tech.yildirim.insurance.dummy.claim;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("AUTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AutoClaim extends Claim {

  @NotBlank(message = "License plate cannot be blank for an auto claim.")
  private String licensePlate;

  private String vehicleVin;
  private String accidentLocation;
}
