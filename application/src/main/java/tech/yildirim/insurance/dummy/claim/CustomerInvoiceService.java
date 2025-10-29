package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.api.generated.model.CustomerInvoiceDto;

/** Service Interface for managing {@link CustomerInvoice}. */
public interface CustomerInvoiceService {

  /**
   * Creates a new customer invoice for a claim.
   *
   * @param claimId The ID of the claim.
   * @param customerInvoiceDto The DTO containing the invoice data.
   * @param pdfFile The PDF file to upload.
   * @return The created customer invoice DTO.
   */
  CustomerInvoiceDto createCustomerInvoice(
      Long claimId, CustomerInvoiceDto customerInvoiceDto, MultipartFile pdfFile);

  /**
   * Retrieves all customer invoices for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return A list of customer invoice DTOs.
   */
  List<CustomerInvoiceDto> findCustomerInvoicesByClaimId(Long claimId);

  /**
   * Finds a specific customer invoice by claim ID and invoice ID.
   *
   * @param claimId The ID of the claim.
   * @param invoiceId The ID of the invoice.
   * @return An Optional containing the found customer invoice DTO, or empty if not found.
   */
  Optional<CustomerInvoiceDto> findCustomerInvoiceByClaimIdAndInvoiceId(
      Long claimId, Long invoiceId);

  /**
   * Updates an existing customer invoice.
   *
   * @param claimId The ID of the claim.
   * @param invoiceId The ID of the invoice.
   * @param customerInvoiceDto The DTO containing the updated invoice data.
   * @return The updated customer invoice DTO.
   */
  CustomerInvoiceDto updateCustomerInvoice(
      Long claimId, Long invoiceId, CustomerInvoiceDto customerInvoiceDto);

  /**
   * Deletes a customer invoice.
   *
   * @param claimId The ID of the claim.
   * @param invoiceId The ID of the invoice.
   */
  void deleteCustomerInvoice(Long claimId, Long invoiceId);

  /**
   * Downloads the PDF file for a customer invoice.
   *
   * @param claimId The ID of the claim.
   * @param invoiceId The ID of the invoice.
   * @return The PDF file as byte array.
   */
  byte[] downloadCustomerInvoicePdf(Long claimId, Long invoiceId);
}
