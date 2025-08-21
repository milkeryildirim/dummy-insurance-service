package tech.yildirim.insurance.dummy.agency;

import java.util.List;
import java.util.Optional;
import tech.yildirim.insurance.api.generated.model.AgencyDto;

/** Service Interface for managing {@link Agency}. */
public interface AgencyService {

  /**
   * Retrieves all agencies.
   *
   * @return A list of all agency DTOs.
   */
  List<AgencyDto> findAllAgencies();

  /**
   * Finds an agency by its ID.
   *
   * @param id The ID of the agency.
   * @return An Optional containing the found agency DTO, or empty if not found.
   */
  Optional<AgencyDto> findAgencyById(Long id);

  /**
   * Creates a new agency.
   *
   * @param agencyDto The DTO containing the data for the new agency.
   * @return The created agency DTO.
   */
  AgencyDto createAgency(AgencyDto agencyDto);
}
