package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.api.generated.model.AdjusterReportDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.employee.Employee;
import tech.yildirim.insurance.dummy.employee.EmployeeRepository;
import tech.yildirim.insurance.dummy.employee.EmployeeRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("Adjuster Report Service Unit Tests")
class AdjusterReportServiceImplTest {

  @Mock private AdjusterReportRepository adjusterReportRepository;
  @Mock private ClaimRepository claimRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private AdjusterReportMapper adjusterReportMapper;
  @Mock private ClaimStorageService claimStorageService;

  @InjectMocks private AdjusterReportServiceImpl adjusterReportService;

  private Long claimId;
  private Long reportId;
  private Long adjusterId;
  private Claim testClaim;
  private Employee testAdjuster;
  private AdjusterReport testReport;
  private AdjusterReportDto testReportDto;

  @BeforeEach
  void setUp() {
    claimId = 1L;
    reportId = 2L;
    adjusterId = 3L;

    // Setup test claim
    testClaim = new AutoClaim();
    testClaim.setId(claimId);
    testClaim.setClaimNumber("CLM-TEST-001");

    // Setup test adjuster
    testAdjuster = new Employee();
    testAdjuster.setId(adjusterId);
    testAdjuster.setFirstName("John");
    testAdjuster.setLastName("Adjuster");
    testAdjuster.setRole(EmployeeRole.CLAIMS_ADJUSTER);

    // Setup test adjuster report
    testReport = new AdjusterReport();
    testReport.setId(reportId);
    testReport.setClaim(testClaim);
    testReport.setAdjuster(testAdjuster);
    testReport.setSummary("Test summary");
    testReport.setFindings("Test findings");
    testReport.setRecommendations("Test recommendations");
    testReport.setRecommendedAmount(BigDecimal.valueOf(1000.00));
    testReport.setStatus(ReportStatus.DRAFT);
    testReport.setCreatedAt(ZonedDateTime.now());

    // Setup test DTO
    testReportDto =
        new AdjusterReportDto()
            .claimId(claimId)
            .adjusterId(adjusterId)
            .summary("Test summary")
            .findings("Test findings")
            .recommendations("Test recommendations")
            .recommendedAmount(BigDecimal.valueOf(1000.00))
            .status(AdjusterReportDto.StatusEnum.DRAFT);
  }

  // ========== Create Adjuster Report Tests ==========

