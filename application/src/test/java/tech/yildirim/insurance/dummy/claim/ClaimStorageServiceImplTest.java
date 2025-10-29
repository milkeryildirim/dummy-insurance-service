package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@DisplayName("Claim Storage Service Unit Tests")
class ClaimStorageServiceImplTest {

  @TempDir Path tempDir;

  private ClaimStorageService claimStorageService;
  private String uploadDir;

  @BeforeEach
  void setUp() {
    uploadDir = tempDir.toString();
    claimStorageService = new ClaimStorageServiceImpl(uploadDir);
  }

  @AfterEach
  void tearDown() {
    // Cleanup is automatically handled by @TempDir
  }

  // ========== PDF Validation Tests ==========

  @Test
  @DisplayName("Should validate PDF file correctly")
  void isValidPdf_withValidPdf_shouldReturnTrue() {
    // Given: A valid PDF file mock
    MultipartFile pdfFile = createMockPdfFile("test.pdf", "application/pdf", "%PDF-1.4 content");

    // When & Then
    assertThat(claimStorageService.isValidPdf(pdfFile)).isTrue();
  }

  @Test
  @DisplayName("Should reject non-PDF file extension")
  void isValidPdf_withNonPdfExtension_shouldReturnFalse() {
    // Given: A file with non-PDF extension
    MultipartFile txtFile = createMockPdfFile("test.txt", "application/pdf", "%PDF-1.4 content");

    // When & Then
    assertThat(claimStorageService.isValidPdf(txtFile)).isFalse();
  }

  @Test
  @DisplayName("Should reject non-PDF MIME type")
  void isValidPdf_withNonPdfMimeType_shouldReturnFalse() {
    // Given: A file with non-PDF MIME type
    MultipartFile file = createMockPdfFile("test.pdf", "text/plain", "%PDF-1.4 content");

    // When & Then
    assertThat(claimStorageService.isValidPdf(file)).isFalse();
  }

  @Test
  @DisplayName("Should reject file without PDF header")
  void isValidPdf_withoutPdfHeader_shouldReturnFalse() {
    // Given: A file without proper PDF header
    MultipartFile file = createMockPdfFile("test.pdf", "application/pdf", "Not a PDF content");

    // When & Then
    assertThat(claimStorageService.isValidPdf(file)).isFalse();
  }

  @Test
  @DisplayName("Should reject null file")
  void isValidPdf_withNullFile_shouldReturnFalse() {
    // When & Then
    assertThat(claimStorageService.isValidPdf(null)).isFalse();
  }

  @Test
  @DisplayName("Should reject empty file")
  void isValidPdf_withEmptyFile_shouldReturnFalse() {
    // Given: An empty file
    MultipartFile emptyFile = mock(MultipartFile.class);
    when(emptyFile.isEmpty()).thenReturn(true);

    // When & Then
    assertThat(claimStorageService.isValidPdf(emptyFile)).isFalse();
  }

  // ========== Adjuster Report Storage Tests ==========

  @Test
  @DisplayName("Should store adjuster report PDF successfully")
  void storeAdjusterReportPdf_withValidFile_shouldStoreSuccessfully() {
    // Given: Valid PDF file and IDs
    Long claimId = 1L;
    Long reportId = 2L;
    MultipartFile pdfFile =
        createMockPdfFile("report.pdf", "application/pdf", "%PDF-1.4 test content");

    // When: Store the file
    String filePath = claimStorageService.storeAdjusterReportPdf(claimId, reportId, pdfFile);

    // Then: File should be stored with correct path structure
    assertThat(filePath).isNotNull();
    assertThat(filePath).startsWith("adjuster-reports/claim-1/report-2/");
    assertThat(filePath).endsWith(".pdf");

    // And: File should exist on disk
    Path storedFile = Paths.get(uploadDir, filePath);
    assertThat(Files.exists(storedFile)).isTrue();
  }

