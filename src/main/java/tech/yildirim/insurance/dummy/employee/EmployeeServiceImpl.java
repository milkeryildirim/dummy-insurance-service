package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

/** Implementation of the {@link EmployeeService} interface. */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeMapper employeeMapper;

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeDto> findAllEmployees() {
    return employeeMapper.toDtoList(employeeRepository.findAll());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<EmployeeDto> findEmployeeById(Long id) {
    return employeeRepository.findById(id).map(employeeMapper::toDto);
  }

  @Override
  @Transactional
  public EmployeeDto createEmployee(EmployeeDto employeeDto) {
    employeeRepository
        .findByEmployeeId(employeeDto.getEmployeeId())
        .ifPresent(
            e -> {
              throw new DataIntegrityViolationException(
                  "Employee already exists with id: " + e.getId());
            });

    employeeRepository
        .findByEmail(employeeDto.getEmail())
        .ifPresent(
            e -> {
              throw new DataIntegrityViolationException(
                  "Employee already exists with email: " + e.getEmail());
            });

    return employeeMapper.toDto(employeeRepository.save(employeeMapper.toEntity(employeeDto)));
  }
}
