package tech.yildirim.insurance.dummy.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

@DisplayName("Employee Mapper Unit Tests")
class EmployeeMapperTest {

  private EmployeeMapper employeeMapper;

  @BeforeEach
  void setUp() {
    employeeMapper = Mappers.getMapper(EmployeeMapper.class);
  }

  @Test
  @DisplayName("Should correctly map Entity to DTO")
  void shouldMapEntityToDto() {
    // Given
    Employee entity = new Employee();
    entity.setId(1L);
    entity.setEmployeeId("EMP-1001");
    entity.setFirstName("John");
    entity.setLastName("Doe");
    entity.setEmail("john.doe@insurance.com");
    entity.setRole(EmployeeRole.MANAGER);

    // When
    EmployeeDto dto = employeeMapper.toDto(entity);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(entity.getId());
    assertThat(dto.getEmployeeId()).isEqualTo(entity.getEmployeeId());
    assertThat(dto.getFirstName()).isEqualTo(entity.getFirstName());
    assertThat(dto.getRole().toString()).isEqualTo(entity.getRole().toString());
  }

  @Test
  @DisplayName("Should correctly map DTO to Entity")
  void shouldMapDtoToEntity() {
    // Given
    EmployeeDto dto =
        new EmployeeDto()
            .employeeId("EMP-1002")
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@insurance.com")
            .role(EmployeeDto.RoleEnum.UNDERWRITER)
            .password("secret");

    // When
    Employee entity = employeeMapper.toEntity(dto);

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getEmployeeId()).isEqualTo(dto.getEmployeeId());
    assertThat(entity.getFirstName()).isEqualTo(dto.getFirstName());
    assertThat(entity.getRole()).isEqualTo(EmployeeRole.UNDERWRITER);
    // Ignored fields should be null
    assertThat(entity.getId()).isNull();
    assertThat(entity.getCreatedAt()).isNull();
  }
}
