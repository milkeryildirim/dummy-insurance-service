package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.ClaimDto;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

  ClaimMapper INSTANCE = Mappers.getMapper(ClaimMapper.class);

  /**
   * Maps a Claim entity (including its subclasses) to the unified ClaimDto. MapStruct is smart
   * enough to find the fields in the subclasses (e.g., licensePlate).
   *
   * @param claim The source entity (can be AutoClaim, HomeClaim, etc.).
   * @return The target DTO.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  ClaimDto toDto(Claim claim);

  /**
   * Maps a specific {@link AutoClaim} entity to the unified {@link ClaimDto}.
   * @param autoClaim The source AutoClaim entity.
   * @return The target DTO, populated with common and auto-specific fields.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  ClaimDto toDto(AutoClaim autoClaim);

  /**
   * Maps a specific {@link HomeClaim} entity to the unified {@link ClaimDto}.
   * @param homeClaim The source HomeClaim entity.
   * @return The target DTO, populated with common and home-specific fields.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  ClaimDto toDto(HomeClaim homeClaim);

  /**
   * Maps a specific {@link HealthClaim} entity to the unified {@link ClaimDto}.
   * @param healthClaim The source HealthClaim entity.
   * @return The target DTO, populated with common and health-specific fields.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  ClaimDto toDto(HealthClaim healthClaim);

  /**
   * Maps a list of Claim entities to a list of ClaimDtos.
   *
   * @param claims The list of entities.
   * @return The list of DTOs.
   */
  List<ClaimDto> toDtoList(List<Claim> claims);

  /**
   * Populates a Claim entity with data from a ClaimDto. This method is used by the service layer
   * after it has instantiated the correct concrete Claim subclass (e.g., new AutoClaim()).
   *
   * @param dto The source DTO.
   * @param entity The target entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  void populateClaimFromDto(ClaimDto dto, @MappingTarget Claim entity);

  /**
   * Populates a specific {@link AutoClaim} entity with data from a ClaimDto.
   * @param dto The source DTO.
   * @param entity The target AutoClaim entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  void populateClaimFromDto(ClaimDto dto, @MappingTarget AutoClaim entity);

  /**
   * Populates a specific {@link HomeClaim} entity with data from a ClaimDto.
   * @param dto The source DTO.
   * @param entity The target HomeClaim entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  void populateClaimFromDto(ClaimDto dto, @MappingTarget HomeClaim entity);

  /**
   * Populates a specific {@link HealthClaim} entity with data from a ClaimDto.
   * @param dto The source DTO.
   * @param entity The target HealthClaim entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  void populateClaimFromDto(ClaimDto dto, @MappingTarget HealthClaim entity);
}
