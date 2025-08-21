package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

/** Implementation of the {@link EmployeeService} interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeMapper employeeMapper;

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeDto> findAllEmployees() {
    log.info("Request to find all employees");
    List<Employee> employees = employeeRepository.findAll();
    log.info("Found {} employees", employees.size());
    return employeeMapper.toDtoList(employees);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<EmployeeDto> findEmployeeById(Long id) {
    log.info("Request to find employee with id: {}", id);
    return employeeRepository.findById(id).map(employeeMapper::toDto);
  }

  @Override
  @Transactional
  public EmployeeDto createEmployee(EmployeeDto employeeDto) {
    log.info(
        "Request to create employee with employeeId: {} and email: {}",
        employeeDto.getEmployeeId(),
        employeeDto.getEmail());
    employeeRepository
        .findByEmployeeId(employeeDto.getEmployeeId())
        .ifPresent(
            e -> {
              log.error(
                  "Employee with employeeId '{}' already exists.", employeeDto.getEmployeeId());
              throw new DataIntegrityViolationException(
                  "Employee already exists with id: " + e.getId());
            });

    employeeRepository
        .findByEmail(employeeDto.getEmail())
        .ifPresent(
            e -> {
              log.error("Employee with email '{}' already exists.", employeeDto.getEmail());
              throw new DataIntegrityViolationException(
                  "Employee already exists with email: " + e.getEmail());
            });

    Employee employeeToSave = employeeMapper.toEntity(employeeDto);
    Employee savedEmployee = employeeRepository.save(employeeToSave);
    log.info("Successfully created employee with id {}", savedEmployee.getId());
    return employeeMapper.toDto(savedEmployee);
  }
}
