package tech.yildirim.insurance.dummy.policy.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.yildirim.insurance.api.generated.model.PolicyConditionsDto;

@WebMvcTest(PolicyConditionsController.class)
@DisplayName("Policy Conditions Controller Web Layer Tests")
class PolicyConditionsControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private PolicyConditionsService policyConditionsService;
  @Autowired
  private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public PolicyConditionsService policyConditionsService() {
      return Mockito.mock(PolicyConditionsService.class);
    }
  }

  @Test
  @DisplayName("GET /policy-conditions - Should return current conditions with 200 OK")
  void getPolicyConditions_shouldReturn200Ok() throws Exception {
    // Given
    PolicyConditionsDto conditionsDto = new PolicyConditionsDto().freeCancellationDays(14);
    when(policyConditionsService.getPolicyConditions()).thenReturn(conditionsDto);

    // When & Then
    mockMvc.perform(get("/policy-conditions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.freeCancellationDays", is(14)));
  }

  @Test
  @DisplayName("PUT /policy-conditions - Should update conditions and return 200 OK")
  void updatePolicyConditions_shouldReturn200Ok() throws Exception {
    // Given: A complete and valid DTO
    PolicyConditionsDto requestDto = new PolicyConditionsDto()
        .freeCancellationDays(30)
        .noClaimBonusPercentage(new BigDecimal("0.07"))
        .cancellationRules(Collections.emptyList());

    PolicyConditionsDto responseDto = new PolicyConditionsDto().freeCancellationDays(30);
    when(policyConditionsService.updatePolicyConditions(any(PolicyConditionsDto.class))).thenReturn(responseDto);

    // When & Then
    mockMvc.perform(put("/policy-conditions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.freeCancellationDays", is(30)));
  }
}
