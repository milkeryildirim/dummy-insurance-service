package tech.yildirim.insurance.dummy.claim;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tech.yildirim.insurance.api.generated.model.AdjusterReportDto;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDecisionDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
import tech.yildirim.insurance.api.generated.model.CustomerInvoiceDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;

@WebMvcTest(ClaimsController.class)
@DisplayName("Auto Claims Controller Web Layer Tests")
class ClaimsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ClaimService claimService;
  @Autowired private AdjusterReportService adjusterReportService;
  @Autowired private CustomerInvoiceService customerInvoiceService;
  @Autowired private ClaimDecisionService claimDecisionService;
  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public ClaimService claimService() {
      return Mockito.mock(ClaimService.class);
    }

    @Bean
    public AdjusterReportService adjusterReportService() {
      return Mockito.mock(AdjusterReportService.class);
    }

    @Bean
    public CustomerInvoiceService customerInvoiceService() {
      return Mockito.mock(CustomerInvoiceService.class);
    }

    @Bean
    public ClaimDecisionService claimDecisionService() {
      return Mockito.mock(ClaimDecisionService.class);
    }
  }

  // ========== AUTO CLAIM BASIC OPERATIONS ==========

  @Test
  @DisplayName("POST /claims/auto - Should create auto claim and return 201 Created")
  void createAutoClaim_withValidData_shouldReturn201() throws Exception {
    AutoClaimDto inputDto =
        new AutoClaimDto()
            .licensePlate("ABC-123")
            .vehicleVin("1HGCM82633A123456")
            .accidentLocation("Main Street Intersection")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO)
            .policyId(1L)
            .description("Rear-end collision at traffic light")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(5000.00));

    AutoClaimDto outputDto =
        new AutoClaimDto()
            .id(100L)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .vehicleVin("1HGCM82633A123456")
            .accidentLocation("Main Street Intersection")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO)
            .policyId(1L)
            .description("Rear-end collision at traffic light")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(5000.00))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.submitClaim(eq(1L), any(AutoClaimDto.class))).thenReturn(outputDto);

    mockMvc
        .perform(
            post("/claims/auto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("AC-2025-001")))
        .andExpect(jsonPath("$.licensePlate", is("ABC-123")))
        .andExpect(jsonPath("$.status", is("SUBMITTED")));
  }

  @Test
  @DisplayName("GET /claims/auto/{id} - Should return auto claim when claim exists")
  void getAutoClaimById_whenExists_shouldReturnAutoClaim() throws Exception {
    long claimId = 100L;
    AutoClaimDto autoClaimDto =
        new AutoClaimDto()
            .id(claimId)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .vehicleVin("1HGCM82633A123456")
            .accidentLocation("Main Street Intersection")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO)
            .policyId(1L)
            .description("Rear-end collision at traffic light")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(autoClaimDto));

    mockMvc
        .perform(get("/claims/auto/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("AC-2025-001")))
        .andExpect(jsonPath("$.licensePlate", is("ABC-123")))
        .andExpect(jsonPath("$.claimType", is(ClaimTypeEnum.AUTO_CLAIM_DTO.toString())));
  }

  @Test
  @DisplayName("GET /claims/auto/{id} - Should return 404 Not Found when claim does not exist")
  void getAutoClaimById_whenNotExists_shouldReturnNotFound() throws Exception {
    when(claimService.findClaimById(anyLong())).thenReturn(Optional.empty());
    mockMvc.perform(get("/claims/auto/{id}", 999L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /claims/auto - Should return list of auto claims")
  void getAllAutoClaims_shouldReturnAutoClaimsList() throws Exception {
    AutoClaimDto claim1 =
        new AutoClaimDto()
            .id(100L)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    AutoClaimDto claim2 =
        new AutoClaimDto()
            .id(101L)
            .claimNumber("AC-2025-002")
            .licensePlate("XYZ-789")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    List<ClaimDto> claims = List.of(claim1, claim2);
    when(claimService.getAllClaimsByType(ClaimTypeEnum.AUTO_CLAIM_DTO)).thenReturn(claims);

    mockMvc
        .perform(get("/claims/auto"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].id", is(100)))
        .andExpect(jsonPath("$[0].claimNumber", is("AC-2025-001")))
        .andExpect(jsonPath("$[1].id", is(101)))
        .andExpect(jsonPath("$[1].claimNumber", is("AC-2025-002")));
  }

  @Test
  @DisplayName("PUT /claims/auto/{id} - Should update auto claim and return updated claim")
  void updateAutoClaim_whenExists_shouldReturnUpdatedClaim() throws Exception {
    long claimId = 100L;
    AutoClaimDto updateDto =
        new AutoClaimDto()
            .policyId(1L)
            .vehicleVin("1HGCM82633A123456")
            .licensePlate("ABC-123")
            .accidentLocation("Updated location")
            .description("Updated description")
            .estimatedAmount(BigDecimal.valueOf(7500.00))
            .dateOfIncident(LocalDate.of(2025, 8, 15));

    AutoClaimDto updatedDto =
        new AutoClaimDto()
            .id(claimId)
            .policyId(1L)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .vehicleVin("1HGCM82633A123456")
            .accidentLocation("Updated location")
            .description("Updated description")
            .estimatedAmount(BigDecimal.valueOf(7500.00))
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .dateOfIncident(LocalDate.of(2025, 8, 15));

    when(claimService.updateClaim(eq(claimId), any(AutoClaimDto.class))).thenReturn(updatedDto);

    mockMvc
        .perform(
            put("/claims/auto/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.accidentLocation", is("Updated location")))
        .andExpect(jsonPath("$.description", is("Updated description")));
  }

  @Test
  @DisplayName("DELETE /claims/auto/{id} - Should delete auto claim and return 204 No Content")
  void deleteAutoClaim_whenExists_shouldReturnNoContent() throws Exception {
    long claimId = 100L;
    AutoClaimDto existingClaim =
        new AutoClaimDto()
            .id(claimId)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doNothing().when(claimService).deleteClaim(claimId);

    mockMvc.perform(delete("/claims/auto/{id}", claimId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "PUT /claims/auto/{id}/assign-adjuster - Should assign adjuster and return updated claim")
  void assignAdjusterToAutoClaim_whenValidRequest_shouldReturnUpdatedClaim() throws Exception {
    long claimId = 100L;
    long employeeId = 50L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    AutoClaimDto updatedClaim =
        new AutoClaimDto()
            .id(claimId)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .assignedAdjusterId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId)).thenReturn(updatedClaim);

    mockMvc
        .perform(
            put("/claims/auto/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.assignedAdjusterId", is(50)))
        .andExpect(jsonPath("$.status", is("IN_REVIEW")));
  }

  // ========== AUTO CLAIM ADJUSTER REPORTS ==========

  @Test
  @DisplayName("GET /claims/auto/{id}/adjuster-reports - Should return list of adjuster reports")
  void getAutoClaimAdjusterReports_shouldReturnReportsList() throws Exception {
    long claimId = 100L;
    AdjusterReportDto report1 =
        new AdjusterReportDto()
            .id(1L)
            .claimId(claimId)
            .adjusterId(20L)
            .summary("Initial damage assessment")
            .findings("Minor front bumper damage")
            .recommendedAmount(BigDecimal.valueOf(1200.00))
            .status(AdjusterReportDto.StatusEnum.DRAFT);

    AdjusterReportDto report2 =
        new AdjusterReportDto()
            .id(2L)
            .claimId(claimId)
            .adjusterId(21L)
            .summary("Final assessment")
            .findings("Comprehensive damage report")
            .recommendedAmount(BigDecimal.valueOf(1500.00))
            .status(AdjusterReportDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(adjusterReportService.findAdjusterReportsByClaimId(claimId))
        .thenReturn(List.of(report1, report2));

    mockMvc
        .perform(get("/claims/auto/{id}/adjuster-reports", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].summary", is("Initial damage assessment")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].summary", is("Final assessment")));
  }

  @Test
  @DisplayName("POST /claims/auto/{id}/adjuster-reports - Should create adjuster report with PDF")
  void createAutoClaimAdjusterReport_withPdf_shouldReturnCreatedReport() throws Exception {
    long claimId = 100L;
    AdjusterReportDto reportDto =
        new AdjusterReportDto()
            .claimId(claimId)
            .adjusterId(20L)
            .summary("Vehicle damage assessment")
            .findings("Front bumper requires replacement")
            .recommendations("Approve repair costs")
            .status(AdjusterReportDto.StatusEnum.DRAFT)
            .recommendedAmount(BigDecimal.valueOf(1250.00));

    AdjusterReportDto createdReport =
        new AdjusterReportDto()
            .id(1L)
            .claimId(claimId)
            .adjusterId(20L)
            .summary("Vehicle damage assessment")
            .findings("Front bumper requires replacement")
            .recommendations("Approve repair costs")
            .recommendedAmount(BigDecimal.valueOf(1250.00))
            .status(AdjusterReportDto.StatusEnum.DRAFT)
            .createdAt(ZonedDateTime.now().toOffsetDateTime());

    MockMultipartFile pdfFile =
        new MockMultipartFile("pdfFile", "report.pdf", "application/pdf", "PDF content".getBytes());

    MockMultipartFile reportJson =
        new MockMultipartFile(
            "report",
            "",
            "application/json",
            objectMapper.writeValueAsString(reportDto).getBytes());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canAddAdjusterReports(claimId)).thenReturn(true);
    when(adjusterReportService.createAdjusterReport(
            eq(claimId), any(AdjusterReportDto.class), any()))
        .thenReturn(createdReport);

    mockMvc
        .perform(
            multipart("/claims/auto/{id}/adjuster-reports", claimId)
                .file(reportJson)
                .file(pdfFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.summary", is("Vehicle damage assessment")))
        .andExpect(jsonPath("$.recommendedAmount", is(1250.00)));
  }

  @Test
  @DisplayName(
      "GET /claims/auto/{id}/adjuster-reports/{reportId} - Should return specific adjuster report")
  void getAutoClaimAdjusterReportById_shouldReturnReport() throws Exception {
    long claimId = 100L;
    long reportId = 1L;
    AdjusterReportDto report =
        new AdjusterReportDto()
            .id(reportId)
            .claimId(claimId)
            .adjusterId(20L)
            .summary("Vehicle damage assessment")
            .findings("Front bumper requires replacement")
            .recommendedAmount(BigDecimal.valueOf(1250.00))
            .status(AdjusterReportDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(adjusterReportService.findAdjusterReportByClaimIdAndReportId(claimId, reportId))
        .thenReturn(Optional.of(report));

    mockMvc
        .perform(get("/claims/auto/{id}/adjuster-reports/{reportId}", claimId, reportId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.summary", is("Vehicle damage assessment")))
        .andExpect(jsonPath("$.recommendedAmount", is(1250.00)));
  }

  @Test
  @DisplayName("PUT /claims/auto/{id}/adjuster-reports/{reportId} - Should update adjuster report")
  void updateAutoClaimAdjusterReport_shouldReturnUpdatedReport() throws Exception {
    long claimId = 100L;
    long reportId = 1L;
    AdjusterReportDto updateDto =
        new AdjusterReportDto()
            .claimId(claimId)
            .adjusterId(20L)
            .status(AdjusterReportDto.StatusEnum.DRAFT)
            .summary("Updated assessment")
            .findings("Updated findings")
            .recommendedAmount(BigDecimal.valueOf(1400.00));

    AdjusterReportDto updatedReport =
        new AdjusterReportDto()
            .id(reportId)
            .claimId(claimId)
            .adjusterId(20L)
            .summary("Updated assessment")
            .findings("Updated findings")
            .recommendedAmount(BigDecimal.valueOf(1400.00))
            .status(AdjusterReportDto.StatusEnum.DRAFT);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(adjusterReportService.updateAdjusterReport(claimId, reportId, updateDto))
        .thenReturn(updatedReport);

    mockMvc
        .perform(
            put("/claims/auto/{id}/adjuster-reports/{reportId}", claimId, reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summary", is("Updated assessment")))
        .andExpect(jsonPath("$.recommendedAmount", is(1400.00)));
  }

  @Test
  @DisplayName(
      "DELETE /claims/auto/{id}/adjuster-reports/{reportId} - Should delete adjuster report")
  void deleteAutoClaimAdjusterReport_shouldReturnNoContent() throws Exception {
    long claimId = 100L;
    long reportId = 1L;

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    doNothing().when(adjusterReportService).deleteAdjusterReport(claimId, reportId);

    mockMvc
        .perform(delete("/claims/auto/{id}/adjuster-reports/{reportId}", claimId, reportId))
        .andExpect(status().isNoContent());
  }

  // ========== AUTO CLAIM CUSTOMER INVOICES ==========

  @Test
  @DisplayName("GET /claims/auto/{id}/customer-invoices - Should return list of customer invoices")
  void getAutoClaimCustomerInvoices_shouldReturnInvoicesList() throws Exception {
    long claimId = 100L;
    CustomerInvoiceDto invoice1 =
        new CustomerInvoiceDto()
            .id(1L)
            .claimId(claimId)
            .vendorName("AutoShop Berlin")
            .invoiceAmount(BigDecimal.valueOf(1150.00))
            .description("Front bumper replacement")
            .invoiceDate(LocalDate.of(2025, 10, 20));

    CustomerInvoiceDto invoice2 =
        new CustomerInvoiceDto()
            .id(2L)
            .claimId(claimId)
            .vendorName("Parts Supplier")
            .invoiceAmount(BigDecimal.valueOf(350.00))
            .description("Replacement parts")
            .invoiceDate(LocalDate.of(2025, 10, 22));

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(customerInvoiceService.findCustomerInvoicesByClaimId(claimId))
        .thenReturn(List.of(invoice1, invoice2));

    mockMvc
        .perform(get("/claims/auto/{id}/customer-invoices", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].vendorName", is("AutoShop Berlin")))
        .andExpect(jsonPath("$[1].vendorName", is("Parts Supplier")));
  }

  @Test
  @DisplayName("POST /claims/auto/{id}/customer-invoices - Should create customer invoice with PDF")
  void createAutoClaimCustomerInvoice_withPdf_shouldReturnCreatedInvoice() throws Exception {
    long claimId = 100L;
    CustomerInvoiceDto invoiceDto =
        new CustomerInvoiceDto()
            .claimId(claimId)
            .vendorName("AutoShop Berlin")
            .invoiceAmount(BigDecimal.valueOf(1150.00))
            .description("Front bumper replacement")
            .invoiceDate(LocalDate.of(2025, 10, 20));

    CustomerInvoiceDto createdInvoice =
        new CustomerInvoiceDto()
            .id(1L)
            .claimId(claimId)
            .vendorName("AutoShop Berlin")
            .invoiceAmount(BigDecimal.valueOf(1150.00))
            .description("Front bumper replacement")
            .invoiceDate(LocalDate.of(2025, 10, 20))
            .uploadedAt(ZonedDateTime.now().toOffsetDateTime());

    MockMultipartFile pdfFile =
        new MockMultipartFile(
            "pdfFile", "invoice.pdf", "application/pdf", "PDF content".getBytes());

    MockMultipartFile invoiceJson =
        new MockMultipartFile(
            "invoice",
            "",
            "application/json",
            objectMapper.writeValueAsString(invoiceDto).getBytes());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canAddCustomerInvoices(claimId)).thenReturn(true);
    when(customerInvoiceService.createCustomerInvoice(
            eq(claimId), any(CustomerInvoiceDto.class), any()))
        .thenReturn(createdInvoice);

    mockMvc
        .perform(
            multipart("/claims/auto/{id}/customer-invoices", claimId)
                .file(invoiceJson)
                .file(pdfFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.vendorName", is("AutoShop Berlin")))
        .andExpect(jsonPath("$.invoiceAmount", is(1150.00)));
  }

  @Test
  @DisplayName(
      "GET /claims/auto/{id}/customer-invoices/{invoiceId} - Should return specific customer invoice")
  void getAutoClaimCustomerInvoiceById_shouldReturnInvoice() throws Exception {
    long claimId = 100L;
    long invoiceId = 1L;
    CustomerInvoiceDto invoice =
        new CustomerInvoiceDto()
            .id(invoiceId)
            .claimId(claimId)
            .vendorName("AutoShop Berlin")
            .invoiceAmount(BigDecimal.valueOf(1150.00))
            .description("Front bumper replacement")
            .invoiceDate(LocalDate.of(2025, 10, 20));

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(customerInvoiceService.findCustomerInvoiceByClaimIdAndInvoiceId(claimId, invoiceId))
        .thenReturn(Optional.of(invoice));

    mockMvc
        .perform(get("/claims/auto/{id}/customer-invoices/{invoiceId}", claimId, invoiceId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vendorName", is("AutoShop Berlin")))
        .andExpect(jsonPath("$.invoiceAmount", is(1150.00)));
  }

  @Test
  @DisplayName(
      "PUT /claims/auto/{id}/customer-invoices/{invoiceId} - Should update customer invoice")
  void updateAutoClaimCustomerInvoice_shouldReturnUpdatedInvoice() throws Exception {
    long claimId = 100L;
    long invoiceId = 1L;
    CustomerInvoiceDto updateDto =
        new CustomerInvoiceDto()
            .claimId(claimId)
            .vendorName("Updated AutoShop")
            .invoiceAmount(BigDecimal.valueOf(1250.00))
            .description("Updated description");

    CustomerInvoiceDto updatedInvoice =
        new CustomerInvoiceDto()
            .id(invoiceId)
            .claimId(claimId)
            .vendorName("Updated AutoShop")
            .invoiceAmount(BigDecimal.valueOf(1250.00))
            .description("Updated description")
            .invoiceDate(LocalDate.of(2025, 10, 20));

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(customerInvoiceService.updateCustomerInvoice(claimId, invoiceId, updateDto))
        .thenReturn(updatedInvoice);

    mockMvc
        .perform(
            put("/claims/auto/{id}/customer-invoices/{invoiceId}", claimId, invoiceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vendorName", is("Updated AutoShop")))
        .andExpect(jsonPath("$.invoiceAmount", is(1250.00)));
  }

  @Test
  @DisplayName(
      "DELETE /claims/auto/{id}/customer-invoices/{invoiceId} - Should delete customer invoice")
  void deleteAutoClaimCustomerInvoice_shouldReturnNoContent() throws Exception {
    long claimId = 100L;
    long invoiceId = 1L;

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    doNothing().when(customerInvoiceService).deleteCustomerInvoice(claimId, invoiceId);

    mockMvc
        .perform(delete("/claims/auto/{id}/customer-invoices/{invoiceId}", claimId, invoiceId))
        .andExpect(status().isNoContent());
  }

  // ========== AUTO CLAIM DECISIONS ==========

  @Test
  @DisplayName("GET /claims/auto/{id}/decision - Should return claim decision")
  void getAutoClaimDecision_shouldReturnDecision() throws Exception {
    long claimId = 100L;
    ClaimDecisionDto decision =
        new ClaimDecisionDto()
            .id(1L)
            .claimId(claimId)
            .decisionMakerId(10L)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED)
            .approvedAmount(BigDecimal.valueOf(1000.00))
            .reasoning("Claim approved based on adjuster report")
            .decisionDate(ZonedDateTime.now().toOffsetDateTime());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimDecisionService.findClaimDecisionByClaimId(claimId))
        .thenReturn(Optional.of(decision));

    mockMvc
        .perform(get("/claims/auto/{id}/decision", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decisionType", is("APPROVED")))
        .andExpect(jsonPath("$.approvedAmount", is(1000.00)));
  }

  @Test
  @DisplayName("POST /claims/auto/{id}/decision - Should create claim decision")
  void createAutoClaimDecision_shouldReturnCreatedDecision() throws Exception {
    long claimId = 100L;
    ClaimDecisionDto decisionDto =
        new ClaimDecisionDto()
            .claimId(claimId)
            .decisionMakerId(10L)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED)
            .approvedAmount(BigDecimal.valueOf(1000.00))
            .reasoning("Claim approved based on adjuster report");

    ClaimDecisionDto createdDecision =
        new ClaimDecisionDto()
            .id(1L)
            .claimId(claimId)
            .decisionMakerId(10L)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED)
            .approvedAmount(BigDecimal.valueOf(1000.00))
            .reasoning("Claim approved based on adjuster report")
            .decisionDate(ZonedDateTime.now().toOffsetDateTime());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canMakeDecision(claimId)).thenReturn(true);
    when(claimDecisionService.createClaimDecision(claimId, decisionDto))
        .thenReturn(createdDecision);

    mockMvc
        .perform(
            post("/claims/auto/{id}/decision", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.decisionType", is("APPROVED")))
        .andExpect(jsonPath("$.approvedAmount", is(1000.00)));
  }

  @Test
  @DisplayName("PUT /claims/auto/{id}/decision - Should update claim decision")
  void updateAutoClaimDecision_shouldReturnUpdatedDecision() throws Exception {
    long claimId = 100L;
    ClaimDecisionDto updateDto =
        new ClaimDecisionDto()
            .id(1L)
            .decisionMakerId(10L)
            .claimId(claimId)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.PARTIALLY_APPROVED)
            .approvedAmount(BigDecimal.valueOf(800.00))
            .reasoning("Updated reasoning");

    ClaimDecisionDto updatedDecision =
        new ClaimDecisionDto()
            .id(1L)
            .claimId(claimId)
            .decisionMakerId(10L)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.PARTIALLY_APPROVED)
            .approvedAmount(BigDecimal.valueOf(800.00))
            .reasoning("Updated reasoning")
            .decisionDate(ZonedDateTime.now().toOffsetDateTime());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimDecisionService.updateClaimDecision(claimId, updateDto)).thenReturn(updatedDecision);

    mockMvc
        .perform(
            put("/claims/auto/{id}/decision", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decisionType", is("PARTIALLY_APPROVED")))
        .andExpect(jsonPath("$.approvedAmount", is(800.00)));
  }

  // ========== HOME CLAIM BASIC OPERATIONS ==========

  @Test
  @DisplayName("POST /claims/home - Should create home claim and return 201 Created")
  void createHomeClaim_withValidData_shouldReturn201() throws Exception {
    HomeClaimDto inputDto =
        new HomeClaimDto()
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .policyId(1L)
            .description("Pipe burst in kitchen")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(15000.00));

    HomeClaimDto outputDto =
        new HomeClaimDto()
            .id(100L)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .policyId(1L)
            .description("Pipe burst in kitchen")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(15000.00))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.submitClaim(eq(1L), any(HomeClaimDto.class))).thenReturn(outputDto);

    mockMvc
        .perform(
            post("/claims/home")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HM-2025-001")))
        .andExpect(jsonPath("$.typeOfDamage", is("Water damage")));
  }

  @Test
  @DisplayName("GET /claims/home/{id} - Should return home claim when claim exists")
  void getHomeClaimById_whenExists_shouldReturnHomeClaim() throws Exception {
    long claimId = 100L;
    HomeClaimDto homeClaimDto =
        new HomeClaimDto()
            .id(claimId)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .policyId(1L)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(homeClaimDto));

    mockMvc
        .perform(get("/claims/home/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HM-2025-001")))
        .andExpect(jsonPath("$.typeOfDamage", is("Water damage")));
  }

  @Test
  @DisplayName("GET /claims/home - Should return list of home claims")
  void getAllHomeClaims_shouldReturnHomeClaimsList() throws Exception {
    HomeClaimDto claim1 =
        new HomeClaimDto()
            .id(100L)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    HomeClaimDto claim2 =
        new HomeClaimDto()
            .id(101L)
            .claimNumber("HM-2025-002")
            .typeOfDamage("Fire damage")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    List<ClaimDto> claims = List.of(claim1, claim2);
    when(claimService.getAllClaimsByType(ClaimTypeEnum.HOME_CLAIM_DTO)).thenReturn(claims);

    mockMvc
        .perform(get("/claims/home"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].typeOfDamage", is("Water damage")))
        .andExpect(jsonPath("$[1].typeOfDamage", is("Fire damage")));
  }

  @Test
  @DisplayName("PUT /claims/home/{id} - Should update home claim and return updated claim")
  void updateHomeClaim_whenExists_shouldReturnUpdatedClaim() throws Exception {
    long claimId = 100L;
    HomeClaimDto updateDto =
        new HomeClaimDto()
            .id(claimId)
            .policyId(1L)
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .typeOfDamage("Updated water damage")
            .damagedItems("Updated items")
            .description("Updated description");

    HomeClaimDto updatedDto =
        new HomeClaimDto()
            .id(claimId)
            .policyId(1L)
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .claimNumber("HM-2025-001")
            .typeOfDamage("Updated water damage")
            .damagedItems("Updated items")
            .description("Updated description")
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    when(claimService.updateClaim(eq(claimId), any(HomeClaimDto.class))).thenReturn(updatedDto);

    mockMvc
        .perform(
            put("/claims/home/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.typeOfDamage", is("Updated water damage")))
        .andExpect(jsonPath("$.description", is("Updated description")));
  }

  @Test
  @DisplayName("DELETE /claims/home/{id} - Should delete home claim and return 204 No Content")
  void deleteHomeClaim_whenExists_shouldReturnNoContent() throws Exception {
    long claimId = 100L;
    HomeClaimDto existingClaim =
        new HomeClaimDto()
            .id(claimId)
            .claimNumber("HM-2025-001")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doNothing().when(claimService).deleteClaim(claimId);

    mockMvc.perform(delete("/claims/home/{id}", claimId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /claims/home/{id}/assign-adjuster - Should assign adjuster to home claim")
  void assignAdjusterToHomeClaim_whenValidRequest_shouldReturnUpdatedClaim() throws Exception {
    long claimId = 100L;
    long employeeId = 50L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    HomeClaimDto updatedClaim =
        new HomeClaimDto()
            .id(claimId)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .assignedAdjusterId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId)).thenReturn(updatedClaim);

    mockMvc
        .perform(
            put("/claims/home/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignedAdjusterId", is(50)))
        .andExpect(jsonPath("$.status", is("IN_REVIEW")));
  }

  // ========== HEALTH CLAIM BASIC OPERATIONS ==========

  @Test
  @DisplayName("POST /claims/health - Should create health claim and return 201 Created")
  void createHealthClaim_withValidData_shouldReturn201() throws Exception {
    HealthClaimDto inputDto =
        new HealthClaimDto()
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .policyId(1L)
            .description("Medical consultation")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(250.00));

    HealthClaimDto outputDto =
        new HealthClaimDto()
            .id(100L)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .policyId(1L)
            .description("Medical consultation")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(250.00))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.submitClaim(eq(1L), any(HealthClaimDto.class))).thenReturn(outputDto);

    mockMvc
        .perform(
            post("/claims/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HC-2025-001")))
        .andExpect(jsonPath("$.medicalProvider", is("City General Hospital")));
  }

  @Test
  @DisplayName("GET /claims/health/{id} - Should return health claim when claim exists")
  void getHealthClaimById_whenExists_shouldReturnHealthClaim() throws Exception {
    long claimId = 100L;
    HealthClaimDto healthClaimDto =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .policyId(1L)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(healthClaimDto));

    mockMvc
        .perform(get("/claims/health/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HC-2025-001")))
        .andExpect(jsonPath("$.medicalProvider", is("City General Hospital")));
  }

  @Test
  @DisplayName("GET /claims/health - Should return list of health claims")
  void getAllHealthClaims_shouldReturnHealthClaimsList() throws Exception {
    HealthClaimDto claim1 =
        new HealthClaimDto()
            .id(100L)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    HealthClaimDto claim2 =
        new HealthClaimDto()
            .id(101L)
            .claimNumber("HC-2025-002")
            .medicalProvider("Regional Medical Center")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    List<ClaimDto> claims = List.of(claim1, claim2);
    when(claimService.getAllClaimsByType(ClaimTypeEnum.HEALTH_CLAIM_DTO)).thenReturn(claims);

    mockMvc
        .perform(get("/claims/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].medicalProvider", is("City General Hospital")))
        .andExpect(jsonPath("$[1].medicalProvider", is("Regional Medical Center")));
  }

  @Test
  @DisplayName("PUT /claims/health/{id} - Should update health claim and return updated claim")
  void updateHealthClaim_whenExists_shouldReturnUpdatedClaim() throws Exception {
    long claimId = 100L;
    HealthClaimDto updateDto =
        new HealthClaimDto()
            .policyId(1L)
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .medicalProvider("Updated Medical Center")
            .procedureCode("CPT-99215")
            .description("Updated procedure");

    HealthClaimDto updatedDto =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .medicalProvider("Updated Medical Center")
            .procedureCode("CPT-99215")
            .description("Updated procedure")
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    when(claimService.updateClaim(eq(claimId), any(HealthClaimDto.class))).thenReturn(updatedDto);

    mockMvc
        .perform(
            put("/claims/health/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.medicalProvider", is("Updated Medical Center")))
        .andExpect(jsonPath("$.procedureCode", is("CPT-99215")));
  }

  @Test
  @DisplayName("DELETE /claims/health/{id} - Should delete health claim and return 204 No Content")
  void deleteHealthClaim_whenExists_shouldReturnNoContent() throws Exception {
    long claimId = 100L;
    HealthClaimDto existingClaim =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doNothing().when(claimService).deleteClaim(claimId);

    mockMvc.perform(delete("/claims/health/{id}", claimId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /claims/health/{id}/assign-adjuster - Should assign adjuster to health claim")
  void assignAdjusterToHealthClaim_whenValidRequest_shouldReturnUpdatedClaim() throws Exception {
    long claimId = 100L;
    long employeeId = 50L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    HealthClaimDto updatedClaim =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .assignedAdjusterId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId)).thenReturn(updatedClaim);

    mockMvc
        .perform(
            put("/claims/health/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignedAdjusterId", is(50)))
        .andExpect(jsonPath("$.status", is("IN_REVIEW")));
  }

  // ========== ERROR SCENARIOS ==========

  @Test
  @DisplayName("Should return 404 when auto claim not found")
  void nonExistentAutoClaim_shouldReturn404() throws Exception {
    when(claimService.findClaimById(999L)).thenReturn(Optional.empty());
    mockMvc.perform(get("/claims/auto/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 404 when home claim not found")
  void nonExistentHomeClaim_shouldReturn404() throws Exception {
    when(claimService.findClaimById(999L)).thenReturn(Optional.empty());
    mockMvc.perform(get("/claims/home/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 404 when health claim not found")
  void nonExistentHealthClaim_shouldReturn404() throws Exception {
    when(claimService.findClaimById(999L)).thenReturn(Optional.empty());
    mockMvc.perform(get("/claims/health/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 400 when creating auto claim decision fails")
  void createAutoClaimDecision_whenAlreadyExists_shouldReturn409() throws Exception {
    long claimId = 100L;
    ClaimDecisionDto decisionDto =
        new ClaimDecisionDto()
            .claimId(claimId)
            .decisionMakerId(10L)
            .reasoning("approved")
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canMakeDecision(claimId)).thenReturn(true);
    when(claimDecisionService.createClaimDecision(claimId, decisionDto))
        .thenThrow(new IllegalStateException("Decision already exists"));

    mockMvc
        .perform(
            post("/claims/auto/{id}/decision", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionDto)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("Should return 400 when cannot add adjuster reports")
  void createAutoClaimAdjusterReport_whenCannotAdd_shouldReturn400() throws Exception {
    long claimId = 100L;
    AdjusterReportDto reportDto =
        new AdjusterReportDto().claimId(claimId).adjusterId(20L).summary("Test report");

    MockMultipartFile pdfFile =
        new MockMultipartFile("pdfFile", "report.pdf", "application/pdf", "PDF content".getBytes());

    MockMultipartFile reportJson =
        new MockMultipartFile(
            "report",
            "",
            "application/json",
            objectMapper.writeValueAsString(reportDto).getBytes());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canAddAdjusterReports(claimId)).thenReturn(false);

    mockMvc
        .perform(
            multipart("/claims/auto/{id}/adjuster-reports", claimId)
                .file(reportJson)
                .file(pdfFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 when cannot add customer invoices")
  void createAutoClaimCustomerInvoice_whenCannotAdd_shouldReturn400() throws Exception {
    long claimId = 100L;
    CustomerInvoiceDto invoiceDto =
        new CustomerInvoiceDto()
            .claimId(claimId)
            .vendorName("Test Vendor")
            .invoiceAmount(BigDecimal.valueOf(100.00));

    MockMultipartFile pdfFile =
        new MockMultipartFile(
            "pdfFile", "invoice.pdf", "application/pdf", "PDF content".getBytes());

    MockMultipartFile invoiceJson =
        new MockMultipartFile(
            "invoice",
            "",
            "application/json",
            objectMapper.writeValueAsString(invoiceDto).getBytes());

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canAddCustomerInvoices(claimId)).thenReturn(false);

    mockMvc
        .perform(
            multipart("/claims/auto/{id}/customer-invoices", claimId)
                .file(invoiceJson)
                .file(pdfFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 when cannot make claim decision")
  void createAutoClaimDecision_whenCannotMakeDecision_shouldReturn400() throws Exception {
    long claimId = 100L;
    ClaimDecisionDto decisionDto =
        new ClaimDecisionDto()
            .claimId(claimId)
            .decisionMakerId(10L)
            .decisionType(ClaimDecisionDto.DecisionTypeEnum.APPROVED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(new AutoClaimDto()));
    when(claimService.canMakeDecision(claimId)).thenReturn(false);

    mockMvc
        .perform(
            post("/claims/auto/{id}/decision", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionDto)))
        .andExpect(status().isBadRequest());
  }
}
