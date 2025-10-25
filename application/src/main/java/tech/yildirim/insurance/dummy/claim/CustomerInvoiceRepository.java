package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for {@link CustomerInvoice} entity. */
@Repository
public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {

  /**
   * Find all customer invoices for a specific claim.
   *
   * @param claimId The ID of the claim.
   * @return List of customer invoices for the claim.
   */
  List<CustomerInvoice> findByClaimId(Long claimId);

  /**
   * Find a specific customer invoice by claim ID and invoice ID.
   *
   * @param claimId The ID of the claim.
   * @param invoiceId The ID of the invoice.
   * @return The customer invoice if found.
   */
  @Query("SELECT ci FROM CustomerInvoice ci WHERE ci.claim.id = :claimId AND ci.id = :invoiceId")
  CustomerInvoice findByClaimIdAndId(
      @Param("claimId") Long claimId, @Param("invoiceId") Long invoiceId);
}