  @Test
  @DisplayName("Should throw exception when storing null file for adjuster report")
  void storeAdjusterReportPdf_withNullFile_shouldThrowException() {
    // Given: Null file
    Long claimId = 1L;
    Long reportId = 2L;

    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(() -> claimStorageService.storeAdjusterReportPdf(claimId, reportId, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw exception when storing empty file for adjuster report")
  void storeAdjusterReportPdf_withEmptyFile_shouldThrowException() {
    // Given: Empty file
    Long claimId = 1L;
    Long reportId = 2L;
    MultipartFile emptyFile = mock(MultipartFile.class);
    when(emptyFile.isEmpty()).thenReturn(true);

    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(
            () -> claimStorageService.storeAdjusterReportPdf(claimId, reportId, emptyFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw exception when storing invalid PDF for adjuster report")
  void storeAdjusterReportPdf_withInvalidPdf_shouldThrowException() {
    // Given: Invalid PDF file
    Long claimId = 1L;
    Long reportId = 2L;
    MultipartFile invalidFile = createMockPdfFile("test.txt", "text/plain", "Not a PDF");

    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(
            () -> claimStorageService.storeAdjusterReportPdf(claimId, reportId, invalidFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File must be a valid PDF");
  }

  // ========== Customer Invoice Storage Tests ==========

  @Test
  @DisplayName("Should store customer invoice PDF successfully")
  void storeCustomerInvoicePdf_withValidFile_shouldStoreSuccessfully() {
    // Given: Valid PDF file and IDs
    Long claimId = 3L;
    Long invoiceId = 4L;
    MultipartFile pdfFile =
        createMockPdfFile("invoice.pdf", "application/pdf", "%PDF-1.4 invoice content");

    // When: Store the file
    String filePath = claimStorageService.storeCustomerInvoicePdf(claimId, invoiceId, pdfFile);

    // Then: File should be stored with correct path structure
    assertThat(filePath).isNotNull();
    assertThat(filePath).startsWith("customer-invoices/claim-3/invoice-4/");
    assertThat(filePath).endsWith(".pdf");

    // And: File should exist on disk
    Path storedFile = Paths.get(uploadDir, filePath);
    assertThat(Files.exists(storedFile)).isTrue();
  }

  @Test
  @DisplayName("Should throw exception when storing null file for customer invoice")
  void storeCustomerInvoicePdf_withNullFile_shouldThrowException() {
    // Given: Null file
    Long claimId = 3L;
    Long invoiceId = 4L;

    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(() -> claimStorageService.storeCustomerInvoicePdf(claimId, invoiceId, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File cannot be null or empty");
  }

  // ========== File Retrieval Tests ==========

  @Test
  @DisplayName("Should retrieve file successfully")
  void retrieveFile_withExistingFile_shouldReturnFileContent() throws IOException {
    // Given: A file stored on disk
    String testContent = "%PDF-1.4 test file content";
    Path testFile = tempDir.resolve("test-file.pdf");
    Files.write(testFile, testContent.getBytes());
    String relativePath = testFile.getFileName().toString();

    // When: Retrieve the file
    byte[] retrievedContent = claimStorageService.retrieveFile(relativePath);

    // Then: Content should match original
    assertThat(retrievedContent).isEqualTo(testContent.getBytes());
  }

  @Test
  @DisplayName("Should throw exception when retrieving non-existent file")
  void retrieveFile_withNonExistentFile_shouldThrowException() {
    // Given: Non-existent file path
    String nonExistentPath = "non-existent/file.pdf";

    // When & Then: Should throw ResourceNotFoundException
    assertThatThrownBy(() -> claimStorageService.retrieveFile(nonExistentPath))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("File not found: " + nonExistentPath);
  }

  @Test
  @DisplayName("Should throw exception when retrieving file with null path")
  void retrieveFile_withNullPath_shouldThrowException() {
    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(() -> claimStorageService.retrieveFile(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File path cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw exception when retrieving file with empty path")
  void retrieveFile_withEmptyPath_shouldThrowException() {
    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(() -> claimStorageService.retrieveFile(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File path cannot be null or empty");
  }

  // ========== File Deletion Tests ==========

  @Test
  @DisplayName("Should delete file successfully")
  void deleteFile_withExistingFile_shouldDeleteSuccessfully() throws IOException {
    // Given: A file stored on disk
    Path testFile = tempDir.resolve("test-delete.pdf");
    Files.write(testFile, "test content".getBytes());
    String relativePath = testFile.getFileName().toString();

    // Verify file exists before deletion
    assertThat(Files.exists(testFile)).isTrue();

    // When: Delete the file
    claimStorageService.deleteFile(relativePath);

    // Then: File should be deleted
    assertThat(Files.exists(testFile)).isFalse();
  }

  @Test
  @DisplayName("Should handle deletion of non-existent file gracefully")
  void deleteFile_withNonExistentFile_shouldNotThrowException() {
    // Given: Non-existent file path
    String nonExistentPath = "non-existent/file.pdf";

    // When & Then: Should not throw exception
    assertThatCode(() -> claimStorageService.deleteFile(nonExistentPath))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Should throw exception when deleting file with null path")
  void deleteFile_withNullPath_shouldThrowException() {
    // When & Then: Should throw IllegalArgumentException
    assertThatThrownBy(() -> claimStorageService.deleteFile(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File path cannot be null or empty");
  }

  // ========== Helper Methods ==========

  private MultipartFile createMockPdfFile(String filename, String contentType, String content) {
    MultipartFile mockFile = mock(MultipartFile.class);

    try {
      when(mockFile.getOriginalFilename()).thenReturn(filename);
      when(mockFile.getContentType()).thenReturn(contentType);
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to create mock file", e);
    }

    return mockFile;
  }
}
