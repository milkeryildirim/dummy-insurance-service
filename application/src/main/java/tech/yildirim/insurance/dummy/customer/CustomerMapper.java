package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.CustomerDto;

/**
 * Mapper for the entity {@link Customer} and its DTO {@link CustomerDto}. MapStruct will generate
 * the implementation of this interface at compile time.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

  CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

  /**
   * Maps a Customer entity to a CustomerDto.
   *
   * @param customer the entity to be mapped.
   * @return the corresponding DTO.
   */
  @Mapping(target = "password", ignore = true)
  CustomerDto toDto(Customer customer);

  /**
   * Maps a CustomerDto to a Customer entity. The 'id' field is ignored because it's
   * database-generated. The 'creationDate' and 'lastUpdateDate' are also ignored as they are
   * managed by the database.
   *
   * @param customerDto the DTO to be mapped.
   * @return the corresponding entity.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Customer toEntity(CustomerDto customerDto);

  /**
   * Maps a list of Customer entities to a list of CustomerDto's.
   *
   * @param customers the list of entities.
   * @return the corresponding list of DTOs.
   */
  List<CustomerDto> toDtoList(List<Customer> customers);

  /**
   * Updates an existing Customer entity from a CustomerDto.
   * It maps fields from the DTO to the existing entity instance.
   *
   * @param dto the source DTO with new data.
   * @param entity the target entity to be updated (annotated with @MappingTarget).
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateCustomerFromDto(CustomerDto dto, @MappingTarget Customer entity);
}
