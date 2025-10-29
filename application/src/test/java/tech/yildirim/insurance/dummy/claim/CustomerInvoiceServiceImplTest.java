package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
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
import tech.yildirim.insurance.api.generated.model.CustomerInvoiceDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Invoice Service Unit Tests")
class CustomerInvoiceServiceImplTest {
  @Mock private CustomerInvoiceRepository customerInvoiceRepository;
  @Mock private ClaimRepository claimRepository;
  @Mock private CustomerInvoiceMapper customerInvoiceMapper;
  @Mock private ClaimStorageService claimStorageService;

  @InjectMocks private CustomerInvoiceServiceImpl customerInvoiceService;

  private Long claimId;
  private Long invoiceId;
  private Claim testClaim;
  private CustomerInvoice testInvoice;
  private CustomerInvoiceDto testInvoiceDto;

  @BeforeEach
  void setUp() {
    claimId = 1L;
    invoiceId = 2L;

    // Setup test claim
    testClaim = new AutoClaim();
    testClaim.setId(claimId);
    testClaim.setClaimNumber("CLM-TEST-001");

    // Setup test customer invoice
    testInvoice = new CustomerInvoice();
    testInvoice.setId(invoiceId);
    testInvoice.setClaim(testClaim);
    testInvoice.setInvoiceAmount(BigDecimal.valueOf(500.00));
    testInvoice.setDescription("Medical expenses");
    testInvoice.setUploadedAt(ZonedDateTime.now());

    // Setup test DTO (no invoiceNumber field)
    testInvoiceDto =
        new CustomerInvoiceDto()
            .claimId(claimId)
            .invoiceAmount(BigDecimal.valueOf(500.00))
            .description("Medical expenses");
  }

  // ========== Create Customer Invoice Tests ==========

  @Test
  @DisplayName("Should create customer invoice successfully with PDF")
  void createCustomerInvoice_withValidPdf_shouldCreateSuccessfully() {
    // Given
    MultipartFile testPdfFile = createMockPdfFile();

    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(claimStorageService.isValidPdf(testPdfFile)).thenReturn(true);
    when(customerInvoiceMapper.toEntity(testInvoiceDto)).thenReturn(testInvoice);
    when(customerInvoiceRepository.save(any(CustomerInvoice.class))).thenReturn(testInvoice);
    when(claimStorageService.storeCustomerInvoicePdf(claimId, invoiceId, testPdfFile))
        .thenReturn("customer-invoices/claim-1/invoice-2/test.pdf");
    when(customerInvoiceMapper.toDto(testInvoice)).thenReturn(testInvoiceDto);

    // When
    CustomerInvoiceDto result =
        customerInvoiceService.createCustomerInvoice(claimId, testInvoiceDto, testPdfFile);

    // Then
    assertThat(result).isEqualTo(testInvoiceDto);
    verify(claimRepository).findById(claimId);
    verify(claimStorageService).isValidPdf(testPdfFile);
    verify(claimStorageService).storeCustomerInvoicePdf(claimId, invoiceId, testPdfFile);
    verify(customerInvoiceRepository, times(2)).save(any(CustomerInvoice.class));

    ArgumentCaptor<CustomerInvoice> invoiceCaptor = ArgumentCaptor.forClass(CustomerInvoice.class);
    verify(customerInvoiceRepository, times(2)).save(invoiceCaptor.capture());

    CustomerInvoice savedInvoice = invoiceCaptor.getValue();
    assertThat(savedInvoice.getClaim()).isEqualTo(testClaim);
  }

