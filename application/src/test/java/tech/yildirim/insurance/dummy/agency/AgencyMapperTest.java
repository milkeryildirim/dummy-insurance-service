package tech.yildirim.insurance.dummy.agency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.AddressDto;
import tech.yildirim.insurance.api.generated.model.AgencyDto;
import tech.yildirim.insurance.dummy.common.Address;

@DisplayName("Agency Mapper Unit Tests")
class AgencyMapperTest {

  private AgencyMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AgencyMapper.class);
  }

  @Test
  @DisplayName("Should correctly map Entity to DTO including nested Address")
  void shouldMapEntityToDto() {
    // Given
    Address address = new Address("Agency St. 1", "12345", "Berlin", "Germany");
    Agency agency = new Agency();
    agency.setId(1L);
    agency.setAgencyCode("AG-001");
    agency.setName("Test Agency");
    agency.setAddress(address);

    // When
    AgencyDto dto = mapper.toDto(agency);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getAgencyCode()).isEqualTo("AG-001");
    assertThat(dto.getAddress()).isNotNull();
    assertThat(dto.getAddress().getCity()).isEqualTo("Berlin");
  }

  @Test
  @DisplayName("Should correctly map DTO to Entity including nested AddressDto")
  void shouldMapDtoToEntity() {
    // Given
    AddressDto addressDto = new AddressDto().streetAndHouseNumber("DTO St. 2").city("Munich");
    AgencyDto dto = new AgencyDto().agencyCode("AG-002").name("DTO Agency").address(addressDto);

    // When
    Agency entity = mapper.toEntity(dto);

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getAgencyCode()).isEqualTo("AG-002");
    assertThat(entity.getAddress()).isNotNull();
    assertThat(entity.getAddress().getCity()).isEqualTo("Munich");
    assertThat(entity.getId()).isNull();
    assertThat(entity.getPolicies()).isEmpty();
  }
}
