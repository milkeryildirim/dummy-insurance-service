package tech.yildirim.insurance.dummy.claim;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tech.yildirim.insurance.api.generated.model.AdjusterReportDto;

/** Mapper for the entity {@link AdjusterReport} and its DTO {@link AdjusterReportDto}. */
@Mapper(componentModel = "spring")
public interface AdjusterReportMapper {

  /**
   * Maps an AdjusterReport entity to an AdjusterReportDto.
   *
   * @param adjusterReport The source entity.
   * @return The target DTO.
   */
  @Mapping(target = "claimId", source = "claim.id")
  @Mapping(target = "adjusterId", source = "adjuster.id")
  @Mapping(
      target = "adjusterName",
      expression =
          "java(adjusterReport.getAdjuster() != null ? adjusterReport.getAdjuster().getFirstName() + \" \" + adjusterReport.getAdjuster().getLastName() : null)")
  AdjusterReportDto toDto(AdjusterReport adjusterReport);

  /**
   * Maps a list of AdjusterReport entities to a list of AdjusterReportDtos.
   *
   * @param adjusterReports The list of source entities.
   * @return The list of target DTOs.
   */
  List<AdjusterReportDto> toDtoList(List<AdjusterReport> adjusterReports);

  /**
   * Maps an AdjusterReportDto to an AdjusterReport entity.
   *
   * @param adjusterReportDto The source DTO.
   * @return The target entity.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claim", ignore = true)
  @Mapping(target = "adjuster", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  AdjusterReport toEntity(AdjusterReportDto adjusterReportDto);

  /**
   * Updates an existing AdjusterReport entity from an AdjusterReportDto.
   *
   * @param adjusterReportDto The source DTO with updated information.
   * @param adjusterReport The target entity to update.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claim", ignore = true)
  @Mapping(target = "adjuster", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateAdjusterReportFromDto(
      AdjusterReportDto adjusterReportDto, @MappingTarget AdjusterReport adjusterReport);

  /** Converts ZonedDateTime to OffsetDateTime. */
  default OffsetDateTime map(ZonedDateTime zonedDateTime) {
    return zonedDateTime != null ? zonedDateTime.toOffsetDateTime() : null;
  }

  /** Converts OffsetDateTime to ZonedDateTime. */
  default ZonedDateTime map(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toZonedDateTime() : null;
  }
}
