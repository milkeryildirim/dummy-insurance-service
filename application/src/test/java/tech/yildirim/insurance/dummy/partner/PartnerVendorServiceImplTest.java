package tech.yildirim.insurance.dummy.partner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PartnerVendorService Implementation Tests")
class PartnerVendorServiceImplTest {

  @Mock private PartnerVendorRepository partnerVendorRepository;

  @InjectMocks private PartnerVendorServiceImpl partnerVendorService;

  private PartnerVendor testPartnerVendor;
  private PartnerVendor savedPartnerVendor;

  @BeforeEach
  void setUp() {
    testPartnerVendor = new PartnerVendor();
    testPartnerVendor.setVendorCode("VND-TEST-001");
    testPartnerVendor.setCompanyName("Test Vendor GmbH");
    testPartnerVendor.setContactPerson("John Doe");
    testPartnerVendor.setEmail("john.doe@testvendor.com");
    testPartnerVendor.setPhoneNumber("+49 30 555 1234");
    testPartnerVendor.setAddress("Test Street 123");
    testPartnerVendor.setCity("Berlin");
    testPartnerVendor.setPostalCode("10115");
    testPartnerVendor.setCountry("Deutschland");
    testPartnerVendor.setVendorType(VendorType.AUTO_REPAIR);
    testPartnerVendor.setStatus(VendorStatus.ACTIVE);
    testPartnerVendor.setSpecialization("Collision repair and paint work");
    testPartnerVendor.setNotes("Test vendor for unit testing");

    savedPartnerVendor = new PartnerVendor();
    savedPartnerVendor.setId(1L);
    savedPartnerVendor.setVendorCode(testPartnerVendor.getVendorCode());
    savedPartnerVendor.setCompanyName(testPartnerVendor.getCompanyName());
    savedPartnerVendor.setContactPerson(testPartnerVendor.getContactPerson());
    savedPartnerVendor.setEmail(testPartnerVendor.getEmail());
    savedPartnerVendor.setPhoneNumber(testPartnerVendor.getPhoneNumber());
    savedPartnerVendor.setAddress(testPartnerVendor.getAddress());
    savedPartnerVendor.setCity(testPartnerVendor.getCity());
    savedPartnerVendor.setPostalCode(testPartnerVendor.getPostalCode());
    savedPartnerVendor.setCountry(testPartnerVendor.getCountry());
    savedPartnerVendor.setVendorType(testPartnerVendor.getVendorType());
    savedPartnerVendor.setStatus(testPartnerVendor.getStatus());
    savedPartnerVendor.setSpecialization(testPartnerVendor.getSpecialization());
    savedPartnerVendor.setNotes(testPartnerVendor.getNotes());
    savedPartnerVendor.setCreatedAt(ZonedDateTime.now());
    savedPartnerVendor.setUpdatedAt(ZonedDateTime.now());
  }

  // ========== CREATE PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("Should create partner vendor successfully when vendor code is unique")
  void createPartnerVendor_whenVendorCodeIsUnique_shouldReturnSavedVendor() {
    when(partnerVendorRepository.existsByVendorCode(testPartnerVendor.getVendorCode()))
        .thenReturn(false);
    when(partnerVendorRepository.save(testPartnerVendor)).thenReturn(savedPartnerVendor);

    PartnerVendor result = partnerVendorService.createPartnerVendor(testPartnerVendor);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getVendorCode()).isEqualTo("VND-TEST-001");
    assertThat(result.getCompanyName()).isEqualTo("Test Vendor GmbH");

    verify(partnerVendorRepository).existsByVendorCode("VND-TEST-001");
    verify(partnerVendorRepository).save(testPartnerVendor);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when vendor code already exists")
  void createPartnerVendor_whenVendorCodeExists_shouldThrowException() {
    when(partnerVendorRepository.existsByVendorCode(testPartnerVendor.getVendorCode()))
        .thenReturn(true);

    assertThatThrownBy(() -> partnerVendorService.createPartnerVendor(testPartnerVendor))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Vendor code already exists: VND-TEST-001");

    verify(partnerVendorRepository).existsByVendorCode("VND-TEST-001");
    verify(partnerVendorRepository, times(0)).save(any());
  }

  // ========== FIND PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("Should return partner vendor when found by ID")
  void findPartnerVendorById_whenExists_shouldReturnVendor() {
    when(partnerVendorRepository.findById(1L)).thenReturn(Optional.of(savedPartnerVendor));

    Optional<PartnerVendor> result = partnerVendorService.findPartnerVendorById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
    assertThat(result.get().getCompanyName()).isEqualTo("Test Vendor GmbH");

    verify(partnerVendorRepository).findById(1L);
  }

