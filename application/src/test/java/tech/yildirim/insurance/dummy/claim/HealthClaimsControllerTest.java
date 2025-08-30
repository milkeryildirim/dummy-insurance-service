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
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@WebMvcTest(HealthClaimsController.class)
@DisplayName("Health Claims Controller Web Layer Tests")
class HealthClaimsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ClaimService claimService;

  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public ClaimService claimService() {
      return Mockito.mock(ClaimService.class);
    }
  }

  @Test
  @DisplayName("POST /health-claims - Should create health claim and return 201 Created")
  void createHealthClaim_withValidData_shouldReturn201() throws Exception {
    // Given: A valid health claim DTO for creation
    HealthClaimDto inputDto =
        new HealthClaimDto()
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .policyId(1L)
            .description("Medical consultation for flu symptoms")
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
            .description("Medical consultation for flu symptoms")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(250.00))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.submitClaim(eq(1L), any(HealthClaimDto.class))).thenReturn(outputDto);

    // When & Then: Perform POST request and assert the response
    mockMvc
        .perform(
            post("/health-claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HC-2025-001")))
        .andExpect(jsonPath("$.medicalProvider", is("City General Hospital")))
        .andExpect(jsonPath("$.procedureCode", is("CPT-99213")))
        .andExpect(jsonPath("$.status", is("SUBMITTED")));
  }

  @Test
  @DisplayName("GET /health-claims/{id} - Should return health claim when claim exists")
  void getHealthClaimById_whenExists_shouldReturnHealthClaim() throws Exception {
    // Given: A health claim exists and the service is mocked to return it
    long claimId = 100L;
    HealthClaimDto healthClaimDto =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .policyId(1L)
            .description("Medical consultation for flu symptoms")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(healthClaimDto));

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/health-claims/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HC-2025-001")))
        .andExpect(jsonPath("$.medicalProvider", is("City General Hospital")))
        .andExpect(jsonPath("$.procedureCode", is("CPT-99213")))
        .andExpect(jsonPath("$.claimType", is(ClaimTypeEnum.HEALTH_CLAIM_DTO.toString())));
  }

  @Test
  @DisplayName("GET /health-claims/{id} - Should return 404 Not Found when claim does not exist")
  void getHealthClaimById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service will not find the claim
    when(claimService.findClaimById(anyLong())).thenReturn(Optional.empty());

    // When & Then: Perform GET request and assert the response
    mockMvc.perform(get("/health-claims/{id}", 999L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /health-claims - Should return list of health claims")
  void getAllHealthClaims_shouldReturnHealthClaimsList() throws Exception {
    // Given: Multiple health claims exist
    HealthClaimDto claim1 =
        new HealthClaimDto()
            .id(100L)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    HealthClaimDto claim2 =
        new HealthClaimDto()
            .id(101L)
            .claimNumber("HC-2025-002")
            .medicalProvider("Regional Medical Center")
            .procedureCode("CPT-99214")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    List<ClaimDto> claims = List.of(claim1, claim2);
    when(claimService.getAllClaimsByType(ClaimTypeEnum.HEALTH_CLAIM_DTO)).thenReturn(claims);

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/health-claims"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].id", is(100)))
        .andExpect(jsonPath("$[0].claimNumber", is("HC-2025-001")))
        .andExpect(jsonPath("$[0].medicalProvider", is("City General Hospital")))
        .andExpect(jsonPath("$[1].id", is(101)))
        .andExpect(jsonPath("$[1].claimNumber", is("HC-2025-002")))
        .andExpect(jsonPath("$[1].medicalProvider", is("Regional Medical Center")));
  }

  @Test
  @DisplayName("GET /health-claims - Should return empty list when no health claims exist")
  void getAllHealthClaims_whenNoClaims_shouldReturnEmptyList() throws Exception {
    // Given: No health claims exist
    when(claimService.getAllClaimsByType(ClaimTypeEnum.HEALTH_CLAIM_DTO)).thenReturn(List.of());

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/health-claims"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(0)));
  }

  @Test
  @DisplayName("PUT /health-claims/{id} - Should update health claim and return updated claim")
  void updateHealthClaim_whenExists_shouldReturnUpdatedClaim() throws Exception {
    // Given: An existing claim and update data
    long claimId = 100L;
    long policyId = 1L;
    LocalDate dateOfIncident = LocalDate.of(2025, 8, 15);
    HealthClaimDto updateDto =
        new HealthClaimDto()
            .policyId(policyId)
            .medicalProvider("Updated Medical Center")
            .procedureCode("CPT-99215")
            .description("Updated medical procedure")
            .estimatedAmount(BigDecimal.valueOf(350.00))
            .dateOfIncident(dateOfIncident);

    HealthClaimDto updatedDto =
        new HealthClaimDto()
            .id(claimId)
            .policyId(policyId)
            .claimNumber("HC-2025-001")
            .medicalProvider("Updated Medical Center")
            .procedureCode("CPT-99215")
            .description("Updated medical procedure")
            .estimatedAmount(BigDecimal.valueOf(350.00))
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .dateOfIncident(dateOfIncident);

    when(claimService.updateClaim(eq(claimId), any(HealthClaimDto.class))).thenReturn(updatedDto);

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/health-claims/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.medicalProvider", is("Updated Medical Center")))
        .andExpect(jsonPath("$.procedureCode", is("CPT-99215")))
        .andExpect(jsonPath("$.description", is("Updated medical procedure")));
  }

  @Test
  @DisplayName("PUT /health-claims/{id} - Should return 404 Not Found when claim does not exist")
  void updateHealthClaim_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service throws an exception when claim is not found
    long claimId = 999L;
    long policyId = 1L;
    LocalDate dateOfIncident = LocalDate.of(2025, 8, 15);
    HealthClaimDto updateDto =
        new HealthClaimDto()
            .policyId(policyId)
            .medicalProvider("Updated Medical Center")
            .description("Updated medical procedure")
            .dateOfIncident(dateOfIncident);

    when(claimService.updateClaim(eq(claimId), any(HealthClaimDto.class)))
        .thenThrow(new ResourceNotFoundException("Claim not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/health-claims/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /health-claims/{id} - Should delete health claim and return 204 No Content")
  void deleteHealthClaim_whenExists_shouldReturnNoContent() throws Exception {
    // Given: An existing health claim
    long claimId = 100L;
    HealthClaimDto existingClaim =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doNothing().when(claimService).deleteClaim(claimId);

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/health-claims/{id}", claimId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /health-claims/{id} - Should return 404 Not Found when claim does not exist")
  void deleteHealthClaim_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The claim does not exist
    long claimId = 999L;
    when(claimService.findClaimById(claimId)).thenReturn(Optional.empty());

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/health-claims/{id}", claimId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /health-claims/{id} - Should return 404 when claim is not a health claim")
  void deleteHealthClaim_whenNotHealthClaim_shouldReturnNotFound() throws Exception {
    // Given: The claim exists but is not a health claim (polymorphic check)
    long claimId = 100L;
    ClaimDto nonHealthClaim =
        new ClaimDto().id(claimId).claimType(ClaimTypeEnum.AUTO_CLAIM_DTO); // Different claim type

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(nonHealthClaim));

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/health-claims/{id}", claimId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /health-claims/{id} - Should return 500 when deletion fails")
  void deleteHealthClaim_whenDeletionFails_shouldReturnInternalServerError() throws Exception {
    // Given: An existing health claim but deletion fails
    long claimId = 100L;
    HealthClaimDto existingClaim =
        new HealthClaimDto()
            .id(claimId)
            .claimNumber("HC-2025-001")
            .medicalProvider("City General Hospital")
            .claimType(ClaimTypeEnum.HEALTH_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doThrow(new RuntimeException("Database error")).when(claimService).deleteClaim(claimId);

    // When & Then: Perform DELETE request and assert the response
    mockMvc
        .perform(delete("/health-claims/{id}", claimId))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName(
      "PUT /health-claims/{id}/assign-adjuster - Should assign adjuster and return updated claim")
  void assignAdjusterToHealthClaim_whenValidRequest_shouldReturnUpdatedClaim() throws Exception {
    // Given: A valid assign adjuster request
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

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/health-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.assignedAdjusterId", is(50)))
        .andExpect(jsonPath("$.status", is("IN_REVIEW")));
  }

  @Test
  @DisplayName(
      "PUT /health-claims/{id}/assign-adjuster - Should return 404 when claim does not exist")
  void assignAdjusterToHealthClaim_whenClaimNotExists_shouldReturnNotFound() throws Exception {
    // Given: The claim does not exist
    long claimId = 999L;
    long employeeId = 50L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId))
        .thenThrow(new ResourceNotFoundException("Claim not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/health-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "PUT /health-claims/{id}/assign-adjuster - Should return 404 when employee does not exist")
  void assignAdjusterToHealthClaim_whenEmployeeNotExists_shouldReturnNotFound() throws Exception {
    // Given: The employee does not exist
    long claimId = 100L;
    long employeeId = 999L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId))
        .thenThrow(new ResourceNotFoundException("Employee not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/health-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isNotFound());
  }
}
