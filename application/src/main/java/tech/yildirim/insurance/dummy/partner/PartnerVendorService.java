package tech.yildirim.insurance.dummy.partner;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing partner vendor operations. Provides business logic for vendor
 * management and validation.
 */
public interface PartnerVendorService {

  /**
   * Creates a new partner vendor.
   *
   * @param partnerVendor the vendor to create
   * @return the created vendor with assigned ID
   * @throws IllegalArgumentException if vendor code already exists
   */
  PartnerVendor createPartnerVendor(PartnerVendor partnerVendor);

  /**
   * Retrieves a partner vendor by ID.
   *
   * @param id the vendor ID
   * @return optional containing the vendor if found
   */
  Optional<PartnerVendor> findPartnerVendorById(Long id);

  /**
   * Retrieves a partner vendor by vendor code.
   *
   * @param vendorCode the unique vendor code
   * @return optional containing the vendor if found
   */
  Optional<PartnerVendor> findPartnerVendorByCode(String vendorCode);

  /**
   * Retrieves all partner vendors with pagination.
   *
   * @param pageable pagination information
   * @return paginated list of vendors
   */
  Page<PartnerVendor> findAllPartnerVendors(Pageable pageable);

  /**
   * Retrieves vendors by type.
   *
   * @param vendorType the type of vendor services
   * @return list of vendors matching the type
   */
  List<PartnerVendor> findPartnerVendorsByType(VendorType vendorType);

  /**
   * Retrieves vendors by status.
   *
   * @param status the vendor status
   * @return list of vendors with matching status
   */
  List<PartnerVendor> findPartnerVendorsByStatus(VendorStatus status);

  /**
   * Retrieves active vendors by type.
   *
   * @param vendorType the type of vendor services
   * @return list of active vendors of specified type
   */
  List<PartnerVendor> findActivePartnerVendorsByType(VendorType vendorType);

  /**
   * Searches vendors by company name.
   *
   * @param companyName partial or full company name
   * @return list of vendors with matching company names
   */
  List<PartnerVendor> searchPartnerVendorsByCompanyName(String companyName);

  /**
   * Updates an existing partner vendor.
   *
   * @param id the vendor ID to update
   * @param partnerVendor the updated vendor data
   * @return the updated vendor
   * @throws IllegalArgumentException if vendor not found
   */
  PartnerVendor updatePartnerVendor(Long id, PartnerVendor partnerVendor);

  /**
   * Updates vendor status.
   *
   * @param id the vendor ID
   * @param status the new status
   * @return the updated vendor
   * @throws IllegalArgumentException if vendor not found
   */
  PartnerVendor updatePartnerVendorStatus(Long id, VendorStatus status);

  /**
   * Deletes a partner vendor.
   *
   * @param id the vendor ID to delete
   * @throws IllegalArgumentException if vendor not found
   */
  void deletePartnerVendor(Long id);

  /**
   * Checks if a vendor code already exists.
   *
   * @param vendorCode the vendor code to check
   * @return true if vendor code exists
   */
  boolean existsByVendorCode(String vendorCode);

  /**
   * Validates if a company name matches a known partner vendor. Used for manual invoice validation
   * against partner vendor list.
   *
   * @param companyName the company name from invoice
   * @return true if company is a known active partner
   */
  boolean isKnownPartner(String companyName);

  /**
   * Validates if a company name matches a partner of specific type. Used for targeted validation
   * based on claim type.
   *
   * @param companyName the company name from invoice
   * @param vendorType the expected vendor type
   * @return true if company is a known active partner of specified type
   */
  boolean isKnownPartnerOfType(String companyName, VendorType vendorType);
}
