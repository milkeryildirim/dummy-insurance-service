package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.yildirim.insurance.dummy.policy.PolicyType;

/** Spring Data JPA repository for the {@link Employee} entity. */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  Optional<Employee> findByEmployeeId(String employeeId);

  Optional<Employee> findByEmail(String email);

  List<Employee> findByRoleAndEmploymentTypeAndSpecializationAreaAndAvailabilityStatus(
      EmployeeRole role,
      EmploymentType employmentType,
      PolicyType specializationArea,
      AvailabilityStatus availabilityStatus);
}