  @Test
  @DisplayName("Should throw exception when claim not found")
  void createCustomerInvoice_withNonExistentClaim_shouldThrowException() {
    // Given
    MultipartFile testPdfFile = createMockPdfFile();
    when(claimRepository.findById(claimId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(
            () ->
                customerInvoiceService.createCustomerInvoice(claimId, testInvoiceDto, testPdfFile))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Claim not found with id: " + claimId);

    verify(claimStorageService, never()).isValidPdf(any());
    verify(customerInvoiceRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when PDF file is null")
  void createCustomerInvoice_withNullPdf_shouldThrowException() {
    // Given
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));

    // When & Then
    assertThatThrownBy(
            () -> customerInvoiceService.createCustomerInvoice(claimId, testInvoiceDto, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("PDF file is required for customer invoices");

    verify(claimStorageService, never()).isValidPdf(any());
    verify(customerInvoiceRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when PDF file is empty")
  void createCustomerInvoice_withEmptyPdf_shouldThrowException() {
    // Given
    MultipartFile emptyPdfFile = mock(MultipartFile.class);
    when(emptyPdfFile.isEmpty()).thenReturn(true);
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));

    // When & Then
    assertThatThrownBy(
            () ->
                customerInvoiceService.createCustomerInvoice(claimId, testInvoiceDto, emptyPdfFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("PDF file is required for customer invoices");

    verify(claimStorageService, never()).isValidPdf(any());
    verify(customerInvoiceRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when PDF file is invalid")
  void createCustomerInvoice_withInvalidPdf_shouldThrowException() {
    // Given
    MultipartFile testPdfFile = createMockPdfFile();
    when(claimRepository.findById(claimId)).thenReturn(Optional.of(testClaim));
    when(claimStorageService.isValidPdf(testPdfFile)).thenReturn(false);

    // When & Then
    assertThatThrownBy(
            () ->
                customerInvoiceService.createCustomerInvoice(claimId, testInvoiceDto, testPdfFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File must be a valid PDF");

    verify(customerInvoiceRepository, never()).save(any());
  }

  // ========== Find Customer Invoices Tests ==========

  @Test
  @DisplayName("Should find customer invoices by claim ID successfully")
  void findCustomerInvoicesByClaimId_withExistingClaim_shouldReturnInvoices() {
    // Given
    List<CustomerInvoice> invoices = Collections.singletonList(testInvoice);
    List<CustomerInvoiceDto> invoiceDtos = Collections.singletonList(testInvoiceDto);

    when(claimRepository.existsById(claimId)).thenReturn(true);
    when(customerInvoiceRepository.findByClaimId(claimId)).thenReturn(invoices);
    when(customerInvoiceMapper.toDtoList(invoices)).thenReturn(invoiceDtos);

    // When
    List<CustomerInvoiceDto> result = customerInvoiceService.findCustomerInvoicesByClaimId(claimId);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(testInvoiceDto);
    verify(claimRepository).existsById(claimId);
    verify(customerInvoiceRepository).findByClaimId(claimId);
  }

  @Test
  @DisplayName("Should throw exception when finding invoices for non-existent claim")
  void findCustomerInvoicesByClaimId_withNonExistentClaim_shouldThrowException() {
    // Given
    when(claimRepository.existsById(claimId)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> customerInvoiceService.findCustomerInvoicesByClaimId(claimId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Claim not found with id: " + claimId);

    verify(customerInvoiceRepository, never()).findByClaimId(anyLong());
  }

  // ========== Find Specific Invoice Tests ==========

  @Test
  @DisplayName("Should find specific customer invoice successfully")
  void findCustomerInvoiceByClaimIdAndInvoiceId_withExistingInvoice_shouldReturnInvoice() {
    // Given
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(testInvoice);
    when(customerInvoiceMapper.toDto(testInvoice)).thenReturn(testInvoiceDto);

    // When
    Optional<CustomerInvoiceDto> result =
        customerInvoiceService.findCustomerInvoiceByClaimIdAndInvoiceId(claimId, invoiceId);

    // Then
    assertThat(result).isPresent();
    assertThat(result).contains(testInvoiceDto);
  }

  @Test
  @DisplayName("Should return empty when invoice not found")
  void findCustomerInvoiceByClaimIdAndInvoiceId_withNonExistentInvoice_shouldReturnEmpty() {
    // Given
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(null);

    // When
    Optional<CustomerInvoiceDto> result =
        customerInvoiceService.findCustomerInvoiceByClaimIdAndInvoiceId(claimId, invoiceId);

    // Then
    assertThat(result).isEmpty();
  }

  // ========== Update Invoice Tests ==========

  @Test
  @DisplayName("Should update customer invoice successfully")
  void updateCustomerInvoice_withExistingInvoice_shouldUpdateSuccessfully() {
    // Given
    CustomerInvoiceDto updateDto =
        new CustomerInvoiceDto()
            .invoiceAmount(BigDecimal.valueOf(600.00))
            .description("Updated medical expenses");

    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(testInvoice);
    when(customerInvoiceRepository.save(testInvoice)).thenReturn(testInvoice);
    when(customerInvoiceMapper.toDto(testInvoice)).thenReturn(testInvoiceDto);

    // When
    CustomerInvoiceDto result =
        customerInvoiceService.updateCustomerInvoice(claimId, invoiceId, updateDto);

    // Then
    assertThat(result).isEqualTo(testInvoiceDto);
    verify(customerInvoiceMapper).updateCustomerInvoiceFromDto(updateDto, testInvoice);
    verify(customerInvoiceRepository).save(testInvoice);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent invoice")
  void updateCustomerInvoice_withNonExistentInvoice_shouldThrowException() {
    // Given
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(null);

    // When & Then
    assertThatThrownBy(
            () -> customerInvoiceService.updateCustomerInvoice(claimId, invoiceId, testInvoiceDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(
            "Customer invoice not found with id: " + invoiceId + " for claim: " + claimId);

    verify(customerInvoiceRepository, never()).save(any());
  }

  // ========== Delete Invoice Tests ==========

  @Test
  @DisplayName("Should delete customer invoice successfully")
  void deleteCustomerInvoice_withExistingInvoice_shouldDeleteSuccessfully() {
    // Given
    testInvoice.setInvoicePdfPath("customer-invoices/claim-1/invoice-2/test.pdf");
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(testInvoice);
    doNothing().when(claimStorageService).deleteFile(anyString());

    // When
    customerInvoiceService.deleteCustomerInvoice(claimId, invoiceId);

    // Then
    verify(claimStorageService).deleteFile("customer-invoices/claim-1/invoice-2/test.pdf");
    verify(customerInvoiceRepository).delete(testInvoice);
  }

  @Test
  @DisplayName("Should delete invoice without PDF file successfully")
  void deleteCustomerInvoice_withoutPdfFile_shouldDeleteSuccessfully() {
    // Given
    testInvoice.setInvoicePdfPath(null);
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(testInvoice);

    // When
    customerInvoiceService.deleteCustomerInvoice(claimId, invoiceId);

    // Then
    verify(claimStorageService, never()).deleteFile(anyString());
    verify(customerInvoiceRepository).delete(testInvoice);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent invoice")
  void deleteCustomerInvoice_withNonExistentInvoice_shouldThrowException() {
    // Given
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(null);

    // When & Then
    assertThatThrownBy(() -> customerInvoiceService.deleteCustomerInvoice(claimId, invoiceId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(
            "Customer invoice not found with id: " + invoiceId + " for claim: " + claimId);

    verify(customerInvoiceRepository, never()).delete(any());
  }

  // ========== Download PDF Tests ==========

  @Test
  @DisplayName("Should download customer invoice PDF successfully")
  void downloadCustomerInvoicePdf_withExistingFile_shouldReturnPdfContent() {
    // Given
    byte[] expectedContent = "PDF content".getBytes();
    testInvoice.setInvoicePdfPath("customer-invoices/claim-1/invoice-2/test.pdf");

    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(testInvoice);
    when(claimStorageService.retrieveFile("customer-invoices/claim-1/invoice-2/test.pdf"))
        .thenReturn(expectedContent);

    // When
    byte[] result = customerInvoiceService.downloadCustomerInvoicePdf(claimId, invoiceId);

    // Then
    assertThat(result).isEqualTo(expectedContent);
    verify(claimStorageService).retrieveFile("customer-invoices/claim-1/invoice-2/test.pdf");
  }

  @Test
  @DisplayName("Should throw exception when downloading PDF for non-existent invoice")
  void downloadCustomerInvoicePdf_withNonExistentInvoice_shouldThrowException() {
    // Given
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(null);

    // When & Then
    assertThatThrownBy(() -> customerInvoiceService.downloadCustomerInvoicePdf(claimId, invoiceId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(
            "Customer invoice not found with id: " + invoiceId + " for claim: " + claimId);

    verify(claimStorageService, never()).retrieveFile(anyString());
  }

  @Test
  @DisplayName("Should throw exception when downloading PDF for invoice without file")
  void downloadCustomerInvoicePdf_withInvoiceWithoutPdf_shouldThrowException() {
    // Given
    testInvoice.setInvoicePdfPath(null);
    when(customerInvoiceRepository.findByClaimIdAndId(claimId, invoiceId)).thenReturn(testInvoice);

    // When & Then
    assertThatThrownBy(() -> customerInvoiceService.downloadCustomerInvoicePdf(claimId, invoiceId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No PDF file found for customer invoice: " + invoiceId);

    verify(claimStorageService, never()).retrieveFile(anyString());
  }

  // ========== Helper Methods ==========

  private MultipartFile createMockPdfFile() {
    MultipartFile mockFile = mock(MultipartFile.class);
    lenient().when(mockFile.isEmpty()).thenReturn(false);
    lenient().when(mockFile.getOriginalFilename()).thenReturn("invoice.pdf");
    return mockFile;
  }
}
