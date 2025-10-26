package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.api.generated.model.CustomerInvoiceDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

/** Implementation of {@link CustomerInvoiceService}. */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerInvoiceServiceImpl implements CustomerInvoiceService {

  private final CustomerInvoiceRepository customerInvoiceRepository;
  private final ClaimRepository claimRepository;
  private final CustomerInvoiceMapper customerInvoiceMapper;
  private final ClaimStorageService claimStorageService;

  @Override
  @Transactional
  public CustomerInvoiceDto createCustomerInvoice(
      Long claimId, CustomerInvoiceDto customerInvoiceDto, MultipartFile pdfFile) {
    log.info("Creating customer invoice for claim {}", claimId);

    // Validate claim exists
    Claim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Claim not found with id: " + claimId));

    // Validate PDF file is provided (required for customer invoices)
    if (pdfFile == null || pdfFile.isEmpty()) {
      throw new IllegalArgumentException("PDF file is required for customer invoices");
    }

    if (!claimStorageService.isValidPdf(pdfFile)) {
      throw new IllegalArgumentException("File must be a valid PDF");
    }

    // Create and populate customer invoice
    CustomerInvoice customerInvoice = customerInvoiceMapper.toEntity(customerInvoiceDto);
    customerInvoice.setClaim(claim);

    // Save the invoice first to get an ID
    CustomerInvoice savedInvoice = customerInvoiceRepository.save(customerInvoice);

    // Store PDF file
    String filePath =
        claimStorageService.storeCustomerInvoicePdf(claimId, savedInvoice.getId(), pdfFile);
    savedInvoice.setInvoicePdfPath(filePath);
    savedInvoice.setOriginalPdfFilename(pdfFile.getOriginalFilename());

    // Save again with file path
    savedInvoice = customerInvoiceRepository.save(savedInvoice);

    log.info("Successfully created customer invoice with id {}", savedInvoice.getId());
    return customerInvoiceMapper.toDto(savedInvoice);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerInvoiceDto> findCustomerInvoicesByClaimId(Long claimId) {
    log.info("Finding customer invoices for claim {}", claimId);

    // Validate claim exists
    if (!claimRepository.existsById(claimId)) {
      throw new ResourceNotFoundException("Claim not found with id: " + claimId);
    }

    List<CustomerInvoice> invoices = customerInvoiceRepository.findByClaimId(claimId);
    log.info("Found {} customer invoices for claim {}", invoices.size(), claimId);

    return customerInvoiceMapper.toDtoList(invoices);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerInvoiceDto> findCustomerInvoiceByClaimIdAndInvoiceId(
      Long claimId, Long invoiceId) {
    log.info("Finding customer invoice {} for claim {}", invoiceId, claimId);

    CustomerInvoice invoice = customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId);
    if (invoice == null) {
      log.warn("Customer invoice {} not found for claim {}", invoiceId, claimId);
      return Optional.empty();
    }

    return Optional.of(customerInvoiceMapper.toDto(invoice));
  }

  @Override
  @Transactional
  public CustomerInvoiceDto updateCustomerInvoice(
      Long claimId, Long invoiceId, CustomerInvoiceDto customerInvoiceDto) {
    log.info("Updating customer invoice {} for claim {}", invoiceId, claimId);

    CustomerInvoice existingInvoice =
        customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId);
    if (existingInvoice == null) {
      throw new ResourceNotFoundException(
          "Customer invoice not found with id: " + invoiceId + " for claim: " + claimId);
    }

    // Update the existing invoice (but not the file paths)
    customerInvoiceMapper.updateCustomerInvoiceFromDto(customerInvoiceDto, existingInvoice);

    CustomerInvoice updatedInvoice = customerInvoiceRepository.save(existingInvoice);
    log.info("Successfully updated customer invoice {}", invoiceId);

    return customerInvoiceMapper.toDto(updatedInvoice);
  }

  @Override
  @Transactional
  public void deleteCustomerInvoice(Long claimId, Long invoiceId) {
    log.info("Deleting customer invoice {} for claim {}", invoiceId, claimId);

    CustomerInvoice invoice = customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId);
    if (invoice == null) {
      throw new ResourceNotFoundException(
          "Customer invoice not found with id: " + invoiceId + " for claim: " + claimId);
    }

    // Delete PDF file if exists
    if (invoice.getInvoicePdfPath() != null) {
      try {
        claimStorageService.deleteFile(invoice.getInvoicePdfPath());
      } catch (Exception e) {
        log.warn(
            "Failed to delete PDF file for customer invoice {}: {}", invoiceId, e.getMessage());
      }
    }

    customerInvoiceRepository.delete(invoice);
    log.info("Successfully deleted customer invoice {}", invoiceId);
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] downloadCustomerInvoicePdf(Long claimId, Long invoiceId) {
    log.info("Downloading PDF for customer invoice {} in claim {}", invoiceId, claimId);

    CustomerInvoice invoice = customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId);
    if (invoice == null) {
      throw new ResourceNotFoundException(
          "Customer invoice not found with id: " + invoiceId + " for claim: " + claimId);
    }

    if (invoice.getInvoicePdfPath() == null) {
      throw new ResourceNotFoundException("No PDF file found for customer invoice: " + invoiceId);
    }

    byte[] pdfContent = claimStorageService.retrieveFile(invoice.getInvoicePdfPath());
    log.info("Successfully retrieved PDF for customer invoice {}", invoiceId);

    return pdfContent;
  }
}
