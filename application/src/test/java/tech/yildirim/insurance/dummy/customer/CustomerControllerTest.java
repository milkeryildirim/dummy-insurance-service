package tech.yildirim.insurance.dummy.customer;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import tech.yildirim.insurance.api.generated.model.AddressDto;
import tech.yildirim.insurance.api.generated.model.CustomerDto;
import tech.yildirim.insurance.api.generated.model.PolicyDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.policy.PolicyService;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Web Layer Tests")
class CustomerControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CustomerService customerService;

  @Autowired private PolicyService policyService;

  @Autowired private ObjectMapper objectMapper;

  /**
   * This static inner class provides the mock bean definition for CustomerService. This is the
   * modern replacement for @MockBean in Spring Boot 3.4+.
   */
  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public CustomerService customerService() {
      // We manually create the mock object here.
      return Mockito.mock(CustomerService.class);
    }

    @Bean
    public PolicyService policyService() {
      return Mockito.mock(PolicyService.class);
    }
  }

  @Test
  @DisplayName("GET /customers/{id} - Should return Customer DTO when customer exists")
  void getCustomerById_whenExists_shouldReturnCustomerDto() throws Exception {
    // Given: A customer exists and the service is mocked to return it
    long customerId = 1L;
    CustomerDto fakeCustomerDto =
        new CustomerDto().id(customerId).firstName("John").lastName("Doe");
    when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(fakeCustomerDto));

    // When & Then: Perform GET request and assert the response
    mockMvc
        .perform(get("/customers/{id}", customerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.firstName", is("John")));
  }

  @Test
  @DisplayName("GET /customers/{id} - Should return 404 Not Found when customer does not exist")
  void getCustomerById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service will not find the customer
    when(customerService.findCustomerById(anyLong())).thenReturn(Optional.empty());

    // When & Then: Perform GET request and assert the response
    mockMvc.perform(get("/customers/{id}", 99L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /customers - Should create customer and return 201 Created")
  void createCustomer_withValidData_shouldReturn201() throws Exception {
    // Given: A DTO to be created
    CustomerDto inputDto =
        new CustomerDto()
            .firstName("Jane")
            .lastName("Doe")
            .password("testpassword")
            .address(
                new AddressDto()
                    .streetAndHouseNumber("Musterstraße 1")
                    .city("Musterstadt")
                    .postalCode("65000")
                    .country("Germany"))
            .email("jane.doe@example.com")
            .dateOfBirth(LocalDate.of(1995, 1, 1));
    CustomerDto outputDto =
        new CustomerDto()
            .id(2L)
            .firstName("Jane")
            .lastName("Doe")
            .password("testpassword")
            .address(
                new AddressDto()
                    .streetAndHouseNumber("Musterstraße 1")
                    .city("Musterstadt")
                    .postalCode("65000")
                    .country("Germany"))
            .email("jane.doe@example.com");
    when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(outputDto);

    // When & Then: Perform POST request and assert the response
    mockMvc
        .perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(2)));
  }

  @Test
  @DisplayName("DELETE /customers/{id} - Should return 204 No Content when customer exists")
  void deleteCustomer_whenExists_shouldReturnNoContent() throws Exception {
    // Given: The service reports that the deletion was successful
    long customerId = 1L;
    when(customerService.deleteCustomerById(customerId)).thenReturn(true);

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/customers/{id}", customerId)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /customers/{id} - Should return 404 Not Found when customer does not exist")
  void deleteCustomer_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given: The service reports that the customer was not found for deletion
    long customerId = 99L;
    when(customerService.deleteCustomerById(customerId)).thenReturn(false);

    // When & Then: Perform DELETE request and assert the response
    mockMvc.perform(delete("/customers/{id}", customerId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /customers/{id}/policies - Should return policies for an existing customer")
  void getPoliciesByCustomerId_whenCustomerExists_shouldReturnPolicies() throws Exception {
    // Given: The policy service will return a list of policies
    long customerId = 1L;
    List<PolicyDto> policies = List.of(new PolicyDto().id(101L));
    when(policyService.findPoliciesByCustomerId(customerId)).thenReturn(policies);

    // When & Then
    mockMvc.perform(get("/customers/{id}/policies", customerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(1)))
        .andExpect(jsonPath("$[0].id", is(101)));
  }

  @Test
  @DisplayName("GET /customers/{id}/policies - Should return 404 Not Found when customer does not exist")
  void getPoliciesByCustomerId_whenCustomerNotExists_shouldReturnNotFound() throws Exception {
    // Given: The policy service will throw an exception
    long nonExistentCustomerId = 99L;
    when(policyService.findPoliciesByCustomerId(nonExistentCustomerId))
        .thenThrow(new ResourceNotFoundException("Customer not found"));

    // When & Then
    mockMvc.perform(get("/customers/{id}/policies", nonExistentCustomerId))
        .andExpect(status().isNotFound());
  }
}
