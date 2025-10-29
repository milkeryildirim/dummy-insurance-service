package tech.yildirim.insurance.dummy.claim;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tech.yildirim.insurance.api.generated.model.ClaimDecisionDto;

/** Mapper for the entity {@link ClaimDecision} and its DTO {@link ClaimDecisionDto}. */
@Mapper(componentModel = "spring")
public interface ClaimDecisionMapper {

  /**
   * Maps a ClaimDecision entity to a ClaimDecisionDto.
   *
   * @param claimDecision The source entity.
   * @return The target DTO.
   */
  @Mapping(target = "claimId", source = "claim.id")
  @Mapping(target = "decisionMakerId", source = "decisionMaker.id")
  @Mapping(
      target = "decisionMakerName",
      expression =
          "java(claimDecision.getDecisionMaker() != null ? claimDecision.getDecisionMaker().getFirstName() + \" \" + claimDecision.getDecisionMaker().getLastName() : null)")
  ClaimDecisionDto toDto(ClaimDecision claimDecision);

  /**
   * Maps a ClaimDecisionDto to a ClaimDecision entity.
   *
   * @param claimDecisionDto The source DTO.
   * @return The target entity.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claim", ignore = true)
  @Mapping(target = "decisionMaker", ignore = true)
  @Mapping(target = "decisionDate", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ClaimDecision toEntity(ClaimDecisionDto claimDecisionDto);

  /**
   * Updates an existing ClaimDecision entity from a ClaimDecisionDto.
   *
   * @param claimDecisionDto The source DTO with updated information.
   * @param claimDecision The target entity to update.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claim", ignore = true)
  @Mapping(target = "decisionMaker", ignore = true)
  @Mapping(target = "decisionDate", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateClaimDecisionFromDto(
      ClaimDecisionDto claimDecisionDto, @MappingTarget ClaimDecision claimDecision);

  /** Converts ZonedDateTime to OffsetDateTime. */
  default OffsetDateTime map(ZonedDateTime zonedDateTime) {
    return zonedDateTime != null ? zonedDateTime.toOffsetDateTime() : null;
  }

  /** Converts OffsetDateTime to ZonedDateTime. */
  default ZonedDateTime map(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toZonedDateTime() : null;
  }
}
