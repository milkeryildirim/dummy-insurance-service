package tech.yildirim.insurance.dummy.agency;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.AgenciesApi;
import tech.yildirim.insurance.api.generated.model.AgencyDto;

/**
 * REST Controller for managing agencies. Implements the generated {@link AgenciesApi} interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AgencyController implements AgenciesApi {

  private final AgencyService agencyService;

  @Override
  public ResponseEntity<AgencyDto> createAgency(AgencyDto agencyDto) {
    log.info("REST request to create an agency with code: {}", agencyDto.getAgencyCode());
    AgencyDto createdAgency = agencyService.createAgency(agencyDto);
    log.info("Successfully created agency, returning HTTP 201 CREATED");
    return new ResponseEntity<>(createdAgency, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<AgencyDto>> getAllAgencies() {
    log.info("REST request to get all agencies");
    List<AgencyDto> agencies = agencyService.findAllAgencies();
    log.debug("Returning {} agencies", agencies.size());
    return ResponseEntity.ok(agencies);
  }

  @Override
  public ResponseEntity<AgencyDto> getAgencyById(Long id) {
    log.info("REST request to get agency by id: {}", id);
    return agencyService
        .findAgencyById(id)
        .map(
            agency -> {
              log.info("Found agency with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok(agency);
            })
        .orElseGet(
            () -> {
              log.warn("Agency with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }
}