  @Test
  @DisplayName("Should return empty when partner vendor not found by ID")
  void findPartnerVendorById_whenNotExists_shouldReturnEmpty() {
    when(partnerVendorRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<PartnerVendor> result = partnerVendorService.findPartnerVendorById(999L);

    assertThat(result).isEmpty();
    verify(partnerVendorRepository).findById(999L);
  }

  @Test
  @DisplayName("Should return partner vendor when found by vendor code")
  void findPartnerVendorByCode_whenExists_shouldReturnVendor() {
    when(partnerVendorRepository.findByVendorCode("VND-TEST-001"))
        .thenReturn(Optional.of(savedPartnerVendor));

    Optional<PartnerVendor> result = partnerVendorService.findPartnerVendorByCode("VND-TEST-001");

    assertThat(result).isPresent();
    assertThat(result.get().getVendorCode()).isEqualTo("VND-TEST-001");

    verify(partnerVendorRepository).findByVendorCode("VND-TEST-001");
  }

  @Test
  @DisplayName("Should return empty when partner vendor not found by code")
  void findPartnerVendorByCode_whenNotExists_shouldReturnEmpty() {
    when(partnerVendorRepository.findByVendorCode("NON-EXISTENT")).thenReturn(Optional.empty());

    Optional<PartnerVendor> result = partnerVendorService.findPartnerVendorByCode("NON-EXISTENT");

    assertThat(result).isEmpty();
    verify(partnerVendorRepository).findByVendorCode("NON-EXISTENT");
  }

  // ========== FIND ALL PARTNER VENDORS TESTS ==========

  @Test
  @DisplayName("Should return paginated list of partner vendors")
  void findAllPartnerVendors_shouldReturnPaginatedList() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PartnerVendor> page = new PageImpl<>(List.of(savedPartnerVendor), pageable, 1);

    when(partnerVendorRepository.findAll(pageable)).thenReturn(page);

    Page<PartnerVendor> result = partnerVendorService.findAllPartnerVendors(pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);

    verify(partnerVendorRepository).findAll(pageable);
  }

  // ========== FIND BY TYPE AND STATUS TESTS ==========

  @Test
  @DisplayName("Should return vendors by type")
  void findPartnerVendorsByType_shouldReturnFilteredList() {
    when(partnerVendorRepository.findByVendorType(VendorType.AUTO_REPAIR))
        .thenReturn(List.of(savedPartnerVendor));

    List<PartnerVendor> result =
        partnerVendorService.findPartnerVendorsByType(VendorType.AUTO_REPAIR);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getVendorType()).isEqualTo(VendorType.AUTO_REPAIR);

