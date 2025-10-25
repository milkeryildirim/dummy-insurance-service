package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjusterReportRepository extends JpaRepository<AdjusterReport, Long> {

  /**
   * Find all adjuster reports for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return List of adjuster reports for the claim.
   */
  List<AdjusterReport> findByClaimId(Long claimId);

  /**
   * Find all adjuster reports created by a specific adjuster.
   *
   * @param adjusterId The ID of the adjuster.
   * @return List of adjuster reports by the adjuster.
   */
  List<AdjusterReport> findByAdjusterId(Long adjusterId);

  /**
   * Find adjuster reports by claim ID and status.
   *
   * @param claimId The ID of the claim.
   * @param status The status of the reports.
   * @return List of adjuster reports matching the criteria.
   */
  List<AdjusterReport> findByClaimIdAndStatus(Long claimId, ReportStatus status);

  /**
   * Find a specific adjuster report by claim ID and report ID.
   *
   * @param claimId The ID of the claim.
   * @param reportId The ID of the report.
   * @return The adjuster report if found.
   */
  @Query("SELECT ar FROM AdjusterReport ar WHERE ar.claim.id = :claimId AND ar.id = :reportId")
  AdjusterReport findByClaimIdAndId(
      @Param("claimId") Long claimId, @Param("reportId") Long reportId);
}
