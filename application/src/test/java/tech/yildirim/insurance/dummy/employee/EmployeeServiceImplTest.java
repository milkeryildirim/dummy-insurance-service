package tech.yildirim.insurance.dummy.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;
import tech.yildirim.insurance.dummy.policy.PolicyType;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Unit Tests")
class EmployeeServiceImplTest {

  @Mock private EmployeeRepository employeeRepository;
  @Mock private EmployeeMapper employeeMapper;

  @InjectMocks private EmployeeServiceImpl employeeService;

  @Test
  @DisplayName("Should create employee successfully when employeeId and email are unique")
  void createEmployee_whenDataIsUnique_shouldSucceed() {
    // Given: A DTO for a new employee
    EmployeeDto inputDto = new EmployeeDto().employeeId("EMP-001").email("new@insurance.com");
    Employee entityToSave = new Employee();
    Employee savedEntity = new Employee();
    savedEntity.setId(1L);
    EmployeeDto outputDto = new EmployeeDto();
    outputDto.setId(1L);

    when(employeeRepository.findByEmployeeId(inputDto.getEmployeeId()))
        .thenReturn(Optional.empty());
    when(employeeRepository.findByEmail(inputDto.getEmail())).thenReturn(Optional.empty());
    when(employeeMapper.toEntity(inputDto)).thenReturn(entityToSave);
    when(employeeRepository.save(entityToSave)).thenReturn(savedEntity);
    when(employeeMapper.toDto(savedEntity)).thenReturn(outputDto);

    // When
    EmployeeDto result = employeeService.createEmployee(inputDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(employeeRepository).save(entityToSave);
  }

  @Test
  @DisplayName("Should throw DataIntegrityViolationException when Employee ID already exists")
  void createEmployee_whenEmployeeIdExists_shouldThrowException() {
    // Given: A DTO with a duplicate employeeId
    EmployeeDto inputDto = new EmployeeDto().employeeId("EMP-001").email("new@insurance.com");

    when(employeeRepository.findByEmployeeId(inputDto.getEmployeeId()))
        .thenReturn(Optional.of(new Employee()));

    // When & Then: Assert that the correct exception is thrown
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class, () -> employeeService.createEmployee(inputDto));

    assertThat(exception.getMessage()).contains("already exists");
    verify(employeeRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw DataIntegrityViolationException when Email already exists")
  void createEmployee_whenEmailExists_shouldThrowException() {
    // Given: A DTO with a duplicate email
    EmployeeDto inputDto = new EmployeeDto().employeeId("EMP-001").email("existing@insurance.com");

    when(employeeRepository.findByEmployeeId(inputDto.getEmployeeId()))
        .thenReturn(Optional.empty());
    when(employeeRepository.findByEmail(inputDto.getEmail()))
        .thenReturn(Optional.of(new Employee()));

    // When & Then: Assert that the correct exception is thrown
    assertThrows(
        DataIntegrityViolationException.class, () -> employeeService.createEmployee(inputDto));

    verify(employeeRepository, never()).save(any());
  }

  @Test
  @DisplayName(
      "Should return available adjusters for AUTO specialization and EXTERNAL employment type")
  void findAvailableAdjustersBySpecialization_whenValidCriteria_shouldReturnAdjusters() {
    // Given: Mock entities and DTOs
    Employee adjuster1 = new Employee();
    adjuster1.setId(1L);
    adjuster1.setEmployeeId("EXT-ADJ-AUTO-001");
    adjuster1.setRole(EmployeeRole.CLAIMS_ADJUSTER);
    adjuster1.setEmploymentType(EmploymentType.EXTERNAL);
    adjuster1.setSpecializationArea(PolicyType.AUTO);
    adjuster1.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

    Employee adjuster2 = new Employee();
    adjuster2.setId(2L);
    adjuster2.setEmployeeId("EXT-ADJ-AUTO-002");
    adjuster2.setRole(EmployeeRole.CLAIMS_ADJUSTER);
    adjuster2.setEmploymentType(EmploymentType.EXTERNAL);
    adjuster2.setSpecializationArea(PolicyType.AUTO);
    adjuster2.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

    List<Employee> mockAdjusters = Arrays.asList(adjuster1, adjuster2);

    EmployeeDto dto1 = new EmployeeDto();
    dto1.setId(1L);
    dto1.setEmployeeId("EXT-ADJ-AUTO-001");

    EmployeeDto dto2 = new EmployeeDto();
    dto2.setId(2L);
    dto2.setEmployeeId("EXT-ADJ-AUTO-002");

    List<EmployeeDto> expectedDtos = Arrays.asList(dto1, dto2);

    when(employeeRepository.findByRoleAndEmploymentTypeAndSpecializationAreaAndAvailabilityStatus(
            EmployeeRole.CLAIMS_ADJUSTER,
            EmploymentType.EXTERNAL,
            PolicyType.AUTO,
            AvailabilityStatus.AVAILABLE))
        .thenReturn(mockAdjusters);

    when(employeeMapper.toDtoList(mockAdjusters)).thenReturn(expectedDtos);

    // When
    List<EmployeeDto> result =
        employeeService.findAvailableAdjustersBySpecialization(
            PolicyType.AUTO, EmploymentType.EXTERNAL);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(0).getEmployeeId()).isEqualTo("EXT-ADJ-AUTO-001");
    assertThat(result.get(1).getId()).isEqualTo(2L);
    assertThat(result.get(1).getEmployeeId()).isEqualTo("EXT-ADJ-AUTO-002");

    verify(employeeRepository)
        .findByRoleAndEmploymentTypeAndSpecializationAreaAndAvailabilityStatus(
            EmployeeRole.CLAIMS_ADJUSTER,
            EmploymentType.EXTERNAL,
            PolicyType.AUTO,
            AvailabilityStatus.AVAILABLE);
    verify(employeeMapper).toDtoList(mockAdjusters);
  }

  @Test
  @DisplayName("Should return empty list when no available adjusters found for HOME specialization")
  void findAvailableAdjustersBySpecialization_whenNoAdjustersFound_shouldReturnEmptyList() {
    // Given: Empty list from repository
    List<Employee> emptyList = List.of();
    List<EmployeeDto> emptyDtoList = List.of();

    when(employeeRepository.findByRoleAndEmploymentTypeAndSpecializationAreaAndAvailabilityStatus(
            EmployeeRole.CLAIMS_ADJUSTER,
            EmploymentType.INTERNAL,
            PolicyType.HOME,
            AvailabilityStatus.AVAILABLE))
        .thenReturn(emptyList);

    when(employeeMapper.toDtoList(emptyList)).thenReturn(emptyDtoList);

    // When
    List<EmployeeDto> result =
        employeeService.findAvailableAdjustersBySpecialization(
            PolicyType.HOME, EmploymentType.INTERNAL);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    verify(employeeRepository)
        .findByRoleAndEmploymentTypeAndSpecializationAreaAndAvailabilityStatus(
            EmployeeRole.CLAIMS_ADJUSTER,
            EmploymentType.INTERNAL,
            PolicyType.HOME,
            AvailabilityStatus.AVAILABLE);
    verify(employeeMapper).toDtoList(emptyList);
  }
}
