package tech.yildirim.insurance.dummy.employee;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.yildirim.insurance.api.generated.model.EmployeeDto;
import tech.yildirim.insurance.dummy.policy.PolicyType;

@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Web Layer Tests")
class EmployeeControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private EmployeeService employeeService;

  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public EmployeeService employeeService() {
      return Mockito.mock(EmployeeService.class);
    }
  }

  @Test
  @DisplayName("GET /employees - Should return a list of employees")
  void getAllEmployees_shouldReturnEmployeeList() throws Exception {
    // Given
    EmployeeDto employee1 = new EmployeeDto().id(1L).employeeId("EMP-001");
    EmployeeDto employee2 = new EmployeeDto().id(2L).employeeId("EMP-002");
    when(employeeService.findAllEmployees()).thenReturn(List.of(employee1, employee2));

    // When & Then
    mockMvc
        .perform(get("/employees"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].employeeId", is("EMP-001")));
  }

  @Test
  @DisplayName("GET /employees/{id} - Should return 404 Not Found when employee does not exist")
  void getEmployeeById_whenNotExists_shouldReturnNotFound() throws Exception {
    // Given
    when(employeeService.findEmployeeById(99L)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/employees/{id}", 99L)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /employees - Should create employee and return 201 Created for valid data")
  void createEmployee_withValidData_shouldReturnCreated() throws Exception {
    // Given: A valid DTO for creation (all required fields are present)
    EmployeeDto inputDto =
        new EmployeeDto()
            .employeeId("EMP-003")
            .firstName("New")
            .lastName("Employee")
            .email("new.employee@insurance.com")
            .phoneNumber("0555555555")
            .employmentType(EmployeeDto.EmploymentTypeEnum.EXTERNAL)
            .role(EmployeeDto.RoleEnum.UNDERWRITER)
            .specializationArea(EmployeeDto.SpecializationAreaEnum.HEALTH)
            .availabilityStatus(EmployeeDto.AvailabilityStatusEnum.AVAILABLE);

    EmployeeDto outputDto = new EmployeeDto().id(3L).employeeId("EMP-003");

    when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(outputDto);

    // When & Then
    mockMvc
        .perform(
            post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(3)));
  }

  @Test
  @DisplayName(
      "POST /employees - Should return 409 Conflict when employee ID or email already exists")
  void createEmployee_whenDataIsDuplicate_shouldReturnConflict() throws Exception {
    // Given: The service will throw a data integrity violation
    EmployeeDto inputDto =
        new EmployeeDto()
            .employeeId("EMP-001")
            .firstName("Duplicate")
            .lastName("User")
            .email("duplicate@insurance.com")
            .phoneNumber("0555555555")
            .employmentType(EmployeeDto.EmploymentTypeEnum.EXTERNAL)
            .availabilityStatus(EmployeeDto.AvailabilityStatusEnum.AVAILABLE)
            .role(EmployeeDto.RoleEnum.CUSTOMER_SUPPORT);

    when(employeeService.createEmployee(any(EmployeeDto.class)))
        .thenThrow(new DataIntegrityViolationException("Employee ID 'EMP-001' already exists."));

    // When & Then: The GlobalExceptionHandler should catch this and return 409
    mockMvc
        .perform(
            post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message", is("Employee ID 'EMP-001' already exists.")));
  }

  @Test
  @DisplayName(
      "GET /employees/available-adjusters - Should return available AUTO adjusters for EXTERNAL employment type")
  void getAvailableAdjustersBySpecialization_withAutoAndExternal_shouldReturnAdjusters()
      throws Exception {
    // Given
    EmployeeDto adjuster1 =
        new EmployeeDto()
            .id(1L)
            .employeeId("EXT-ADJ-AUTO-001")
            .firstName("Michael")
            .lastName("Schmidt")
            .email("michael.schmidt@kfz-gutachter-berlin.de")
            .role(EmployeeDto.RoleEnum.CLAIMS_ADJUSTER)
            .employmentType(EmployeeDto.EmploymentTypeEnum.EXTERNAL)
            .specializationArea(EmployeeDto.SpecializationAreaEnum.AUTO)
            .availabilityStatus(EmployeeDto.AvailabilityStatusEnum.AVAILABLE);

    EmployeeDto adjuster2 =
        new EmployeeDto()
            .id(2L)
            .employeeId("EXT-ADJ-AUTO-002")
            .firstName("Sandra")
            .lastName("Wagner")
            .email("sandra.wagner@fahrzeug-experten-muenchen.de")
            .role(EmployeeDto.RoleEnum.CLAIMS_ADJUSTER)
            .employmentType(EmployeeDto.EmploymentTypeEnum.EXTERNAL)
            .specializationArea(EmployeeDto.SpecializationAreaEnum.AUTO)
            .availabilityStatus(EmployeeDto.AvailabilityStatusEnum.AVAILABLE);

    List<EmployeeDto> expectedAdjusters = Arrays.asList(adjuster1, adjuster2);

    when(employeeService.findAvailableAdjustersBySpecialization(
            PolicyType.AUTO, EmploymentType.EXTERNAL))
        .thenReturn(expectedAdjusters);

    // When & Then
    mockMvc
        .perform(
            get("/employees/available-adjusters")
                .param("specializationArea", "AUTO")
                .param("employmentType", "EXTERNAL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)))
        .andExpect(jsonPath("$[0].employeeId", is("EXT-ADJ-AUTO-001")))
        .andExpect(jsonPath("$[0].specializationArea", is("AUTO")))
        .andExpect(jsonPath("$[0].employmentType", is("EXTERNAL")))
        .andExpect(jsonPath("$[0].availabilityStatus", is("AVAILABLE")))
        .andExpect(jsonPath("$[1].employeeId", is("EXT-ADJ-AUTO-002")))
        .andExpect(jsonPath("$[1].specializationArea", is("AUTO")));
  }

  @Test
  @DisplayName(
      "GET /employees/available-adjusters - Should return empty list when no adjusters available for HOME specialization")
  void getAvailableAdjustersBySpecialization_withHomeAndNoAdjusters_shouldReturnEmptyList()
      throws Exception {
    // Given
    when(employeeService.findAvailableAdjustersBySpecialization(
            PolicyType.HOME, EmploymentType.EXTERNAL))
        .thenReturn(List.of());

    // When & Then
    mockMvc
        .perform(
            get("/employees/available-adjusters")
                .param("specializationArea", "HOME")
                .param("employmentType", "EXTERNAL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(0)));
  }

  @Test
  @DisplayName(
      "GET /employees/available-adjusters - Should use default EXTERNAL when employmentType not provided")
  void getAvailableAdjustersBySpecialization_withDefaultEmploymentType_shouldUseExternal()
      throws Exception {
    // Given
    EmployeeDto adjuster =
        new EmployeeDto()
            .id(1L)
            .employeeId("EXT-ADJ-HEALTH-001")
            .specializationArea(EmployeeDto.SpecializationAreaEnum.HEALTH)
            .employmentType(EmployeeDto.EmploymentTypeEnum.EXTERNAL);

    when(employeeService.findAvailableAdjustersBySpecialization(
            PolicyType.HEALTH, EmploymentType.EXTERNAL))
        .thenReturn(Collections.singletonList(adjuster));

    // When & Then
    mockMvc
        .perform(get("/employees/available-adjusters").param("specializationArea", "HEALTH"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(1)))
        .andExpect(jsonPath("$[0].employeeId", is("EXT-ADJ-HEALTH-001")));
  }

  @Test
  @DisplayName(
      "GET /employees/available-adjusters - Should return 400 Bad Request for missing specializationArea parameter")
  void getAvailableAdjustersBySpecialization_withMissingSpecializationArea_shouldReturnBadRequest()
      throws Exception {
    // When & Then
    mockMvc
        .perform(get("/employees/available-adjusters").param("employmentType", "EXTERNAL"))
        .andExpect(status().isBadRequest());
  }
}
