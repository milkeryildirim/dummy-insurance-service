package tech.yildirim.insurance.dummy.claim;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;

@Mapper(
    componentModel = "spring",
    uses = {AdjusterReportMapper.class, CustomerInvoiceMapper.class, ClaimDecisionMapper.class})
public interface ClaimMapper {

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
  @Mapping(source = "adjusterReports", target = "adjusterReports")
  @Mapping(source = "customerInvoices", target = "customerInvoices")
  @Mapping(source = "claimDecision", target = "claimDecision")
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
  @Mapping(target = "adjusterReports", ignore = true)
  @Mapping(target = "customerInvoices", ignore = true)
  @Mapping(target = "claimDecision", ignore = true)
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
  @Mapping(source = "adjusterReports", target = "adjusterReports")
  @Mapping(source = "customerInvoices", target = "customerInvoices")
  @Mapping(source = "claimDecision", target = "claimDecision")
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
  @Mapping(target = "adjusterReports", ignore = true)
  @Mapping(target = "customerInvoices", ignore = true)
  @Mapping(target = "claimDecision", ignore = true)
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
  @Mapping(source = "adjusterReports", target = "adjusterReports")
  @Mapping(source = "customerInvoices", target = "customerInvoices")
  @Mapping(source = "claimDecision", target = "claimDecision")
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
  @Mapping(target = "adjusterReports", ignore = true)
  @Mapping(target = "customerInvoices", ignore = true)
  @Mapping(target = "claimDecision", ignore = true)
  void populateHealthClaimFromDto(HealthClaimDto dto, @MappingTarget HealthClaim entity);

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
