package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import java.util.Optional;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

/** Service Interface for managing {@link Employee}. */
public interface EmployeeService {

  /**
   * Retrieves all employees.
   *
   * @return A list of all employee DTOs.
   */
  List<EmployeeDto> findAllEmployees();

  /**
   * Finds an employee by their ID.
   *
   * @param id The ID of the employee.
   * @return An Optional containing the found employee DTO, or empty if not found.
   */
  Optional<EmployeeDto> findEmployeeById(Long id);

  /**
   * Creates a new employee.
   *
   * @param employeeDto The DTO containing the data for the new employee.
   * @return The created employee DTO.
   */
  EmployeeDto createEmployee(EmployeeDto employeeDto);
}
