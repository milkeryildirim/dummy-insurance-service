package tech.yildirim.insurance.dummy.policy.condition;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

/** Mapper for the {@link PolicyConditions} entity and its DTO {@link PolicyConditionsDto}. */
@Mapper(componentModel = "spring")
public interface PolicyConditionsMapper {

  /**
   * Maps a PolicyConditions entity to its DTO representation.
   *
   * @param conditions The source entity.
   * @return The target DTO.
   */
  PolicyConditionsDto toDto(PolicyConditions conditions);

  /**
   * Updates an existing PolicyConditions entity from a DTO. The ID is ignored as we always update
   * the same record.
   *
   * @param dto The source DTO with new data.
   * @param conditions The target entity to be updated.
   */
  @Mapping(target = "id", ignore = true)
  void updateEntityFromDto(PolicyConditionsDto dto, @MappingTarget PolicyConditions conditions);
}
