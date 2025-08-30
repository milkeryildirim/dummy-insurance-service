package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

  ClaimMapper INSTANCE = Mappers.getMapper(ClaimMapper.class);

  // ========== AutoClaim Mappings ==========

  /**
   * Maps an {@link AutoClaim} entity to {@link AutoClaimDto}.
   *
   * @param autoClaim The source AutoClaim entity.
   * @return The target AutoClaimDto.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.id", target = "assignedAdjusterId")
  @Mapping(
      target = "assignedAdjusterName",
      expression =
          "java(autoClaim.getAssignedAdjuster() != null ? autoClaim.getAssignedAdjuster().getFirstName() + \" \" + autoClaim.getAssignedAdjuster().getLastName() : null)")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  AutoClaimDto toDto(AutoClaim autoClaim);

  /** Maps a list of AutoClaim entities to a list of AutoClaimDtos. */
  List<AutoClaimDto> toAutoClaimDtoList(List<AutoClaim> autoClaims);

  /**
   * Populates an {@link AutoClaim} entity with data from an {@link AutoClaimDto}.
   *
   * @param dto The source AutoClaimDto.
   * @param entity The target AutoClaim entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  @Mapping(target = "assignedAdjuster", ignore = true)
  void populateAutoClaimFromDto(AutoClaimDto dto, @MappingTarget AutoClaim entity);

  // ========== HomeClaim Mappings ==========

  /**
   * Maps a {@link HomeClaim} entity to {@link HomeClaimDto}.
   *
   * @param homeClaim The source HomeClaim entity.
   * @return The target HomeClaimDto.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.id", target = "assignedAdjusterId")
  @Mapping(
      target = "assignedAdjusterName",
      expression =
          "java(homeClaim.getAssignedAdjuster() != null ? homeClaim.getAssignedAdjuster().getFirstName() + \" \" + homeClaim.getAssignedAdjuster().getLastName() : null)")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  HomeClaimDto toDto(HomeClaim homeClaim);

  /** Maps a list of HomeClaim entities to a list of HomeClaimDtos. */
  List<HomeClaimDto> toHomeClaimDtoList(List<HomeClaim> homeClaims);

  /**
   * Populates a {@link HomeClaim} entity with data from a {@link HomeClaimDto}.
   *
   * @param dto The source HomeClaimDto.
   * @param entity The target HomeClaim entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  @Mapping(target = "assignedAdjuster", ignore = true)
  void populateHomeClaimFromDto(HomeClaimDto dto, @MappingTarget HomeClaim entity);

  // ========== HealthClaim Mappings ==========

  /**
   * Maps a {@link HealthClaim} entity to {@link HealthClaimDto}.
   *
   * @param healthClaim The source HealthClaim entity.
   * @return The target HealthClaimDto.
   */
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "assignedAdjuster.id", target = "assignedAdjusterId")
  @Mapping(
      target = "assignedAdjusterName",
      expression =
          "java(healthClaim.getAssignedAdjuster() != null ? healthClaim.getAssignedAdjuster().getFirstName() + \" \" + healthClaim.getAssignedAdjuster().getLastName() : null)")
  @Mapping(source = "assignedAdjuster.phoneNumber", target = "assignedAdjusterContact")
  HealthClaimDto toDto(HealthClaim healthClaim);

  /** Maps a list of HealthClaim entities to a list of HealthClaimDtos. */
  List<HealthClaimDto> toHealthClaimDtoList(List<HealthClaim> healthClaims);

  /**
   * Populates a {@link HealthClaim} entity with data from a {@link HealthClaimDto}.
   *
   * @param dto The source HealthClaimDto.
   * @param entity The target HealthClaim entity to be populated.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claimNumber", ignore = true)
  @Mapping(target = "policy", ignore = true)
  @Mapping(target = "dateReported", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "paidAmount", ignore = true)
  @Mapping(target = "assignedAdjuster", ignore = true)
  void populateHealthClaimFromDto(HealthClaimDto dto, @MappingTarget HealthClaim entity);
}

