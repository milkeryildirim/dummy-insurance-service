package tech.yildirim.insurance.dummy.partner;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing partner vendor data. Provides methods for vendor lookup and
 * validation operations.
 */
@Repository
public interface PartnerVendorRepository extends JpaRepository<PartnerVendor, Long> {

  /**
   * Finds a partner vendor by their unique vendor code.
   *
   * @param vendorCode the vendor code to search for
   * @return optional containing the vendor if found
   */
  Optional<PartnerVendor> findByVendorCode(String vendorCode);

  /**
   * Finds all vendors of a specific service type.
   *
   * @param vendorType the type of vendor services
   * @return list of vendors matching the type
   */
  List<PartnerVendor> findByVendorType(VendorType vendorType);

  /**
   * Finds all vendors with a specific status.
   *
   * @param status the vendor status to filter by
   * @return list of vendors with matching status
   */
  List<PartnerVendor> findByStatus(VendorStatus status);

  /**
   * Finds vendors by both type and status.
   *
   * @param vendorType the type of vendor services
   * @param status the vendor status
   * @return list of vendors matching both criteria
   */
  List<PartnerVendor> findByVendorTypeAndStatus(VendorType vendorType, VendorStatus status);

  /**
   * Searches for vendors by company name using case-insensitive partial matching.
   *
   * @param name partial company name to search for
   * @return list of vendors with matching company names
   */
  @Query(
      "SELECT pv FROM PartnerVendor pv WHERE LOWER(pv.companyName) LIKE LOWER(CONCAT('%', :name, '%'))")
  List<PartnerVendor> findByCompanyNameContainingIgnoreCase(@Param("name") String name);

  /**
   * Checks if a vendor code already exists in the system.
   *
   * @param vendorCode the vendor code to check
   * @return true if vendor code exists
   */
  boolean existsByVendorCode(String vendorCode);

  /**
   * Checks if combination of company name and vendor code exists.
   *
   * @param companyName the company name
   * @param vendorCode the vendor code
   * @return true if combination exists
   */
  boolean existsByCompanyNameAndVendorCode(String companyName, String vendorCode);
}
