package tech.yildirim.insurance.dummy.employee;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

/** Mapper for the entity {@link Employee} and its DTO {@link EmployeeDto}. */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  /**
   * Maps an Employee entity to an EmployeeDto.
   *
   * @param employee The source entity.
   * @return The target DTO.
   */
  EmployeeDto toDto(Employee employee);

  /**
   * Maps a list of Employee entities to a list of EmployeeDtos.
   *
   * @param employees The list of source entities.
   * @return The list of target DTOs.
   */
  List<EmployeeDto> toDtoList(List<Employee> employees);

  /**
   * Maps an EmployeeDto to an Employee entity. Ignores database-managed fields like id, createdAt,
   * and updatedAt.
   *
   * @param employeeDto The source DTO.
   * @return The target entity.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Employee toEntity(EmployeeDto employeeDto);
}
