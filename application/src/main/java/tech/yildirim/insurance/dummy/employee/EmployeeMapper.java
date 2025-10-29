package tech.yildirim.insurance.dummy.employee;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;

/** Mapper for the entity {@link Employee} and its DTO {@link EmployeeDto}. */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  /**
   * Maps an Employee entity to an EmployeeDto. MapStruct will automatically handle enum conversions
   * between entity and DTO.
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

  /**
   * Updates an existing Employee entity from an EmployeeDto, ignoring database-managed fields. This
   * is useful for update operations where you want to preserve the ID and timestamps.
   *
   * @param employeeDto The source DTO with updated information.
   * @param employee The target entity to update.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEmployeeFromDto(EmployeeDto employeeDto, @MappingTarget Employee employee);

  /**
   * Converts ZonedDateTime to OffsetDateTime.
   *
   * @param zonedDateTime The source ZonedDateTime.
   * @return The converted OffsetDateTime.
   */
  default OffsetDateTime map(ZonedDateTime zonedDateTime) {
    return zonedDateTime != null ? zonedDateTime.toOffsetDateTime() : null;
  }

  /**
   * Converts OffsetDateTime to ZonedDateTime.
   *
   * @param offsetDateTime The source OffsetDateTime.
   * @return The converted ZonedDateTime.
   */
  default ZonedDateTime map(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toZonedDateTime() : null;
  }
}
