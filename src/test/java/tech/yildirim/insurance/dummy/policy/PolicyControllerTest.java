package tech.yildirim.insurance.dummy.policy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
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
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@WebMvcTest(PolicyController.class)
@DisplayName("Policy Controller Web Layer Tests")
class PolicyControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PolicyService policyService;

  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public PolicyService policyService() {
      return Mockito.mock(PolicyService.class);
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
    PolicyDto inputDto = new PolicyDto()
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
}
