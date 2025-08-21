package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.EmployeesApi;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

/**
 * REST Controller for managing employees. Implements the generated {@link EmployeesApi} interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements EmployeesApi {

  private final EmployeeService employeeService;

  @Override
  public ResponseEntity<EmployeeDto> createEmployee(EmployeeDto employeeDto) {
    log.info("REST request to create employee with email: {}", employeeDto.getEmail());
    EmployeeDto createdEmployee = employeeService.createEmployee(employeeDto);
    log.info("Successfully created employee with id {}", createdEmployee.getId());
    return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
    log.info("REST request to get all employees");
    List<EmployeeDto> employees = employeeService.findAllEmployees();
    log.debug("Returning {} employees", employees.size());
    return ResponseEntity.ok(employees);
  }

  @Override
  public ResponseEntity<EmployeeDto> getEmployeeById(Long id) {
    log.info("REST request to get employee by id: {}", id);
    return employeeService
        .findEmployeeById(id)
        .map(
            employee -> {
              log.info("Found employee with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok(employee);
            })
        .orElseGet(
            () -> {
              log.warn("Employee with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }
}
