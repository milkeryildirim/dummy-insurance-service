package tech.yildirim.insurance.dummy.partner;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

/** Implementation of PartnerVendorService providing partner vendor management operations. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartnerVendorServiceImpl implements PartnerVendorService {

  private final PartnerVendorRepository partnerVendorRepository;

  @Override
  public PartnerVendor createPartnerVendor(PartnerVendor partnerVendor) {
    log.info("Creating new partner vendor with code: {}", partnerVendor.getVendorCode());

    if (partnerVendorRepository.existsByVendorCode(partnerVendor.getVendorCode())) {
      throw new IllegalArgumentException(
          "Vendor code already exists: " + partnerVendor.getVendorCode());
    }

    PartnerVendor savedVendor = partnerVendorRepository.save(partnerVendor);
    log.info("Successfully created partner vendor with ID: {}", savedVendor.getId());
    return savedVendor;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PartnerVendor> findPartnerVendorById(Long id) {
    log.debug("Finding partner vendor by ID: {}", id);
    return partnerVendorRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PartnerVendor> findPartnerVendorByCode(String vendorCode) {
    log.debug("Finding partner vendor by code: {}", vendorCode);
    return partnerVendorRepository.findByVendorCode(vendorCode);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PartnerVendor> findAllPartnerVendors(Pageable pageable) {
    log.debug("Finding all partner vendors with pagination: {}", pageable);
    return partnerVendorRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartnerVendor> findPartnerVendorsByType(VendorType vendorType) {
    log.debug("Finding partner vendors by type: {}", vendorType);
    return partnerVendorRepository.findByVendorType(vendorType);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartnerVendor> findPartnerVendorsByStatus(VendorStatus status) {
    log.debug("Finding partner vendors by status: {}", status);
    return partnerVendorRepository.findByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartnerVendor> findActivePartnerVendorsByType(VendorType vendorType) {
    log.debug("Finding active partner vendors by type: {}", vendorType);
    return partnerVendorRepository.findByVendorTypeAndStatus(vendorType, VendorStatus.ACTIVE);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartnerVendor> searchPartnerVendorsByCompanyName(String companyName) {
    log.debug("Searching partner vendors by company name: {}", companyName);
    return partnerVendorRepository.findByCompanyNameContainingIgnoreCase(companyName);
  }

  @Override
  public PartnerVendor updatePartnerVendor(Long id, PartnerVendor partnerVendor) {
    log.info("Updating partner vendor with ID: {}", id);

    PartnerVendor existingVendor =
        partnerVendorRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Partner vendor not found with ID: " + id));

    // Update fields while preserving ID and timestamps
    existingVendor.setVendorCode(partnerVendor.getVendorCode());
    existingVendor.setCompanyName(partnerVendor.getCompanyName());
    existingVendor.setContactPerson(partnerVendor.getContactPerson());
    existingVendor.setEmail(partnerVendor.getEmail());
    existingVendor.setPhoneNumber(partnerVendor.getPhoneNumber());
    existingVendor.setAddress(partnerVendor.getAddress());
    existingVendor.setCity(partnerVendor.getCity());
    existingVendor.setPostalCode(partnerVendor.getPostalCode());
    existingVendor.setCountry(partnerVendor.getCountry());
    existingVendor.setVendorType(partnerVendor.getVendorType());
    existingVendor.setStatus(partnerVendor.getStatus());
    existingVendor.setSpecialization(partnerVendor.getSpecialization());
    existingVendor.setNotes(partnerVendor.getNotes());

    PartnerVendor updatedVendor = partnerVendorRepository.save(existingVendor);
    log.info("Successfully updated partner vendor with ID: {}", id);
    return updatedVendor;
  }

  @Override
  public PartnerVendor updatePartnerVendorStatus(Long id, VendorStatus status) {
    log.info("Updating partner vendor status for ID: {} to: {}", id, status);

    PartnerVendor vendor =
        partnerVendorRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Partner vendor not found with ID: " + id));

    vendor.setStatus(status);
    PartnerVendor updatedVendor = partnerVendorRepository.save(vendor);

    log.info("Successfully updated partner vendor status for ID: {}", id);
    return updatedVendor;
  }

  @Override
  public void deletePartnerVendor(Long id) {
    log.info("Deleting partner vendor with ID: {}", id);

    if (!partnerVendorRepository.existsById(id)) {
      throw new ResourceNotFoundException("Partner vendor not found with ID: " + id);
    }

    partnerVendorRepository.deleteById(id);
    log.info("Successfully deleted partner vendor with ID: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByVendorCode(String vendorCode) {
    log.debug("Checking if vendor code exists: {}", vendorCode);
    return partnerVendorRepository.existsByVendorCode(vendorCode);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isKnownPartner(String companyName) {
    log.debug("Checking if company is a known partner: {}", companyName);

    if (companyName == null || companyName.trim().isEmpty()) {
      return false;
    }

    List<PartnerVendor> vendors =
        partnerVendorRepository.findByCompanyNameContainingIgnoreCase(companyName.trim());

    // Check for exact match with active status
    boolean isKnown =
        vendors.stream()
            .anyMatch(
                vendor ->
                    vendor.getStatus() == VendorStatus.ACTIVE
                        && vendor.getCompanyName().equalsIgnoreCase(companyName.trim()));

    log.debug("Company {} is known partner: {}", companyName, isKnown);
    return isKnown;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isKnownPartnerOfType(String companyName, VendorType vendorType) {
    log.debug("Checking if company {} is a known partner of type: {}", companyName, vendorType);

    if (companyName == null || companyName.trim().isEmpty() || vendorType == null) {
      return false;
    }

    List<PartnerVendor> vendors =
        partnerVendorRepository.findByVendorTypeAndStatus(vendorType, VendorStatus.ACTIVE);

    // Check for exact match
    boolean isKnown =
        vendors.stream()
            .anyMatch(vendor -> vendor.getCompanyName().equalsIgnoreCase(companyName.trim()));

    log.debug("Company {} is known partner of type {}: {}", companyName, vendorType, isKnown);
    return isKnown;
  }
}
