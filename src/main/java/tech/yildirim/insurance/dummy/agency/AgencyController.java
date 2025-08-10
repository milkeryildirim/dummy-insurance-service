package tech.yildirim.insurance.dummy.agency;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class AgencyController implements AgenciesApi {

  private final AgencyService agencyService;

  @Override
  public ResponseEntity<AgencyDto> createAgency(AgencyDto agencyDto) {
    AgencyDto createdAgency = agencyService.createAgency(agencyDto);
    return new ResponseEntity<>(createdAgency, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<AgencyDto>> getAllAgencies() {
    List<AgencyDto> agencies = agencyService.findAllAgencies();
    return ResponseEntity.ok(agencies);
  }

  @Override
  public ResponseEntity<AgencyDto> getAgencyById(Long id) {
    return agencyService
        .findAgencyById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
