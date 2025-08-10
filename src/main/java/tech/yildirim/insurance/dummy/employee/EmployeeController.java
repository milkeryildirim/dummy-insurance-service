package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class EmployeeController implements EmployeesApi {

  private final EmployeeService employeeService;

  @Override
  public ResponseEntity<EmployeeDto> createEmployee(EmployeeDto employeeDto) {
    return new ResponseEntity<>(employeeService.createEmployee(employeeDto), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
    return ResponseEntity.ok(employeeService.findAllEmployees());
  }

  @Override
  public ResponseEntity<EmployeeDto> getEmployeeById(Long id) {
    return employeeService
        .findEmployeeById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