  @Test
  @DisplayName("Should create adjuster report successfully without PDF")
  void createAdjusterReport_withoutPdf_shouldCreateSuccessfully() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(adjusterId)).thenReturn(Optional.of(testAdjuster));
    when(adjusterReportMapper.toEntity(testReportDto)).thenReturn(testReport);
    when(adjusterReportRepository.save(any(AdjusterReport.class))).thenReturn(testReport);
    when(adjusterReportMapper.toDto(testReport)).thenReturn(testReportDto);

    // When
    AdjusterReportDto result =
        adjusterReportService.createAdjusterReport(claimId, testReportDto, null);

    // Then
    assertThat(result).isEqualTo(testReportDto);
    verify(claimRepository).findById(claimId);
    verify(employeeRepository).findById(adjusterId);
    verify(adjusterReportRepository).save(any(AdjusterReport.class));
    verify(claimStorageService, never()).storeAdjusterReportPdf(anyLong(), anyLong(), any());
  }

  @Test
  @DisplayName("Should create adjuster report successfully with PDF")
  void createAdjusterReport_withPdf_shouldCreateSuccessfully() {
    // Given
    MultipartFile pdfFile = mock(MultipartFile.class);
    when(pdfFile.isEmpty()).thenReturn(false);
    when(pdfFile.getOriginalFilename()).thenReturn("report.pdf");

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(adjusterId)).thenReturn(Optional.of(testAdjuster));
    when(adjusterReportMapper.toEntity(testReportDto)).thenReturn(testReport);

    when(adjusterReportRepository.save(any(AdjusterReport.class))).thenReturn(testReport);

    when(claimStorageService.storeAdjusterReportPdf(claimId, reportId, pdfFile))
        .thenReturn("adjuster-reports/claim-1/report-2/test.pdf");
    when(adjusterReportMapper.toDto(testReport)).thenReturn(testReportDto);

    // When
    AdjusterReportDto result =
        adjusterReportService.createAdjusterReport(claimId, testReportDto, pdfFile);

    // Then
    assertThat(result).isEqualTo(testReportDto);
    verify(claimStorageService).storeAdjusterReportPdf(claimId, reportId, pdfFile);

    verify(adjusterReportRepository, times(2)).save(any(AdjusterReport.class));

    ArgumentCaptor<AdjusterReport> reportCaptor = ArgumentCaptor.forClass(AdjusterReport.class);
    verify(adjusterReportRepository, atLeastOnce()).save(reportCaptor.capture());

    AdjusterReport savedReport = reportCaptor.getValue();
    assertThat(savedReport.getClaim()).isEqualTo(testClaim);
    assertThat(savedReport.getAdjuster()).isEqualTo(testAdjuster);
  }

  @Test
  @DisplayName("Should set default status to DRAFT when not provided")
  void createAdjusterReport_withoutStatus_shouldSetDefaultStatus() {
    // Given
    testReport.setStatus(null);
    testReportDto.setStatus(null);

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(adjusterId)).thenReturn(Optional.of(testAdjuster));
    when(adjusterReportMapper.toEntity(testReportDto)).thenReturn(testReport);
    when(adjusterReportRepository.save(any(AdjusterReport.class))).thenReturn(testReport);
    when(adjusterReportMapper.toDto(testReport)).thenReturn(testReportDto);

    // When
    adjusterReportService.createAdjusterReport(claimId, testReportDto, null);

    // Then
    ArgumentCaptor<AdjusterReport> reportCaptor = ArgumentCaptor.forClass(AdjusterReport.class);
    verify(adjusterReportRepository).save(reportCaptor.capture());

    AdjusterReport savedReport = reportCaptor.getValue();
    assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.DRAFT);
  }

  @Test
  @DisplayName("Should throw exception when claim not found")
  void createAdjusterReport_withNonExistentClaim_shouldThrowException() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(
            () -> adjusterReportService.createAdjusterReport(claimId, testReportDto, null))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Claim not found with id: " + claimId);

    verify(employeeRepository, never()).findById(anyLong());
    verify(adjusterReportRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when adjuster not found")
  void createAdjusterReport_withNonExistentAdjuster_shouldThrowException() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(employeeRepository.findById(adjusterId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(
            () -> adjusterReportService.createAdjusterReport(claimId, testReportDto, null))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Employee not found with id: " + adjusterId);

    verify(adjusterReportRepository, never()).save(any());
  }

  // ========== Find Adjuster Reports Tests ==========

  @Test
  @DisplayName("Should find adjuster reports by claim ID successfully")
  void findAdjusterReportsByClaimId_withExistingClaim_shouldReturnReports() {
    // Given
    List<AdjusterReport> reports = Collections.singletonList(testReport);
    List<AdjusterReportDto> reportDtos = Collections.singletonList(testReportDto);

    when(claimRepository.existsById(claimId)).thenReturn(true);
    when(adjusterReportRepository.findByClaimId(claimId)).thenReturn(reports);
    when(adjusterReportMapper.toDtoList(reports)).thenReturn(reportDtos);

    // When
    List<AdjusterReportDto> result = adjusterReportService.findAdjusterReportsByClaimId(claimId);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(testReportDto);
    verify(claimRepository).existsById(claimId);
    verify(adjusterReportRepository).findByClaimId(claimId);
  }

  @Test
  @DisplayName("Should throw exception when finding reports for non-existent claim")
  void findAdjusterReportsByClaimId_withNonExistentClaim_shouldThrowException() {
    // Given
    when(claimRepository.existsById(claimId)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> adjusterReportService.findAdjusterReportsByClaimId(claimId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Claim not found with id: " + claimId);

    verify(adjusterReportRepository, never()).findByClaimId(anyLong());
  }

  // ========== Find Specific Report Tests ==========

  @Test
  @DisplayName("Should find specific adjuster report successfully")
  void findAdjusterReportByClaimIdAndReportId_withExistingReport_shouldReturnReport() {
    // Given
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(testReport);
    when(adjusterReportMapper.toDto(testReport)).thenReturn(testReportDto);

    // When
    Optional<AdjusterReportDto> result =
        adjusterReportService.findAdjusterReportByClaimIdAndReportId(claimId, reportId);

    // Then
    assertThat(result).isPresent();
    assertThat(result).contains(testReportDto);
  }

  @Test
  @DisplayName("Should return empty when report not found")
  void findAdjusterReportByClaimIdAndReportId_withNonExistentReport_shouldReturnEmpty() {
    // Given
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(null);

    // When
    Optional<AdjusterReportDto> result =
        adjusterReportService.findAdjusterReportByClaimIdAndReportId(claimId, reportId);

    // Then
    assertThat(result).isEmpty();
  }

  // ========== Update Report Tests ==========

  @Test
  @DisplayName("Should update adjuster report successfully")
  void updateAdjusterReport_withExistingReport_shouldUpdateSuccessfully() {
    // Given
    AdjusterReportDto updateDto =
        new AdjusterReportDto().summary("Updated summary").findings("Updated findings");

    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(testReport);
    when(adjusterReportRepository.save(testReport)).thenReturn(testReport);
    when(adjusterReportMapper.toDto(testReport)).thenReturn(testReportDto);

    // When
    AdjusterReportDto result =
        adjusterReportService.updateAdjusterReport(claimId, reportId, updateDto);

    // Then
    assertThat(result).isEqualTo(testReportDto);
    verify(adjusterReportMapper).updateAdjusterReportFromDto(updateDto, testReport);
    verify(adjusterReportRepository).save(testReport);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent report")
  void updateAdjusterReport_withNonExistentReport_shouldThrowException() {
    // Given
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(null);

    // When & Then
    assertThatThrownBy(
            () -> adjusterReportService.updateAdjusterReport(claimId, reportId, testReportDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(
            "Adjuster report not found with id: " + reportId + " for claim: " + claimId);

    verify(adjusterReportRepository, never()).save(any());
  }

  // ========== Delete Report Tests ==========

  @Test
  @DisplayName("Should delete adjuster report successfully")
  void deleteAdjusterReport_withExistingReport_shouldDeleteSuccessfully() {
    // Given
    testReport.setReportPdfPath("adjuster-reports/claim-1/report-2/test.pdf");
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(testReport);
    doNothing().when(claimStorageService).deleteFile(anyString());

    // When
    adjusterReportService.deleteAdjusterReport(claimId, reportId);

    // Then
    verify(claimStorageService).deleteFile("adjuster-reports/claim-1/report-2/test.pdf");
    verify(adjusterReportRepository).delete(testReport);
  }

  @Test
  @DisplayName("Should delete report without PDF file successfully")
  void deleteAdjusterReport_withoutPdfFile_shouldDeleteSuccessfully() {
    // Given
    testReport.setReportPdfPath(null);
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(testReport);

    // When
    adjusterReportService.deleteAdjusterReport(claimId, reportId);

    // Then
    verify(claimStorageService, never()).deleteFile(anyString());
    verify(adjusterReportRepository).delete(testReport);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent report")
  void deleteAdjusterReport_withNonExistentReport_shouldThrowException() {
    // Given
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(null);

    // When & Then
    assertThatThrownBy(() -> adjusterReportService.deleteAdjusterReport(claimId, reportId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(
            "Adjuster report not found with id: " + reportId + " for claim: " + claimId);

    verify(adjusterReportRepository, never()).delete(any());
  }

  // ========== Update Status Tests ==========

  @Test
  @DisplayName("Should update report status successfully")
  void updateAdjusterReportStatus_withExistingReport_shouldUpdateStatus() {
    // Given
    ReportStatus newStatus = ReportStatus.SUBMITTED;
    when(adjusterReportRepository.findByClaimIdAndId(claimId, reportId)).thenReturn(testReport);
    when(adjusterReportRepository.save(testReport)).thenReturn(testReport);
    when(adjusterReportMapper.toDto(testReport)).thenReturn(testReportDto);

    // When
    AdjusterReportDto result =
        adjusterReportService.updateAdjusterReportStatus(claimId, reportId, newStatus);

    // Then
    assertThat(result).isEqualTo(testReportDto);
    verify(adjusterReportRepository).save(testReport);
    assertThat(testReport.getStatus()).isEqualTo(newStatus);
  }

  // ========== Find by Adjuster Tests ==========

  @Test
  @DisplayName("Should find reports by adjuster ID successfully")
  void findAdjusterReportsByAdjusterId_withExistingAdjuster_shouldReturnReports() {
    // Given
    List<AdjusterReport> reports = Collections.singletonList(testReport);
    List<AdjusterReportDto> reportDtos = Collections.singletonList(testReportDto);

    when(employeeRepository.existsById(adjusterId)).thenReturn(true);
    when(adjusterReportRepository.findByAdjusterId(adjusterId)).thenReturn(reports);
    when(adjusterReportMapper.toDtoList(reports)).thenReturn(reportDtos);

    // When
    List<AdjusterReportDto> result =
        adjusterReportService.findAdjusterReportsByAdjusterId(adjusterId);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(testReportDto);
    verify(employeeRepository).existsById(adjusterId);
    verify(adjusterReportRepository).findByAdjusterId(adjusterId);
  }

  @Test
  @DisplayName("Should throw exception when finding reports for non-existent adjuster")
  void findAdjusterReportsByAdjusterId_withNonExistentAdjuster_shouldThrowException() {
    // Given
    when(employeeRepository.existsById(adjusterId)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> adjusterReportService.findAdjusterReportsByAdjusterId(adjusterId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Employee not found with id: " + adjusterId);

    verify(adjusterReportRepository, never()).findByAdjusterId(anyLong());
  }
}
