package tech.yildirim.insurance.dummy.policy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.claim.ClaimService;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@WebMvcTest(PolicyController.class)
@DisplayName("Policy Controller Web Layer Tests")
class PolicyControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PolicyService policyService;

  @Autowired private ClaimService claimService;

  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public PolicyService policyService() {
      return Mockito.mock(PolicyService.class);
    }

    @Bean
    public ClaimService claimService() {
      return Mockito.mock(ClaimService.class);
    }
  }

  @Test
  @DisplayName("GET /policies/{id} - Should return 200 OK when policy exists")
  void getPolicyById_whenExists_shouldReturnOk() throws Exception {
    // Given: A policy DTO is available
    long policyId = 1L;
    PolicyDto fakePolicy = new PolicyDto().id(policyId).policyNumber("POL-123");
    when(policyService.findPolicyById(policyId)).thenReturn(Optional.of(fakePolicy));

    // When & Then
    mockMvc
        .perform(get("/policies/{id}", policyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.policyNumber", is("POL-123")));
  }

  @Test
  @DisplayName("GET /policies/{id} - Should return 404 Not Found when policy does not exist")
  void getPolicyById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service will not find the policy
    when(policyService.findPolicyById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/policies/{id}", 99L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /policies - Should return 201 Created for a valid request")
  void createPolicy_withValidData_shouldReturnCreated() throws Exception {
    // Given: A valid DTO for policy creation
    PolicyDto inputDto =
        new PolicyDto()
            .customerId(1L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .premium(new BigDecimal("100.00"))
            .type(PolicyDto.TypeEnum.HEALTH);

    PolicyDto outputDto =
        new PolicyDto()
            .id(101L)
            .policyNumber("POL-XYZ-123")
            .customerId(1L)
            .status(PolicyDto.StatusEnum.PENDING);

    when(policyService.createPolicy(any(PolicyDto.class))).thenReturn(outputDto);

    // When & Then
    mockMvc
        .perform(
            post("/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(101)))
        .andExpect(jsonPath("$.status", is("PENDING")));
  }

  @Test
  @DisplayName("POST /policies - Should return 404 Not Found when customer does not exist")
  void createPolicy_forNonExistentCustomer_shouldReturnNotFound() throws Exception {
    // Given: The service will throw an exception because the customer is not found
    PolicyDto inputDto =
        new PolicyDto()
            .customerId(1L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .premium(new BigDecimal("100.00"))
            .type(PolicyDto.TypeEnum.HEALTH)
            .customerId(99L);
    when(policyService.createPolicy(any(PolicyDto.class)))
        .thenThrow(new ResourceNotFoundException("Customer not found with id: 99"));

    // When & Then
    mockMvc
        .perform(
            post("/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /policies/{policyId}/auto-claims - Should return auto claims for policy")
  void getAutoClaimsByPolicyId_shouldReturnAutoClaims() throws Exception {
    // Given: A policy ID and some claims including auto claims
    Long policyId = 1L;

    AutoClaimDto autoClaim1 =
        new AutoClaimDto()
            .id(1L)
            .claimNumber("AUTO-001")
            .policyId(policyId)
            .vehicleVin("1HGBH41JXMN109186")
            .licensePlate("ABC123");

    AutoClaimDto autoClaim2 =
        new AutoClaimDto()
            .id(2L)
            .claimNumber("AUTO-002")
            .policyId(policyId)
            .vehicleVin("2HGBH41JXMN109187")
            .licensePlate("XYZ789");

    HomeClaimDto homeClaim =
        new HomeClaimDto()
            .id(3L)
            .claimNumber("HOME-001")
            .policyId(policyId)
            .typeOfDamage("Water damage");

    List<ClaimDto> allClaims = List.of(autoClaim1, autoClaim2, homeClaim);
    when(claimService.findClaimsByPolicyId(policyId)).thenReturn(allClaims);

    // When & Then
    mockMvc
        .perform(get("/policies/{policyId}/auto-claims", policyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].claimNumber", is("AUTO-001")))
        .andExpect(jsonPath("$[0].vehicleVin", is("1HGBH41JXMN109186")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].claimNumber", is("AUTO-002")));
  }

  @Test
  @DisplayName(
      "GET /policies/{policyId}/auto-claims - Should return empty list when no auto claims exist")
  void getAutoClaimsByPolicyId_whenNoAutoClaims_shouldReturnEmptyList() throws Exception {
    // Given: A policy ID with only non-auto claims
    Long policyId = 1L;

    HomeClaimDto homeClaim = new HomeClaimDto().id(1L).claimNumber("HOME-001").policyId(policyId);

    List<ClaimDto> allClaims = List.of(homeClaim);
    when(claimService.findClaimsByPolicyId(policyId)).thenReturn(allClaims);

    // When & Then
    mockMvc
        .perform(get("/policies/{policyId}/auto-claims", policyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("GET /policies/{policyId}/home-claims - Should return home claims for policy")
  void getHomeClaimsByPolicyId_shouldReturnHomeClaims() throws Exception {
    // Given: A policy ID and some claims including home claims
    Long policyId = 1L;

    HomeClaimDto homeClaim1 =
        new HomeClaimDto()
            .id(1L)
            .claimNumber("HOME-001")
            .policyId(policyId)
            .typeOfDamage("Water damage")
            .damagedItems("Living room carpet");

    HomeClaimDto homeClaim2 =
        new HomeClaimDto()
            .id(2L)
            .claimNumber("HOME-002")
            .policyId(policyId)
            .typeOfDamage("Fire damage")
            .damagedItems("Kitchen cabinets");

    AutoClaimDto autoClaim = new AutoClaimDto().id(3L).claimNumber("AUTO-001").policyId(policyId);

    List<ClaimDto> allClaims = List.of(homeClaim1, homeClaim2, autoClaim);
    when(claimService.findClaimsByPolicyId(policyId)).thenReturn(allClaims);

    // When & Then
    mockMvc
        .perform(get("/policies/{policyId}/home-claims", policyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].claimNumber", is("HOME-001")))
        .andExpect(jsonPath("$[0].typeOfDamage", is("Water damage")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].claimNumber", is("HOME-002")));
  }

  @Test
  @DisplayName("GET /policies/{policyId}/health-claims - Should return health claims for policy")
  void getHealthClaimsByPolicyId_shouldReturnHealthClaims() throws Exception {
    // Given: A policy ID and some claims including health claims
    Long policyId = 1L;

    HealthClaimDto healthClaim1 =
        new HealthClaimDto()
            .id(1L)
            .claimNumber("HEALTH-001")
            .policyId(policyId)
            .medicalProvider("City General Hospital")
            .procedureCode("CPT-99213");

    HealthClaimDto healthClaim2 =
        new HealthClaimDto()
            .id(2L)
            .claimNumber("HEALTH-002")
            .policyId(policyId)
            .medicalProvider("Downtown Clinic")
            .procedureCode("CPT-99214");

    AutoClaimDto autoClaim = new AutoClaimDto().id(3L).claimNumber("AUTO-001").policyId(policyId);

    List<ClaimDto> allClaims = List.of(healthClaim1, healthClaim2, autoClaim);
    when(claimService.findClaimsByPolicyId(policyId)).thenReturn(allClaims);

    // When & Then
    mockMvc
        .perform(get("/policies/{policyId}/health-claims", policyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].claimNumber", is("HEALTH-001")))
        .andExpect(jsonPath("$[0].medicalProvider", is("City General Hospital")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].claimNumber", is("HEALTH-002")));
  }

  @Test
  @DisplayName("GET /policies/{policyId}/auto-claims - Should support query parameters")
  void getAutoClaimsByPolicyId_withQueryParams_shouldWork() throws Exception {
    // Given: A policy ID and auto claims
    Long policyId = 1L;

    AutoClaimDto autoClaim = new AutoClaimDto().id(1L).claimNumber("AUTO-001").policyId(policyId);

    List<ClaimDto> allClaims = List.of(autoClaim);
    when(claimService.findClaimsByPolicyId(policyId)).thenReturn(allClaims);

    // When & Then
    mockMvc
        .perform(
            get("/policies/{policyId}/auto-claims", policyId)
                .param("page", "0")
                .param("size", "10")
                .param("status", "SUBMITTED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }
}
