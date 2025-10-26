package tech.yildirim.insurance.dummy.claim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

/** Implementation of {@link ClaimStorageService } for managing PDF file storage operations. */
@Service
@Slf4j
public class ClaimStorageServiceImpl implements ClaimStorageService {

  private final String uploadDir;
  private final String adjusterReportsDir;
  private final String customerInvoicesDir;

  public ClaimStorageServiceImpl(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
    this.uploadDir = uploadDir;
    this.adjusterReportsDir = uploadDir + "/adjuster-reports";
    this.customerInvoicesDir = uploadDir + "/customer-invoices";

    // Create directories if they don't exist
    createDirectories();
  }

  @Override
  public String storeAdjusterReportPdf(Long claimId, Long reportId, MultipartFile file) {
    log.info("Storing adjuster report PDF for claim {} and report {}", claimId, reportId);

    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null or empty");
    }

    if (!isValidPdf(file)) {
      throw new IllegalArgumentException("File must be a valid PDF");
    }

    String fileName = generateFileName(file.getOriginalFilename());
    String relativePath =
        String.format("adjuster-reports/claim-%d/report-%d/%s", claimId, reportId, fileName);
    Path targetPath = Paths.get(uploadDir, relativePath);

    try {
      // Create parent directories if they don't exist
      Files.createDirectories(targetPath.getParent());

      // Copy file to target location
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

      log.info("Successfully stored adjuster report PDF at: {}", relativePath);
      return relativePath;

    } catch (IOException e) {
      log.error(
          "Failed to store adjuster report PDF for claim {} and report {}", claimId, reportId, e);
      throw new RuntimeException("Failed to store file", e);
    }
  }

  @Override
  public String storeCustomerInvoicePdf(Long claimId, Long invoiceId, MultipartFile file) {
    log.info("Storing customer invoice PDF for claim {} and invoice {}", claimId, invoiceId);

    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null or empty");
    }

    if (!isValidPdf(file)) {
      throw new IllegalArgumentException("File must be a valid PDF");
    }

    String fileName = generateFileName(file.getOriginalFilename());
    String relativePath =
        String.format("customer-invoices/claim-%d/invoice-%d/%s", claimId, invoiceId, fileName);
    Path targetPath = Paths.get(uploadDir, relativePath);

    try {
      // Create parent directories if they don't exist
      Files.createDirectories(targetPath.getParent());

      // Copy file to target location
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

      log.info("Successfully stored customer invoice PDF at: {}", relativePath);
      return relativePath;

    } catch (IOException e) {
      log.error(
          "Failed to store customer invoice PDF for claim {} and invoice {}",
          claimId,
          invoiceId,
          e);
      throw new RuntimeException("Failed to store file", e);
    }
  }

  @Override
  public byte[] retrieveFile(String filePath) {
    log.info("Retrieving file: {}", filePath);

    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty");
    }

    Path targetPath = Paths.get(uploadDir, filePath);

    if (!Files.exists(targetPath)) {
      log.warn("File not found: {}", filePath);
      throw new ResourceNotFoundException("File not found: " + filePath);
    }

    try {
      byte[] fileContent = Files.readAllBytes(targetPath);
      log.info("Successfully retrieved file: {} (size: {} bytes)", filePath, fileContent.length);
      return fileContent;

    } catch (IOException e) {
      log.error("Failed to retrieve file: {}", filePath, e);
      throw new RuntimeException("Failed to retrieve file", e);
    }
  }

  @Override
  public void deleteFile(String filePath) {
    log.info("Deleting file: {}", filePath);

    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty");
    }

    Path targetPath = Paths.get(uploadDir, filePath);

    if (!Files.exists(targetPath)) {
      log.warn("File not found for deletion: {}", filePath);
      return; // File doesn't exist, consider it already deleted
    }

    try {
      Files.delete(targetPath);
      log.info("Successfully deleted file: {}", filePath);

    } catch (IOException e) {
      log.error("Failed to delete file: {}", filePath, e);
      throw new RuntimeException("Failed to delete file", e);
    }
  }

  @Override
  public boolean isValidPdf(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return false;
    }

    // Check file extension
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
      return false;
    }

    // Check MIME type
    String contentType = file.getContentType();
    if (contentType == null || !contentType.equals("application/pdf")) {
      return false;
    }

    // Additional validation: Check PDF header (optional but recommended)
    try {
      byte[] header = new byte[4];
      int bytesRead = file.getInputStream().read(header);

      if (bytesRead >= 4) {
        String headerString = new String(header);
        return headerString.equals("%PDF");
      }

    } catch (IOException e) {
      log.warn("Failed to read file header for PDF validation", e);
      return false;
    }

    return true;
  }

  /** Creates necessary directories for file storage. */
  private void createDirectories() {
    try {
      Path uploadPath = Paths.get(uploadDir);
      Path adjusterPath = Paths.get(adjusterReportsDir);
      Path customerPath = Paths.get(customerInvoicesDir);

      Files.createDirectories(uploadPath);
      Files.createDirectories(adjusterPath);
      Files.createDirectories(customerPath);

      log.info("Created upload directories: {}", uploadDir);

    } catch (IOException e) {
      log.error("Failed to create upload directories", e);
      throw new RuntimeException("Failed to create upload directories", e);
    }
  }

  /** Generates a unique file name while preserving the original extension, ensuring it is safe. */
  private String generateFileName(String originalFilename) {
    if (originalFilename == null || originalFilename.trim().isEmpty()) {
      return UUID.randomUUID() + ".pdf";
    }

    String extension = "";
    int lastDotIndex = originalFilename.lastIndexOf('.');
    if (lastDotIndex > 0) {
      // Get extension including leading dot
      extension = originalFilename.substring(lastDotIndex);
      // Only allow extensions with alphanumeric characters, dot, and length up to 8
      if (!extension.matches("\\.[A-Za-z0-9]{1,8}")) {
        extension = ".pdf";
      }
    } else {
      extension = ".pdf";
    }

    return UUID.randomUUID() + extension;
  }
}
