package tech.yildirim.insurance.dummy.partner;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.yildirim.insurance.api.generated.model.PartnerVendorDto;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@WebMvcTest(PartnerVendorsController.class)
@DisplayName("PartnerVendors Controller Web Layer Tests")
class PartnerVendorsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PartnerVendorService partnerVendorService;
  @Autowired private PartnerVendorMapper partnerVendorMapper;
  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class ControllerTestConfig {
    @Bean
    public PartnerVendorService partnerVendorService() {
      return Mockito.mock(PartnerVendorService.class);
    }

    @Bean
    public PartnerVendorMapper partnerVendorMapper() {
      return Mockito.mock(PartnerVendorMapper.class);
    }
  }

  private PartnerVendor testPartnerVendor;
  private PartnerVendorDto testPartnerVendorDto;

  @BeforeEach
  void setUp() {
    testPartnerVendor = new PartnerVendor();
    testPartnerVendor.setId(5001L);
    testPartnerVendor.setVendorCode("VND-AUTO-001");
    testPartnerVendor.setCompanyName("AutoWerkstatt Schmidt & Söhne GmbH");
    testPartnerVendor.setContactPerson("Hans Schmidt");
    testPartnerVendor.setEmail("kontakt@auto-schmidt.de");
    testPartnerVendor.setPhoneNumber("+49 30 555 2001");
    testPartnerVendor.setAddress("Berliner Str. 45");
    testPartnerVendor.setCity("Berlin");
    testPartnerVendor.setPostalCode("10115");
    testPartnerVendor.setCountry("Deutschland");
    testPartnerVendor.setVendorType(VendorType.AUTO_REPAIR);
    testPartnerVendor.setStatus(VendorStatus.ACTIVE);
    testPartnerVendor.setSpecialization("Collision repair, paint work, bodywork");
    testPartnerVendor.setNotes("Preferred partner for auto claims in Berlin area");
    testPartnerVendor.setCreatedAt(ZonedDateTime.now());
    testPartnerVendor.setUpdatedAt(ZonedDateTime.now());

    testPartnerVendorDto = new PartnerVendorDto();
    testPartnerVendorDto.setId(5001L);
    testPartnerVendorDto.setVendorCode("VND-AUTO-001");
    testPartnerVendorDto.setCompanyName("AutoWerkstatt Schmidt & Söhne GmbH");
    testPartnerVendorDto.setContactPerson("Hans Schmidt");
    testPartnerVendorDto.setEmail("kontakt@auto-schmidt.de");
    testPartnerVendorDto.setPhoneNumber("+49 30 555 2001");
    testPartnerVendorDto.setAddress("Berliner Str. 45");
    testPartnerVendorDto.setCity("Berlin");
    testPartnerVendorDto.setPostalCode("10115");
    testPartnerVendorDto.setCountry("Deutschland");
    testPartnerVendorDto.setVendorType(PartnerVendorDto.VendorTypeEnum.AUTO_REPAIR);
    testPartnerVendorDto.setStatus(PartnerVendorDto.StatusEnum.ACTIVE);
    testPartnerVendorDto.setSpecialization("Collision repair, paint work, bodywork");
    testPartnerVendorDto.setNotes("Preferred partner for auto claims in Berlin area");
  }

  // ========== CREATE PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("POST /partner-vendors - Should create partner vendor and return 201 Created")
  void createPartnerVendor_withValidData_shouldReturn201() throws Exception {
    PartnerVendorDto inputDto = new PartnerVendorDto();
    inputDto.setVendorCode("VND-TEST-001");
    inputDto.setCompanyName("Test Company GmbH");
    inputDto.setContactPerson("Test Person");
    inputDto.setEmail("test@company.de");
    inputDto.setVendorType(PartnerVendorDto.VendorTypeEnum.AUTO_REPAIR);
    inputDto.setStatus(PartnerVendorDto.StatusEnum.ACTIVE);

    when(partnerVendorMapper.toNewEntity(any(PartnerVendorDto.class)))
        .thenReturn(testPartnerVendor);
    when(partnerVendorService.createPartnerVendor(any(PartnerVendor.class)))
        .thenReturn(testPartnerVendor);
    when(partnerVendorMapper.toDto(any(PartnerVendor.class))).thenReturn(testPartnerVendorDto);

    mockMvc
        .perform(
            post("/partner-vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(5001)))
        .andExpect(jsonPath("$.vendorCode", is("VND-AUTO-001")))
        .andExpect(jsonPath("$.companyName", is("AutoWerkstatt Schmidt & Söhne GmbH")))
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }

  @Test
  @DisplayName("POST /partner-vendors - Should return 400 when vendor code already exists")
  void createPartnerVendor_whenVendorCodeExists_shouldReturn400() throws Exception {
    PartnerVendorDto inputDto = new PartnerVendorDto();
    inputDto.setVendorCode("VND-AUTO-001");
    inputDto.setCompanyName("Test Company GmbH");
    inputDto.setVendorType(PartnerVendorDto.VendorTypeEnum.AUTO_REPAIR);
    inputDto.setStatus(PartnerVendorDto.StatusEnum.ACTIVE);

    when(partnerVendorMapper.toNewEntity(any(PartnerVendorDto.class)))
        .thenReturn(testPartnerVendor);
    when(partnerVendorService.createPartnerVendor(any(PartnerVendor.class)))
        .thenThrow(new IllegalArgumentException("Vendor code already exists"));

    mockMvc
        .perform(
            post("/partner-vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isBadRequest());
  }

  // ========== GET ALL PARTNER VENDORS TESTS ==========

  @Test
  @DisplayName("GET /partner-vendors - Should return paginated list of partner vendors")
  void getAllPartnerVendors_shouldReturnPaginatedList() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    List<PartnerVendor> vendors = List.of(testPartnerVendor);
    Page<PartnerVendor> page = new PageImpl<>(vendors, pageable, 1);

    when(partnerVendorService.findAllPartnerVendors(any(Pageable.class))).thenReturn(page);
    when(partnerVendorMapper.toDto(any(PartnerVendor.class))).thenReturn(testPartnerVendorDto);

    mockMvc
        .perform(get("/partner-vendors").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id", is(5001)))
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.totalPages", is(1)))
        .andExpect(jsonPath("$.first", is(true)))
        .andExpect(jsonPath("$.last", is(true)));
  }

  @Test
  @DisplayName("GET /partner-vendors - Should filter by vendor type")
  void getAllPartnerVendors_withVendorTypeFilter_shouldReturnFilteredList() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    List<PartnerVendor> vendors = List.of(testPartnerVendor);
    Page<PartnerVendor> page = new PageImpl<>(vendors, pageable, 1);

    when(partnerVendorService.findPartnerVendorsByType(VendorType.AUTO_REPAIR)).thenReturn(vendors);
    when(partnerVendorService.findAllPartnerVendors(any(Pageable.class))).thenReturn(page);
    when(partnerVendorMapper.toDto(any(PartnerVendor.class))).thenReturn(testPartnerVendorDto);

    mockMvc
        .perform(
            get("/partner-vendors")
                .param("vendorType", "AUTO_REPAIR")
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].vendorType", is("AUTO_REPAIR")));
  }

  @Test
  @DisplayName("GET /partner-vendors - Should return 400 for invalid vendor type")
  void getAllPartnerVendors_withInvalidVendorType_shouldReturn400() throws Exception {
    mockMvc
        .perform(get("/partner-vendors").param("vendorType", "INVALID_TYPE"))
        .andExpect(status().isBadRequest());
  }

  // ========== GET PARTNER VENDOR BY ID TESTS ==========

  @Test
  @DisplayName("GET /partner-vendors/{id} - Should return partner vendor when found")
  void getPartnerVendorById_whenExists_shouldReturnPartnerVendor() throws Exception {
    when(partnerVendorService.findPartnerVendorById(5001L))
        .thenReturn(Optional.of(testPartnerVendor));
    when(partnerVendorMapper.toDto(testPartnerVendor)).thenReturn(testPartnerVendorDto);

    mockMvc
        .perform(get("/partner-vendors/{id}", 5001L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(5001)))
        .andExpect(jsonPath("$.vendorCode", is("VND-AUTO-001")))
        .andExpect(jsonPath("$.companyName", is("AutoWerkstatt Schmidt & Söhne GmbH")));
  }

  @Test
  @DisplayName("GET /partner-vendors/{id} - Should return 404 when not found")
  void getPartnerVendorById_whenNotExists_shouldReturn404() throws Exception {
    when(partnerVendorService.findPartnerVendorById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/partner-vendors/{id}", 999L)).andExpect(status().isNotFound());
  }

  // ========== UPDATE PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("PUT /partner-vendors/{id} - Should update partner vendor and return updated data")
  void updatePartnerVendor_whenExists_shouldReturnUpdatedPartnerVendor() throws Exception {
    PartnerVendorDto updateDto =
        new PartnerVendorDto()
            .vendorCode("VND-AUTO-001-UPDATED")
            .companyName("Updated Company Name")
            .contactPerson("Test Person")
            .email("test@test.com")
            .vendorType(PartnerVendorDto.VendorTypeEnum.AUTO_REPAIR)
            .status(PartnerVendorDto.StatusEnum.ACTIVE);

    when(partnerVendorMapper.toEntity(any(PartnerVendorDto.class))).thenReturn(testPartnerVendor);
    when(partnerVendorService.updatePartnerVendor(eq(5001L), any(PartnerVendor.class)))
        .thenReturn(testPartnerVendor);
    when(partnerVendorMapper.toDto(any(PartnerVendor.class))).thenReturn(testPartnerVendorDto);

    mockMvc
        .perform(
            put("/partner-vendors/{id}", 5001L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(5001)))
        .andExpect(jsonPath("$.vendorCode", is("VND-AUTO-001")));
  }

  @Test
  @DisplayName("PUT /partner-vendors/{id} - Should return 404 when partner vendor not found")
  void updatePartnerVendor_whenNotExists_shouldReturn404() throws Exception {
    PartnerVendorDto updateDto =
        new PartnerVendorDto()
            .vendorCode("VND-TEST-001")
            .companyName("Test Company")
            .vendorType(PartnerVendorDto.VendorTypeEnum.AUTO_REPAIR)
            .contactPerson("Test Person")
            .email("test@test.com")
            .status(PartnerVendorDto.StatusEnum.ACTIVE);

    when(partnerVendorMapper.toEntity(any(PartnerVendorDto.class))).thenReturn(testPartnerVendor);
    when(partnerVendorService.updatePartnerVendor(eq(999L), any(PartnerVendor.class)))
        .thenThrow(new ResourceNotFoundException("Partner vendor not found"));

    mockMvc
        .perform(
            put("/partner-vendors/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  // ========== DELETE PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("DELETE /partner-vendors/{id} - Should delete partner vendor and return 204")
  void deletePartnerVendor_whenExists_shouldReturn204() throws Exception {
    doNothing().when(partnerVendorService).deletePartnerVendor(5001L);

    mockMvc.perform(delete("/partner-vendors/{id}", 5001L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /partner-vendors/{id} - Should return 404 when partner vendor not found")
  void deletePartnerVendor_whenNotExists_shouldReturn404() throws Exception {
    doThrow(new ResourceNotFoundException("Partner vendor not found"))
        .when(partnerVendorService)
        .deletePartnerVendor(999L);

    mockMvc.perform(delete("/partner-vendors/{id}", 999L)).andExpect(status().isNotFound());
  }

  // ========== UPDATE STATUS TESTS ==========

  @Test
  @DisplayName(
      "PATCH /partner-vendors/{id}/status - Should update status and return updated vendor")
  void updatePartnerVendorStatus_whenValid_shouldReturnUpdatedVendor() throws Exception {
    String statusUpdateJson = "{ \"status\": \"INACTIVE\" }";

    when(partnerVendorService.updatePartnerVendorStatus(eq(5001L), eq(VendorStatus.INACTIVE)))
        .thenReturn(testPartnerVendor);
    when(partnerVendorMapper.toDto(testPartnerVendor)).thenReturn(testPartnerVendorDto);

    mockMvc
        .perform(
            patch("/partner-vendors/{id}/status", 5001L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(5001)))
        .andExpect(jsonPath("$.vendorCode", is("VND-AUTO-001")));
  }

  @Test
  @DisplayName(
      "PATCH /partner-vendors/{id}/status - Should return 404 when partner vendor not found")
  void updatePartnerVendorStatus_whenNotExists_shouldReturn404() throws Exception {
    String statusUpdateJson = "{ \"status\": \"INACTIVE\" }";

    when(partnerVendorService.updatePartnerVendorStatus(eq(999L), any(VendorStatus.class)))
        .thenThrow(new ResourceNotFoundException("Partner vendor not found"));

    mockMvc
        .perform(
            patch("/partner-vendors/{id}/status", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson))
        .andExpect(status().isNotFound());
  }

  // ========== SEARCH TESTS ==========

  @Test
  @DisplayName("GET /partner-vendors/search - Should search by company name")
  void searchPartnerVendors_byCompanyName_shouldReturnMatchingVendors() throws Exception {
    List<PartnerVendor> vendors = List.of(testPartnerVendor);
    when(partnerVendorService.searchPartnerVendorsByCompanyName("AutoWerkstatt"))
        .thenReturn(vendors);
    when(partnerVendorMapper.toDtoList(vendors)).thenReturn(List.of(testPartnerVendorDto));

    mockMvc
        .perform(get("/partner-vendors/search").param("companyName", "AutoWerkstatt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id", is(5001)))
        .andExpect(jsonPath("$[0].companyName", is("AutoWerkstatt Schmidt & Söhne GmbH")));
  }

  @Test
  @DisplayName("GET /partner-vendors/search - Should search by vendor code")
  void searchPartnerVendors_byVendorCode_shouldReturnMatchingVendor() throws Exception {
    when(partnerVendorService.findPartnerVendorByCode("VND-AUTO-001"))
        .thenReturn(Optional.of(testPartnerVendor));
    when(partnerVendorMapper.toDtoList(List.of(testPartnerVendor)))
        .thenReturn(List.of(testPartnerVendorDto));

    mockMvc
        .perform(get("/partner-vendors/search").param("vendorCode", "VND-AUTO-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].vendorCode", is("VND-AUTO-001")));
  }

  @Test
  @DisplayName("GET /partner-vendors/search - Should search by vendor type")
  void searchPartnerVendors_byVendorType_shouldReturnMatchingVendors() throws Exception {
    List<PartnerVendor> vendors = List.of(testPartnerVendor);
    when(partnerVendorService.findPartnerVendorsByType(VendorType.AUTO_REPAIR)).thenReturn(vendors);
    when(partnerVendorMapper.toDtoList(vendors)).thenReturn(List.of(testPartnerVendorDto));

    mockMvc
        .perform(get("/partner-vendors/search").param("vendorType", "AUTO_REPAIR"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].vendorType", is("AUTO_REPAIR")));
  }

  @Test
  @DisplayName("GET /partner-vendors/search - Should search active vendors only")
  void searchPartnerVendors_activeOnly_shouldReturnActiveVendorsOnly() throws Exception {
    List<PartnerVendor> vendors = List.of(testPartnerVendor);
    when(partnerVendorService.findPartnerVendorsByStatus(VendorStatus.ACTIVE)).thenReturn(vendors);
    when(partnerVendorMapper.toDtoList(vendors)).thenReturn(List.of(testPartnerVendorDto));

    mockMvc
        .perform(get("/partner-vendors/search").param("activeOnly", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].status", is("ACTIVE")));
  }

  @Test
  @DisplayName("GET /partner-vendors/search - Should return 400 when no search criteria provided")
  void searchPartnerVendors_withoutCriteria_shouldReturn400() throws Exception {
    mockMvc.perform(get("/partner-vendors/search")).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /partner-vendors/search - Should return 400 for invalid vendor type")
  void searchPartnerVendors_withInvalidVendorType_shouldReturn400() throws Exception {
    mockMvc
        .perform(get("/partner-vendors/search").param("vendorType", "INVALID_TYPE"))
        .andExpect(status().isBadRequest());
  }

  // ========== EDGE CASES ==========

  @Test
  @DisplayName("Should handle empty search results")
  void searchPartnerVendors_withNoResults_shouldReturnEmptyArray() throws Exception {
    when(partnerVendorService.searchPartnerVendorsByCompanyName("NonExistent"))
        .thenReturn(List.of());
    when(partnerVendorMapper.toDtoList(List.of())).thenReturn(List.of());

    mockMvc
        .perform(get("/partner-vendors/search").param("companyName", "NonExistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("Should handle service exceptions gracefully")
  void getAllPartnerVendors_whenServiceThrowsException_shouldReturn500() throws Exception {
    when(partnerVendorService.findAllPartnerVendors(any(Pageable.class)))
        .thenThrow(new RuntimeException("Database connection failed"));

    mockMvc.perform(get("/partner-vendors")).andExpect(status().isInternalServerError());
  }
}
