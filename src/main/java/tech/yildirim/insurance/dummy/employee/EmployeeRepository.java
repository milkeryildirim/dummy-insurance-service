package tech.yildirim.insurance.dummy.employee;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for the {@link Employee} entity. */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  Optional<Employee> findByEmployeeId(String employeeId);

  Optional<Employee> findByEmail(String email);
}
