package tech.yildirim.insurance.dummy.agency;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.yildirim.insurance.api.generated.model.AddressDto;
import tech.yildirim.insurance.api.generated.model.AgencyDto;

@WebMvcTest(AgencyController.class)
@DisplayName("Agency Controller Web Layer Tests")
class AgencyControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private AgencyService agencyService;
  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public AgencyService agencyService() {
      return Mockito.mock(AgencyService.class);
    }
  }

  @Test
  @DisplayName("POST /agencies - Should create agency and return 201 Created for valid data")
  void createAgency_withValidData_shouldReturnCreated() throws Exception {
    // Given: A valid DTO for creation
    AddressDto addressDto =
        new AddressDto()
            .streetAndHouseNumber("Test Street 1")
            .postalCode("12345")
            .city("Testville")
            .country("Testland");

    AgencyDto inputDto =
        new AgencyDto()
            .agencyCode("AG-NEW-001")
            .name("New Agency")
            .address(addressDto)
            .contactPerson("New Person")
            .contactEmail("new@agency.com");

    AgencyDto outputDto = new AgencyDto().id(1L).agencyCode("AG-NEW-001");

    when(agencyService.createAgency(any(AgencyDto.class))).thenReturn(outputDto);

    // When & Then
    mockMvc
        .perform(
            post("/agencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)));
  }
}
