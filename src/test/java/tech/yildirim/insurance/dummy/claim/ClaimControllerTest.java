package tech.yildirim.insurance.dummy.claim;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import tech.yildirim.insurance.api.generated.model.ClaimDto;

@WebMvcTest(ClaimController.class)
@DisplayName("Claim Controller Web Layer Tests")
class ClaimControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ClaimService claimService;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public ClaimService claimService() {
      return Mockito.mock(ClaimService.class);
    }
  }

  @Test
  @DisplayName("GET /claims/{id} - Should return 200 OK when claim exists")
  void getClaimById_whenExists_shouldReturnOk() throws Exception {
    // Given
    long claimId = 1L;
    ClaimDto fakeClaim = new ClaimDto().id(claimId).claimNumber("CLM-123");
    when(claimService.findClaimById(claimId)).thenReturn(Optional.of(fakeClaim));

    // When & Then
    mockMvc
        .perform(get("/claims/{id}", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.claimNumber", is("CLM-123")));
  }

  @Test
  @DisplayName("GET /claims/{id} - Should return 404 Not Found when claim does not exist")
  void getClaimById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given
    when(claimService.findClaimById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/claims/{id}", 99L)).andExpect(status().isNotFound());
  }
}
