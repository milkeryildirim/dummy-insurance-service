package tech.yildirim.insurance.dummy.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

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
            DataIntegrityViolationException.class,
            () -> {
              employeeService.createEmployee(inputDto);
            });

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
        DataIntegrityViolationException.class,
        () -> {
          employeeService.createEmployee(inputDto);
        });

    verify(employeeRepository, never()).save(any());
  }
}
