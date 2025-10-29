package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.api.generated.model.AdjusterReportDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.employee.EmployeeRepository;

/** Implementation of {@link AdjusterReportService}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdjusterReportServiceImpl implements AdjusterReportService {

  private final AdjusterReportRepository adjusterReportRepository;
  private final ClaimRepository claimRepository;
  private final EmployeeRepository employeeRepository;
  private final AdjusterReportMapper adjusterReportMapper;
  private final ClaimStorageService claimStorageService;

  @Override
  @Transactional
  public AdjusterReportDto createAdjusterReport(
      Long claimId, AdjusterReportDto adjusterReportDto, MultipartFile pdfFile) {
    log.info("Creating adjuster report for claim {}", claimId);

    // Validate claim exists
    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    // Validate adjuster exists
    Employee adjuster =
        employeeRepository
            .findById(adjusterReportDto.getAdjusterId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Employee not found with id: " + adjusterReportDto.getAdjusterId()));

    // Create and populate adjuster report
    AdjusterReport adjusterReport = adjusterReportMapper.toEntity(adjusterReportDto);
    adjusterReport.setClaim(claim);
    adjusterReport.setAdjuster(adjuster);

    // Set default status if not provided
    if (adjusterReport.getStatus() == null) {
      adjusterReport.setStatus(ReportStatus.DRAFT);
    }

    // Save the report first to get an ID
    AdjusterReport savedReport = adjusterReportRepository.save(adjusterReport);

    // Handle PDF file upload if provided
    if (pdfFile != null && !pdfFile.isEmpty()) {
      String filePath =
          claimStorageService.storeAdjusterReportPdf(claimId, savedReport.getId(), pdfFile);
      savedReport.setReportPdfPath(filePath);
      savedReport.setOriginalPdfFilename(pdfFile.getOriginalFilename());

      // Save again with file path
      savedReport = adjusterReportRepository.save(savedReport);
    }

    log.info("Successfully created adjuster report with id {}", savedReport.getId());
    return adjusterReportMapper.toDto(savedReport);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdjusterReportDto> findAdjusterReportsByClaimId(Long claimId) {
    log.info("Finding adjuster reports for claim {}", claimId);

    // Validate claim exists
    if (!claimRepository.existsById(claimId)) {
      throw new ResourceNotFoundException("Claim not found with id: " + claimId);
    }

    List<AdjusterReport> reports = adjusterReportRepository.findByClaimId(claimId);
    log.info("Found {} adjuster reports for claim {}", reports.size(), claimId);

    return adjusterReportMapper.toDtoList(reports);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AdjusterReportDto> findAdjusterReportByClaimIdAndReportId(
      Long claimId, Long reportId) {
    log.info("Finding adjuster report {} for claim {}", reportId, claimId);

    AdjusterReport report = adjusterReportRepository.findByClaimIdAndId(claimId, reportId);
    if (report == null) {
      log.warn("Adjuster report {} not found for claim {}", reportId, claimId);
      return Optional.empty();
    }

    return Optional.of(adjusterReportMapper.toDto(report));
  }

  @Override
  @Transactional
  public AdjusterReportDto updateAdjusterReport(
      Long claimId, Long reportId, AdjusterReportDto adjusterReportDto) {
    log.info("Updating adjuster report {} for claim {}", reportId, claimId);

    AdjusterReport existingReport = adjusterReportRepository.findByClaimIdAndId(claimId, reportId);
    if (existingReport == null) {
      throw new ResourceNotFoundException(
          "Adjuster report not found with id: " + reportId + " for claim: " + claimId);
    }

    // Update the existing report
    adjusterReportMapper.updateAdjusterReportFromDto(adjusterReportDto, existingReport);

    AdjusterReport updatedReport = adjusterReportRepository.save(existingReport);
    log.info("Successfully updated adjuster report {}", reportId);

    return adjusterReportMapper.toDto(updatedReport);
  }

  @Override
  @Transactional
  public void deleteAdjusterReport(Long claimId, Long reportId) {
    log.info("Deleting adjuster report {} for claim {}", reportId, claimId);

    AdjusterReport report = adjusterReportRepository.findByClaimIdAndId(claimId, reportId);
    if (report == null) {
      throw new ResourceNotFoundException(
          "Adjuster report not found with id: " + reportId + " for claim: " + claimId);
    }

    // Delete PDF file if exists
    if (report.getReportPdfPath() != null) {
      try {
        claimStorageService.deleteFile(report.getReportPdfPath());
      } catch (Exception e) {
        log.warn("Failed to delete PDF file for adjuster report {}: {}", reportId, e.getMessage());
      }
    }

    adjusterReportRepository.delete(report);
    log.info("Successfully deleted adjuster report {}", reportId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdjusterReportDto> findAdjusterReportsByAdjusterId(Long adjusterId) {
    log.info("Finding adjuster reports by adjuster {}", adjusterId);

    // Validate adjuster exists
    if (!employeeRepository.existsById(adjusterId)) {
      throw new ResourceNotFoundException("Employee not found with id: " + adjusterId);
    }

    List<AdjusterReport> reports = adjusterReportRepository.findByAdjusterId(adjusterId);
    log.info("Found {} adjuster reports for adjuster {}", reports.size(), adjusterId);

    return adjusterReportMapper.toDtoList(reports);
  }

  @Override
  @Transactional
  public AdjusterReportDto updateAdjusterReportStatus(
      Long claimId, Long reportId, ReportStatus status) {
    log.info("Updating adjuster report {} status to {} for claim {}", reportId, status, claimId);

    AdjusterReport report = adjusterReportRepository.findByClaimIdAndId(claimId, reportId);
    if (report == null) {
      throw new ResourceNotFoundException(
          "Adjuster report not found with id: " + reportId + " for claim: " + claimId);
    }

    report.setStatus(status);
    AdjusterReport updatedReport = adjusterReportRepository.save(report);

    log.info("Successfully updated adjuster report {} status to {}", reportId, status);
    return adjusterReportMapper.toDto(updatedReport);
  }
}
