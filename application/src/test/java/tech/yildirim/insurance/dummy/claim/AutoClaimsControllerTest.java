package tech.yildirim.insurance.dummy.claim;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.test.web.servlet.MockMvc;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@WebMvcTest(AutoClaimsController.class)
@DisplayName("Auto Claims Controller Web Layer Tests")
class AutoClaimsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ClaimService claimService;

  @Autowired private ObjectMapper objectMapper;

  /**
   * This static inner class provides the mock bean definition for ClaimService. This is the modern
   * replacement for @MockBean in Spring Boot 3.4+.
   */
  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public ClaimService claimService() {
      return Mockito.mock(ClaimService.class);
    }
  }

  @Test
  @DisplayName("POST /auto-claims - Should create auto claim and return 201 Created")
  void createAutoClaim_withValidData_shouldReturn201() throws Exception {
    // Given: A valid auto claim DTO for creation
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

    // When & Then: Perform POST request and assert the response
    mockMvc
        .perform(
            post("/auto-claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("AC-2025-001")))
        .andExpect(jsonPath("$.licensePlate", is("ABC-123")))
        .andExpect(jsonPath("$.status", is("SUBMITTED")));
  }

  @Test
  @DisplayName("GET /auto-claims/{id} - Should return auto claim when claim exists")
  void getAutoClaimById_whenExists_shouldReturnAutoClaim() throws Exception {
    // Given: An auto claim exists and the service is mocked to return it
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

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/auto-claims/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("AC-2025-001")))
        .andExpect(jsonPath("$.licensePlate", is("ABC-123")))
        .andExpect(jsonPath("$.claimType", is(ClaimTypeEnum.AUTO_CLAIM_DTO.toString())));
  }

  @Test
  @DisplayName("GET /auto-claims/{id} - Should return 404 Not Found when claim does not exist")
  void getAutoClaimById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service will not find the claim
    when(claimService.findClaimById(anyLong())).thenReturn(Optional.empty());

    // When & Then: Perform GET request and assert the response
    mockMvc.perform(get("/auto-claims/{id}", 999L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /auto-claims - Should return list of auto claims")
  void getAllAutoClaims_shouldReturnAutoClaimsList() throws Exception {
    // Given: Multiple auto claims exist
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

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/auto-claims"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].id", is(100)))
        .andExpect(jsonPath("$[0].claimNumber", is("AC-2025-001")))
        .andExpect(jsonPath("$[1].id", is(101)))
        .andExpect(jsonPath("$[1].claimNumber", is("AC-2025-002")));
  }

  @Test
  @DisplayName("GET /auto-claims - Should return empty list when no auto claims exist")
  void getAllAutoClaims_whenNoClaims_shouldReturnEmptyList() throws Exception {
    // Given: No auto claims exist
    when(claimService.getAllClaimsByType(ClaimTypeEnum.AUTO_CLAIM_DTO)).thenReturn(List.of());

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/auto-claims"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(0)));
  }

  @Test
  @DisplayName("PUT /auto-claims/{id} - Should update auto claim and return updated claim")
  void updateAutoClaim_whenExists_shouldReturnUpdatedClaim() throws Exception {
    // Given: An existing claim and update data
    long claimId = 100L;
    long policyId = 1L;
    LocalDate dateOfIncident = LocalDate.of(2025, 8, 15);
    AutoClaimDto updateDto =
        new AutoClaimDto()
            .policyId(policyId)
            .vehicleVin("1HGCM82633A123456")
            .licensePlate("ABC-123")
            .accidentLocation("Updated location")
            .description("Updated description")
            .estimatedAmount(BigDecimal.valueOf(7500.00))
            .dateOfIncident(dateOfIncident);

    AutoClaimDto updatedDto =
        new AutoClaimDto()
            .id(claimId)
            .policyId(policyId)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .vehicleVin("1HGCM82633A123456")
            .accidentLocation("Updated location")
            .description("Updated description")
            .estimatedAmount(BigDecimal.valueOf(7500.00))
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .dateOfIncident(dateOfIncident);

    when(claimService.updateClaim(eq(claimId), any(AutoClaimDto.class))).thenReturn(updatedDto);

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/auto-claims/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.accidentLocation", is("Updated location")))
        .andExpect(jsonPath("$.description", is("Updated description")));
  }

  @Test
  @DisplayName("PUT /auto-claims/{id} - Should return 404 Not Found when claim does not exist")
  void updateAutoClaim_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service throws an exception when claim is not found
    long claimId = 999L;
    long policyId = 1L;
    LocalDate dateOfIncident = LocalDate.of(2025, 8, 15);
    AutoClaimDto updateDto =
        new AutoClaimDto()
            .policyId(policyId)
            .licensePlate("ABC-123")
            .description("Updated description")
            .dateOfIncident(dateOfIncident);

    when(claimService.updateClaim(eq(claimId), any(AutoClaimDto.class)))
        .thenThrow(new ResourceNotFoundException("Claim not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/auto-claims/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /auto-claims/{id} - Should delete auto claim and return 204 No Content")
  void deleteAutoClaim_whenExists_shouldReturnNoContent() throws Exception {
    // Given: An existing auto claim
    long claimId = 100L;
    AutoClaimDto existingClaim =
        new AutoClaimDto()
            .id(claimId)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doNothing().when(claimService).deleteClaim(claimId);

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/auto-claims/{id}", claimId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /auto-claims/{id} - Should return 404 Not Found when claim does not exist")
  void deleteAutoClaim_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The claim does not exist
    long claimId = 999L;
    when(claimService.findClaimById(claimId)).thenReturn(Optional.empty());

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/auto-claims/{id}", claimId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /auto-claims/{id} - Should return 404 when claim is not an auto claim")
  void deleteAutoClaim_whenNotAutoClaim_shouldReturnNotFound() throws Exception {
    // Given: The claim exists but is not an auto claim (polymorphic check)
    long claimId = 100L;
    ClaimDto nonAutoClaim =
        new ClaimDto().id(claimId).claimType(ClaimTypeEnum.HOME_CLAIM_DTO); // Different claim type

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(nonAutoClaim));

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/auto-claims/{id}", claimId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /auto-claims/{id} - Should return 500 when deletion fails")
  void deleteAutoClaim_whenDeletionFails_shouldReturnInternalServerError() throws Exception {
    // Given: An existing auto claim but deletion fails
    long claimId = 100L;
    AutoClaimDto existingClaim =
        new AutoClaimDto()
            .id(claimId)
            .claimNumber("AC-2025-001")
            .licensePlate("ABC-123")
            .claimType(ClaimTypeEnum.AUTO_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doThrow(new RuntimeException("Database error")).when(claimService).deleteClaim(claimId);

    // When & Then: Perform DELETE request and assert the response
    mockMvc
        .perform(delete("/auto-claims/{id}", claimId))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName(
      "PUT /auto-claims/{id}/assign-adjuster - Should assign adjuster and return updated claim")
  void assignAdjusterToAutoClaim_whenValidRequest_shouldReturnUpdatedClaim() throws Exception {
    // Given: A valid assign adjuster request
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

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/auto-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.assignedAdjusterId", is(50)))
        .andExpect(jsonPath("$.status", is("IN_REVIEW")));
  }

  @Test
  @DisplayName(
      "PUT /auto-claims/{id}/assign-adjuster - Should return 404 when claim does not exist")
  void assignAdjusterToAutoClaim_whenClaimNotExists_shouldReturnNotFound() throws Exception {
    // Given: The claim does not exist
    long claimId = 999L;
    long employeeId = 50L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId))
        .thenThrow(new ResourceNotFoundException("Claim not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/auto-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "PUT /auto-claims/{id}/assign-adjuster - Should return 404 when employee does not exist")
  void assignAdjusterToAutoClaim_whenEmployeeNotExists_shouldReturnNotFound() throws Exception {
    // Given: The employee does not exist
    long claimId = 100L;
    long employeeId = 999L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId))
        .thenThrow(new ResourceNotFoundException("Employee not found"));

    // When & Then: Perform POST request and assert the response
    mockMvc
        .perform(
            put("/auto-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isNotFound());
  }
}
