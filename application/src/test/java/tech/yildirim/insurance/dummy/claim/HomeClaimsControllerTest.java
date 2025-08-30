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
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@WebMvcTest(HomeClaimsController.class)
@DisplayName("Home Claims Controller Web Layer Tests")
class HomeClaimsControllerTest {

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
  @DisplayName("POST /home-claims - Should create home claim and return 201 Created")
  void createHomeClaim_withValidData_shouldReturn201() throws Exception {
    // Given: A valid home claim DTO for creation
    HomeClaimDto inputDto =
        new HomeClaimDto()
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets, electronics")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .policyId(1L)
            .description("Pipe burst in kitchen causing water damage")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(15000.00));

    HomeClaimDto outputDto =
        new HomeClaimDto()
            .id(100L)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets, electronics")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .policyId(1L)
            .description("Pipe burst in kitchen causing water damage")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .estimatedAmount(BigDecimal.valueOf(15000.00))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.submitClaim(eq(1L), any(HomeClaimDto.class))).thenReturn(outputDto);

    // When & Then: Perform POST request and assert the response
    mockMvc
        .perform(
            post("/home-claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HM-2025-001")))
        .andExpect(jsonPath("$.typeOfDamage", is("Water damage")))
        .andExpect(
            jsonPath("$.damagedItems", is("Living room carpet, kitchen cabinets, electronics")))
        .andExpect(jsonPath("$.status", is("SUBMITTED")));
  }

  @Test
  @DisplayName("GET /home-claims/{id} - Should return home claim when claim exists")
  void getHomeClaimById_whenExists_shouldReturnHomeClaim() throws Exception {
    // Given: A home claim exists and the service is mocked to return it
    long claimId = 100L;
    HomeClaimDto homeClaimDto =
        new HomeClaimDto()
            .id(claimId)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets, electronics")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .policyId(1L)
            .description("Pipe burst in kitchen causing water damage")
            .dateOfIncident(LocalDate.of(2025, 8, 15))
            .status(ClaimDto.StatusEnum.SUBMITTED);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(homeClaimDto));

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/home-claims/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.claimNumber", is("HM-2025-001")))
        .andExpect(jsonPath("$.typeOfDamage", is("Water damage")))
        .andExpect(
            jsonPath("$.damagedItems", is("Living room carpet, kitchen cabinets, electronics")))
        .andExpect(jsonPath("$.claimType", is(ClaimTypeEnum.HOME_CLAIM_DTO.toString())));
  }

  @Test
  @DisplayName("GET /home-claims/{id} - Should return 404 Not Found when claim does not exist")
  void getHomeClaimById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service will not find the claim
    when(claimService.findClaimById(anyLong())).thenReturn(Optional.empty());

    // When & Then: Perform GET request and assert the response
    mockMvc.perform(get("/home-claims/{id}", 999L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /home-claims - Should return list of home claims")
  void getAllHomeClaims_shouldReturnHomeClaimsList() throws Exception {
    // Given: Multiple home claims exist
    HomeClaimDto claim1 =
        new HomeClaimDto()
            .id(100L)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet, kitchen cabinets")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.SUBMITTED);

    HomeClaimDto claim2 =
        new HomeClaimDto()
            .id(101L)
            .claimNumber("HM-2025-002")
            .typeOfDamage("Fire damage")
            .damagedItems("Roof, attic, bedroom walls")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO)
            .status(ClaimDto.StatusEnum.IN_REVIEW);

    List<ClaimDto> claims = List.of(claim1, claim2);
    when(claimService.getAllClaimsByType(ClaimTypeEnum.HOME_CLAIM_DTO)).thenReturn(claims);

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/home-claims"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].id", is(100)))
        .andExpect(jsonPath("$[0].claimNumber", is("HM-2025-001")))
        .andExpect(jsonPath("$[0].typeOfDamage", is("Water damage")))
        .andExpect(jsonPath("$[1].id", is(101)))
        .andExpect(jsonPath("$[1].claimNumber", is("HM-2025-002")))
        .andExpect(jsonPath("$[1].typeOfDamage", is("Fire damage")));
  }

  @Test
  @DisplayName("GET /home-claims - Should return empty list when no home claims exist")
  void getAllHomeClaims_whenNoClaims_shouldReturnEmptyList() throws Exception {
    // Given: No home claims exist
    when(claimService.getAllClaimsByType(ClaimTypeEnum.HOME_CLAIM_DTO)).thenReturn(List.of());

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/home-claims"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(0)));
  }

  @Test
  @DisplayName("PUT /home-claims/{id} - Should update home claim and return updated claim")
  void updateHomeClaim_whenExists_shouldReturnUpdatedClaim() throws Exception {
    // Given: An existing claim and update data
    long claimId = 100L;
    long policyId = 1L;
    LocalDate dateOfIncident = LocalDate.of(2025, 8, 15);
    HomeClaimDto updateDto =
        new HomeClaimDto()
            .policyId(policyId)
            .typeOfDamage("Updated water damage")
            .damagedItems("Living room carpet, kitchen cabinets, updated electronics")
            .description("Updated pipe burst description")
            .estimatedAmount(BigDecimal.valueOf(18000.00))
            .dateOfIncident(dateOfIncident);

    HomeClaimDto updatedDto =
        new HomeClaimDto()
            .id(claimId)
            .policyId(policyId)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Updated water damage")
            .damagedItems("Living room carpet, kitchen cabinets, updated electronics")
            .description("Updated pipe burst description")
            .estimatedAmount(BigDecimal.valueOf(18000.00))
            .status(ClaimDto.StatusEnum.IN_REVIEW)
            .dateOfIncident(dateOfIncident);

    when(claimService.updateClaim(eq(claimId), any(HomeClaimDto.class))).thenReturn(updatedDto);

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/home-claims/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.typeOfDamage", is("Updated water damage")))
        .andExpect(
            jsonPath(
                "$.damagedItems", is("Living room carpet, kitchen cabinets, updated electronics")))
        .andExpect(jsonPath("$.description", is("Updated pipe burst description")));
  }

  @Test
  @DisplayName("PUT /home-claims/{id} - Should return 404 Not Found when claim does not exist")
  void updateHomeClaim_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service throws an exception when claim is not found
    long claimId = 999L;
    long policyId = 1L;
    LocalDate dateOfIncident = LocalDate.of(2025, 8, 15);
    HomeClaimDto updateDto =
        new HomeClaimDto()
            .policyId(policyId)
            .typeOfDamage("Updated water damage")
            .description("Updated pipe burst description")
            .dateOfIncident(dateOfIncident);

    when(claimService.updateClaim(eq(claimId), any(HomeClaimDto.class)))
        .thenThrow(new ResourceNotFoundException("Claim not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/home-claims/{id}", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /home-claims/{id} - Should delete home claim and return 204 No Content")
  void deleteHomeClaim_whenExists_shouldReturnNoContent() throws Exception {
    // Given: An existing home claim
    long claimId = 100L;
    HomeClaimDto existingClaim =
        new HomeClaimDto()
            .id(claimId)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doNothing().when(claimService).deleteClaim(claimId);

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/home-claims/{id}", claimId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /home-claims/{id} - Should return 404 Not Found when claim does not exist")
  void deleteHomeClaim_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The claim does not exist
    long claimId = 999L;
    when(claimService.findClaimById(claimId)).thenReturn(Optional.empty());

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/home-claims/{id}", claimId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /home-claims/{id} - Should return 404 when claim is not a home claim")
  void deleteHomeClaim_whenNotHomeClaim_shouldReturnNotFound() throws Exception {
    // Given: The claim exists but is not a home claim (polymorphic check)
    long claimId = 100L;
    ClaimDto nonHomeClaim =
        new ClaimDto().id(claimId).claimType(ClaimTypeEnum.AUTO_CLAIM_DTO); // Different claim type

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(nonHomeClaim));

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/home-claims/{id}", claimId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /home-claims/{id} - Should return 500 when deletion fails")
  void deleteHomeClaim_whenDeletionFails_shouldReturnInternalServerError() throws Exception {
    // Given: An existing home claim but deletion fails
    long claimId = 100L;
    HomeClaimDto existingClaim =
        new HomeClaimDto()
            .id(claimId)
            .claimNumber("HM-2025-001")
            .typeOfDamage("Water damage")
            .claimType(ClaimTypeEnum.HOME_CLAIM_DTO);

    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(existingClaim));
    doThrow(new RuntimeException("Database error")).when(claimService).deleteClaim(claimId);

    // When & Then: Perform DELETE request and assert the response
    mockMvc
        .perform(delete("/home-claims/{id}", claimId))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName(
      "PUT /home-claims/{id}/assign-adjuster - Should assign adjuster and return updated claim")
  void assignAdjusterToHomeClaim_whenValidRequest_shouldReturnUpdatedClaim() throws Exception {
    // Given: A valid assign adjuster request
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

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/home-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.assignedAdjusterId", is(50)))
        .andExpect(jsonPath("$.status", is("IN_REVIEW")));
  }

  @Test
  @DisplayName(
      "PUT /home-claims/{id}/assign-adjuster - Should return 404 when claim does not exist")
  void assignAdjusterToHomeClaim_whenClaimNotExists_shouldReturnNotFound() throws Exception {
    // Given: The claim does not exist
    long claimId = 999L;
    long employeeId = 50L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId))
        .thenThrow(new ResourceNotFoundException("Claim not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/home-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "PUT /home-claims/{id}/assign-adjuster - Should return 404 when employee does not exist")
  void assignAdjusterToHomeClaim_whenEmployeeNotExists_shouldReturnNotFound() throws Exception {
    // Given: The employee does not exist
    long claimId = 100L;
    long employeeId = 999L;
    AssignAdjusterRequestDto assignRequest = new AssignAdjusterRequestDto().employeeId(employeeId);

    when(claimService.assignAdjuster(claimId, employeeId))
        .thenThrow(new ResourceNotFoundException("Employee not found"));

    // When & Then: Perform PUT request and assert the response
    mockMvc
        .perform(
            put("/home-claims/{id}/assign-adjuster", claimId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isNotFound());
  }
}
