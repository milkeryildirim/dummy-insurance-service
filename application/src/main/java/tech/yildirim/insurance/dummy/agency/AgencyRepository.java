package tech.yildirim.insurance.dummy.agency;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for the {@link Agency} entity. */
@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {
  /**
   * Finds an agency by its unique business code.
   *
   * @param agencyCode The unique code of the agency.
   * @return An Optional containing the found agency.
   */
  Optional<Agency> findByAgencyCode(String agencyCode);
}
