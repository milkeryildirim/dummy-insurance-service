package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.api.generated.model.AdjusterReportDto;

/** Service Interface for managing {@link AdjusterReport}. */
public interface AdjusterReportService {

  /**
   * Creates a new adjuster report for a claim.
   *
   * @param claimId The ID of the claim.
   * @param adjusterReportDto The DTO containing the report data.
   * @param pdfFile The PDF file to upload (optional).
   * @return The created adjuster report DTO.
   */
  AdjusterReportDto createAdjusterReport(
      Long claimId, AdjusterReportDto adjusterReportDto, MultipartFile pdfFile);

  /**
   * Retrieves all adjuster reports for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return A list of adjuster report DTOs.
   */
  List<AdjusterReportDto> findAdjusterReportsByClaimId(Long claimId);

  /**
   * Finds a specific adjuster report by claim ID and report ID.
   *
   * @param claimId The ID of the claim.
   * @param reportId The ID of the report.
   * @return An Optional containing the found adjuster report DTO, or empty if not found.
   */
  Optional<AdjusterReportDto> findAdjusterReportByClaimIdAndReportId(Long claimId, Long reportId);

  /**
   * Updates an existing adjuster report.
   *
   * @param claimId The ID of the claim.
   * @param reportId The ID of the report.
   * @param adjusterReportDto The DTO containing the updated report data.
   * @return The updated adjuster report DTO.
   */
  AdjusterReportDto updateAdjusterReport(
      Long claimId, Long reportId, AdjusterReportDto adjusterReportDto);

  /**
   * Deletes an adjuster report.
   *
   * @param claimId The ID of the claim.
   * @param reportId The ID of the report.
   */
  void deleteAdjusterReport(Long claimId, Long reportId);

  /**
   * Retrieves all adjuster reports created by a specific adjuster.
   *
   * @param adjusterId The ID of the adjuster.
   * @return A list of adjuster report DTOs.
   */
  List<AdjusterReportDto> findAdjusterReportsByAdjusterId(Long adjusterId);

  /**
   * Updates the status of an adjuster report.
   *
   * @param claimId The ID of the claim.
   * @param reportId The ID of the report.
   * @param status The new status.
   * @return The updated adjuster report DTO.
   */
  AdjusterReportDto updateAdjusterReportStatus(Long claimId, Long reportId, ReportStatus status);
}
