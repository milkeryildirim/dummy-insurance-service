package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.PolicyDto;

/** Mapper for the entity {@link Policy} and its DTO {@link PolicyDto}. */
@Mapper(componentModel = "spring")
public interface PolicyMapper {

  PolicyMapper INSTANCE = Mappers.getMapper(PolicyMapper.class);

  /**
   * Maps a Policy entity to a PolicyDto. The customer's ID is explicitly mapped from the nested
   * Customer object.
   *
   * @param policy The source entity.
   * @return The target DTO.
   */
  @Mapping(source = "customer.id", target = "customerId")
  @Mapping(source = "agency.id", target = "agencyId")
  PolicyDto toDto(Policy policy);

  /**
   * Maps a PolicyDto to a Policy entity. The customerId from the DTO will be used to fetch the full
   * Customer entity in the service layer, so we ignore it during this initial mapping.
   *
   * @param policyDto The source DTO.
   * @return The target entity.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "policyNumber", ignore = true)
  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "agency", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Policy toEntity(PolicyDto policyDto);

  /**
   * Maps a list of Policy entities to a list of PolicyDtos.
   *
   * @param policies The list of entities.
   * @return The list of DTOs.
   */
  List<PolicyDto> toDtoList(List<Policy> policies);

  /**
   * Updates an existing Policy entity from a PolicyDto, ignoring relationship and managed fields.
   *
   * @param dto The source DTO.
   * @param entity The target entity to update.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "policyNumber", ignore = true)
  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updatePolicyFromDto(PolicyDto dto, @MappingTarget Policy entity);
}