    verify(partnerVendorRepository).findByVendorType(VendorType.AUTO_REPAIR);
  }

  @Test
  @DisplayName("Should return vendors by status")
  void findPartnerVendorsByStatus_shouldReturnFilteredList() {
    when(partnerVendorRepository.findByStatus(VendorStatus.ACTIVE))
        .thenReturn(List.of(savedPartnerVendor));

    List<PartnerVendor> result =
        partnerVendorService.findPartnerVendorsByStatus(VendorStatus.ACTIVE);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getStatus()).isEqualTo(VendorStatus.ACTIVE);

    verify(partnerVendorRepository).findByStatus(VendorStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should return active vendors by type")
  void findActivePartnerVendorsByType_shouldReturnActiveVendorsOfType() {
    when(partnerVendorRepository.findByVendorTypeAndStatus(
            VendorType.AUTO_REPAIR, VendorStatus.ACTIVE))
        .thenReturn(List.of(savedPartnerVendor));

    List<PartnerVendor> result =
        partnerVendorService.findActivePartnerVendorsByType(VendorType.AUTO_REPAIR);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getVendorType()).isEqualTo(VendorType.AUTO_REPAIR);
    assertThat(result.getFirst().getStatus()).isEqualTo(VendorStatus.ACTIVE);

    verify(partnerVendorRepository)
        .findByVendorTypeAndStatus(VendorType.AUTO_REPAIR, VendorStatus.ACTIVE);
  }

  // ========== SEARCH TESTS ==========

  @Test
  @DisplayName("Should return vendors matching company name search")
  void searchPartnerVendorsByCompanyName_shouldReturnMatchingVendors() {
    when(partnerVendorRepository.findByCompanyNameContainingIgnoreCase("Test"))
        .thenReturn(List.of(savedPartnerVendor));

    List<PartnerVendor> result = partnerVendorService.searchPartnerVendorsByCompanyName("Test");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getCompanyName()).contains("Test");

    verify(partnerVendorRepository).findByCompanyNameContainingIgnoreCase("Test");
  }

  // ========== UPDATE PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("Should update partner vendor successfully when exists")
  void updatePartnerVendor_whenExists_shouldReturnUpdatedVendor() {
    PartnerVendor updateData = new PartnerVendor();
    updateData.setVendorCode("VND-TEST-001-UPDATED");
    updateData.setCompanyName("Updated Test Vendor GmbH");
    updateData.setContactPerson("Jane Doe");
    updateData.setEmail("jane.doe@testvendor.com");
    updateData.setVendorType(VendorType.MEDICAL_PROVIDER);
    updateData.setStatus(VendorStatus.INACTIVE);

    when(partnerVendorRepository.findById(1L)).thenReturn(Optional.of(savedPartnerVendor));
    when(partnerVendorRepository.save(any(PartnerVendor.class))).thenReturn(savedPartnerVendor);

    PartnerVendor result = partnerVendorService.updatePartnerVendor(1L, updateData);

    assertThat(result).isNotNull();
    verify(partnerVendorRepository).findById(1L);
    verify(partnerVendorRepository).save(any(PartnerVendor.class));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when updating non-existent vendor")
  void updatePartnerVendor_whenNotExists_shouldThrowException() {
    when(partnerVendorRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> partnerVendorService.updatePartnerVendor(999L, testPartnerVendor))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Partner vendor not found with ID: 999");

    verify(partnerVendorRepository).findById(999L);
    verify(partnerVendorRepository, times(0)).save(any());
  }

  // ========== UPDATE STATUS TESTS ==========

  @Test
  @DisplayName("Should update vendor status successfully when exists")
  void updatePartnerVendorStatus_whenExists_shouldReturnUpdatedVendor() {
    when(partnerVendorRepository.findById(1L)).thenReturn(Optional.of(savedPartnerVendor));
    when(partnerVendorRepository.save(any(PartnerVendor.class))).thenReturn(savedPartnerVendor);

    PartnerVendor result =
        partnerVendorService.updatePartnerVendorStatus(1L, VendorStatus.SUSPENDED);

    assertThat(result).isNotNull();
    verify(partnerVendorRepository).findById(1L);
    verify(partnerVendorRepository).save(any(PartnerVendor.class));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when updating status of non-existent vendor")
  void updatePartnerVendorStatus_whenNotExists_shouldThrowException() {
    when(partnerVendorRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> partnerVendorService.updatePartnerVendorStatus(999L, VendorStatus.SUSPENDED))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Partner vendor not found with ID: 999");

    verify(partnerVendorRepository).findById(999L);
    verify(partnerVendorRepository, times(0)).save(any());
  }

  // ========== DELETE PARTNER VENDOR TESTS ==========

  @Test
  @DisplayName("Should delete partner vendor successfully when exists")
  void deletePartnerVendor_whenExists_shouldDeleteSuccessfully() {
    when(partnerVendorRepository.existsById(1L)).thenReturn(true);
    doNothing().when(partnerVendorRepository).deleteById(1L);

    partnerVendorService.deletePartnerVendor(1L);

    verify(partnerVendorRepository).existsById(1L);
    verify(partnerVendorRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when deleting non-existent vendor")
  void deletePartnerVendor_whenNotExists_shouldThrowException() {
    when(partnerVendorRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> partnerVendorService.deletePartnerVendor(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Partner vendor not found with ID: 999");

    verify(partnerVendorRepository).existsById(999L);
    verify(partnerVendorRepository, times(0)).deleteById(any());
  }

  // ========== EXISTS BY VENDOR CODE TESTS ==========

  @Test
  @DisplayName("Should return true when vendor code exists")
  void existsByVendorCode_whenExists_shouldReturnTrue() {
    when(partnerVendorRepository.existsByVendorCode("VND-TEST-001")).thenReturn(true);

    boolean result = partnerVendorService.existsByVendorCode("VND-TEST-001");

    assertThat(result).isTrue();
    verify(partnerVendorRepository).existsByVendorCode("VND-TEST-001");
  }

  @Test
  @DisplayName("Should return false when vendor code does not exist")
  void existsByVendorCode_whenNotExists_shouldReturnFalse() {
    when(partnerVendorRepository.existsByVendorCode("NON-EXISTENT")).thenReturn(false);

    boolean result = partnerVendorService.existsByVendorCode("NON-EXISTENT");

    assertThat(result).isFalse();
    verify(partnerVendorRepository).existsByVendorCode("NON-EXISTENT");
  }

  // ========== VALIDATION TESTS ==========

  @Test
  @DisplayName("Should return true when company is known active partner")
  void isKnownPartner_whenCompanyIsActivePartner_shouldReturnTrue() {
    PartnerVendor activeVendor = new PartnerVendor();
    activeVendor.setCompanyName("Test Vendor GmbH");
    activeVendor.setStatus(VendorStatus.ACTIVE);

    when(partnerVendorRepository.findByCompanyNameContainingIgnoreCase("Test Vendor GmbH"))
        .thenReturn(List.of(activeVendor));

    boolean result = partnerVendorService.isKnownPartner("Test Vendor GmbH");

    assertThat(result).isTrue();
    verify(partnerVendorRepository).findByCompanyNameContainingIgnoreCase("Test Vendor GmbH");
  }

  @Test
  @DisplayName("Should return false when company is inactive partner")
  void isKnownPartner_whenCompanyIsInactivePartner_shouldReturnFalse() {
    PartnerVendor inactiveVendor = new PartnerVendor();
    inactiveVendor.setCompanyName("Test Vendor GmbH");
    inactiveVendor.setStatus(VendorStatus.INACTIVE);

    when(partnerVendorRepository.findByCompanyNameContainingIgnoreCase("Test Vendor GmbH"))
        .thenReturn(List.of(inactiveVendor));

    boolean result = partnerVendorService.isKnownPartner("Test Vendor GmbH");

    assertThat(result).isFalse();
    verify(partnerVendorRepository).findByCompanyNameContainingIgnoreCase("Test Vendor GmbH");
  }

  @Test
  @DisplayName("Should return false when company name is null or empty")
  void isKnownPartner_whenCompanyNameIsNullOrEmpty_shouldReturnFalse() {
    boolean resultNull = partnerVendorService.isKnownPartner(null);
    boolean resultEmpty = partnerVendorService.isKnownPartner("");
    boolean resultWhitespace = partnerVendorService.isKnownPartner("   ");

    assertThat(resultNull).isFalse();
    assertThat(resultEmpty).isFalse();
    assertThat(resultWhitespace).isFalse();
  }

  @Test
  @DisplayName("Should return true when company is known active partner of specified type")
  void isKnownPartnerOfType_whenCompanyIsActivePartnerOfType_shouldReturnTrue() {
    PartnerVendor autoRepairVendor = new PartnerVendor();
    autoRepairVendor.setCompanyName("Test Auto Repair");
    autoRepairVendor.setVendorType(VendorType.AUTO_REPAIR);
    autoRepairVendor.setStatus(VendorStatus.ACTIVE);

    when(partnerVendorRepository.findByVendorTypeAndStatus(
            VendorType.AUTO_REPAIR, VendorStatus.ACTIVE))
        .thenReturn(List.of(autoRepairVendor));

    boolean result =
        partnerVendorService.isKnownPartnerOfType("Test Auto Repair", VendorType.AUTO_REPAIR);

    assertThat(result).isTrue();
    verify(partnerVendorRepository)
        .findByVendorTypeAndStatus(VendorType.AUTO_REPAIR, VendorStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should return false when company is not found in specified type")
  void isKnownPartnerOfType_whenCompanyNotFoundInType_shouldReturnFalse() {
    when(partnerVendorRepository.findByVendorTypeAndStatus(
            VendorType.MEDICAL_PROVIDER, VendorStatus.ACTIVE))
        .thenReturn(List.of());

    boolean result =
        partnerVendorService.isKnownPartnerOfType("Test Auto Repair", VendorType.MEDICAL_PROVIDER);

    assertThat(result).isFalse();
    verify(partnerVendorRepository)
        .findByVendorTypeAndStatus(VendorType.MEDICAL_PROVIDER, VendorStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should return false when parameters are null")
  void isKnownPartnerOfType_whenParametersAreNull_shouldReturnFalse() {
    boolean resultNullCompany =
        partnerVendorService.isKnownPartnerOfType(null, VendorType.AUTO_REPAIR);
    boolean resultNullType = partnerVendorService.isKnownPartnerOfType("Test Company", null);
    boolean resultBothNull = partnerVendorService.isKnownPartnerOfType(null, null);

    assertThat(resultNullCompany).isFalse();
    assertThat(resultNullType).isFalse();
    assertThat(resultBothNull).isFalse();
  }
}
