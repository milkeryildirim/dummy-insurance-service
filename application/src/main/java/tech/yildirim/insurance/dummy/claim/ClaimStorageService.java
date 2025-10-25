package tech.yildirim.insurance.dummy.claim;

import org.springframework.web.multipart.MultipartFile;

/** Service Interface for managing file storage operations. */
public interface ClaimStorageService {

  /**
   * Stores a PDF file for an adjuster report.
   *
   * @param claimId The ID of the claim.
   * @param reportId The ID of the adjuster report.
   * @param file The PDF file to store.
   * @return The file path where the file was stored.
   */
  String storeAdjusterReportPdf(Long claimId, Long reportId, MultipartFile file);

  /**
   * Stores a PDF file for a customer invoice.
   *
   * @param claimId The ID of the claim.
   * @param invoiceId The ID of the customer invoice.
   * @param file The PDF file to store.
   * @return The file path where the file was stored.
   */
  String storeCustomerInvoicePdf(Long claimId, Long invoiceId, MultipartFile file);

  /**
   * Retrieves a file as byte array.
   *
   * @param filePath The path to the file.
   * @return The file content as byte array.
   */
  byte[] retrieveFile(String filePath);

  /**
   * Deletes a file from storage.
   *
   * @param filePath The path to the file to delete.
   */
  void deleteFile(String filePath);

  /**
   * Validates if the uploaded file is a valid PDF.
   *
   * @param file The file to validate.
   * @return true if the file is a valid PDF, false otherwise.
   */
  boolean isValidPdf(MultipartFile file);
}
