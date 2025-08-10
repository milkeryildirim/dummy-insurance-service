package tech.yildirim.insurance.dummy.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a reusable address component that can be embedded in other entities. This class is not
 * an entity itself but part of an entity.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

  private String streetAndHouseNumber;
  private String postalCode;
  private String city;
  private String country;

}
