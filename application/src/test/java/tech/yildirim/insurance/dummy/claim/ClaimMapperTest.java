package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.dummy.policy.Policy;

@DisplayName("Claim Mapper Unit Tests")
class ClaimMapperTest {

  private ClaimMapper claimMapper;
  private Policy testPolicy;

  @BeforeEach
  void setUp() {
    claimMapper = ClaimMapper.INSTANCE;
    testPolicy = new Policy();
    testPolicy.setId(201L);
  }

  @Test
  @DisplayName("Should correctly map an AutoClaim Entity to the flat DTO")
  void shouldMapAutoClaimEntityToDto() {
    // Given: An AutoClaim entity with specific fields
    AutoClaim autoClaim = new AutoClaim();
    autoClaim.setId(1L);
    autoClaim.setClaimNumber("CLM-AUTO-001");
    autoClaim.setDescription("Fender bender accident");
    autoClaim.setPolicy(testPolicy);
    autoClaim.setLicensePlate("B-MW-1234");
    autoClaim.setVehicleVin("VIN123456789");

    // When: Mapping to DTO
    ClaimDto claimDto = claimMapper.toDto(autoClaim);

    // Then: The DTO should contain all common and auto-specific fields
    assertThat(claimDto).isNotNull();
    assertThat(claimDto.getId()).isEqualTo(1L);
    assertThat(claimDto.getClaimNumber()).isEqualTo("CLM-AUTO-001");
    assertThat(claimDto.getDescription()).isEqualTo("Fender bender accident");
    assertThat(claimDto.getPolicyId()).isEqualTo(201L);
    assertThat(claimDto.getLicensePlate()).isEqualTo("B-MW-1234");
    assertThat(claimDto.getVehicleVin()).isEqualTo("VIN123456789");
    // And other type's fields should be null
    assertThat(claimDto.getTypeOfDamage()).isNull();
  }

  @Test
  @DisplayName("Should correctly map a HomeClaim Entity to the flat DTO")
  void shouldMapHomeClaimEntityToDto() {
    // Given: A HomeClaim entity with specific fields
    HomeClaim homeClaim = new HomeClaim();
    homeClaim.setId(2L);
    homeClaim.setPolicy(testPolicy);
    homeClaim.setTypeOfDamage("Water leak");
    homeClaim.setDamagedItems("Kitchen floor, basement walls");

    // When: Mapping to DTO
    ClaimDto claimDto = claimMapper.toDto(homeClaim);

    // Then: The DTO should contain all common and home-specific fields
    assertThat(claimDto).isNotNull();
    assertThat(claimDto.getId()).isEqualTo(2L);
    assertThat(claimDto.getPolicyId()).isEqualTo(201L);
    assertThat(claimDto.getTypeOfDamage()).isEqualTo("Water leak");
    assertThat(claimDto.getDamagedItems()).isEqualTo("Kitchen floor, basement walls");
    // And other type's fields should be null
    assertThat(claimDto.getLicensePlate()).isNull();
  }

  @Test
  @DisplayName("Should correctly populate an AutoClaim Entity from a DTO")
  void shouldPopulateAutoClaimFromDto() {
    // Given: An empty AutoClaim entity and a DTO with data
    AutoClaim emptyAutoClaim = new AutoClaim();
    ClaimDto dto = new ClaimDto()
        .description("New claim description")
        .dateOfIncident(LocalDate.now())
        .estimatedAmount(new BigDecimal("2500.00"))
        .licensePlate("B-AU-5678")
        .accidentLocation("A10 Highway");

    // When: Populating the entity from the DTO
    claimMapper.populateClaimFromDto(dto, emptyAutoClaim);

    // Then: The entity should be populated with the DTO's data
    assertThat(emptyAutoClaim.getDescription()).isEqualTo("New claim description");
    assertThat(emptyAutoClaim.getEstimatedAmount()).isEqualTo(new BigDecimal("2500.00"));
    assertThat(emptyAutoClaim.getLicensePlate()).isEqualTo("B-AU-5678");
    assertThat(emptyAutoClaim.getAccidentLocation()).isEqualTo("A10 Highway");

    // And: Ignored fields should remain null
    assertThat(emptyAutoClaim.getId()).isNull();
    assertThat(emptyAutoClaim.getClaimNumber()).isNull();
    assertThat(emptyAutoClaim.getPolicy()).isNull();
  }
}
