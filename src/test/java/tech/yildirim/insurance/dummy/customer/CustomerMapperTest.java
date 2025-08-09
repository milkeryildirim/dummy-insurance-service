package tech.yildirim.insurance.dummy.customer;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.yildirim.insurance.api.generated.model.AddressDto;
import tech.yildirim.insurance.api.generated.model.CustomerDto;

@DisplayName("Customer Mapper Unit Tests")
class CustomerMapperTest {

  private final CustomerMapper customerMapper = CustomerMapper.INSTANCE;

  @Test
  @DisplayName("Should correctly map Entity to DTO")
  void shouldMapEntityToDto() {
    // Given: An entity object
    Address addressEntity = new Address("Musterstraße 123", "63500", "Musterstadt", "Germany");
    Customer customerEntity =
        new Customer(
            1L,
            "John",
            "Doe",
            LocalDate.of(1980, 1, 1),
            addressEntity,
            "password123",
            "jonh.doe@example.com",
            ZonedDateTime.now(),
            ZonedDateTime.now());

    CustomerDto customerDto = customerMapper.toDto(customerEntity);

    assertNotNull(customerDto);
    assertEquals(customerEntity.getId(), customerDto.getId());
    assertEquals(customerEntity.getFirstName(), customerDto.getFirstName());
    assertEquals(customerEntity.getLastName(), customerDto.getLastName());
    assertEquals(customerEntity.getDateOfBirth(), customerDto.getDateOfBirth());
    assertNotNull(customerDto.getAddress());
    assertEquals(customerEntity.getAddress().getCity(), customerDto.getAddress().getCity());
  }

  @Test
  @DisplayName("Should correctly map DTO to Entity")
  void shouldMapDtoToEntity() {
    // Given: A DTO object
    AddressDto addressDto =
        new AddressDto()
            .streetAndHouseNumber("Musterstraße 123")
            .postalCode("65000")
            .city("Musterstadt")
            .country("Germany");

    CustomerDto customerDto =
        new CustomerDto()
            .firstName("Jane")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1992, 2, 2))
            .address(addressDto)
            .password("new-password");

    Customer customerEntity = customerMapper.toEntity(customerDto);

    assertNotNull(customerEntity);
    assertEquals(customerDto.getFirstName(), customerEntity.getFirstName());
    assertEquals(customerDto.getLastName(), customerEntity.getLastName());
    assertNotNull(customerEntity.getAddress());
    assertEquals(customerDto.getAddress().getCity(), customerEntity.getAddress().getCity());

    // Important: Check that ignored fields are null
    assertNull(customerEntity.getId());
    assertNull(customerEntity.getCreatedAt());
    assertNull(customerEntity.getUpdatedAt());
  }

  @Test
  @DisplayName("Should correctly update an existing Entity from a DTO")
  void shouldUpdateEntityFromDto() {
    // Given: An existing entity and a DTO with new data
    Address existingAddress =
        new Address("Alte Musterstraße 1", "66000", "Alte Musterstadt", "Old Country");
    Customer existingCustomer =
        new Customer(
            1L,
            "John",
            "Smith",
            LocalDate.of(1980, 1, 1),
            existingAddress,
            "old-hashed-password",
            "john.doe@example.com",
            ZonedDateTime.now().minusDays(10),
            ZonedDateTime.now().minusDays(5));

    AddressDto newAddressDto =
        new AddressDto()
            .city("New Berlin")
            .country("New Germany")
            .streetAndHouseNumber("New St 1")
            .postalCode("11111");
    CustomerDto updateDto =
        new CustomerDto()
            .firstName("Johnny")
            .lastName("Smith")
            .dateOfBirth(LocalDate.of(1981, 2, 3))
            .address(newAddressDto);

    customerMapper.updateCustomerFromDto(updateDto, existingCustomer);

    assertEquals(1L, existingCustomer.getId());
    assertEquals("Johnny", existingCustomer.getFirstName());
    assertEquals("Smith", existingCustomer.getLastName());
    assertEquals("New Berlin", existingCustomer.getAddress().getCity());

    assertNotNull(existingCustomer.getCreatedAt());
    assertNotEquals(updateDto.getDateOfBirth(), existingCustomer.getCreatedAt().toLocalDate());
  }
}
