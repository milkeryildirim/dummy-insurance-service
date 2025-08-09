package tech.yildirim.insurance.dummy.claim;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.ClaimsApi;
import tech.yildirim.insurance.api.generated.model.ClaimDto;

/**
 * REST Controller for direct management of claims. Implements the generated {@link ClaimsApi}
 * interface.
 */
@RestController
@RequiredArgsConstructor
public class ClaimController implements ClaimsApi {

  private final ClaimService claimService;

  @Override
  public ResponseEntity<ClaimDto> getClaimById(Long id) {
    return claimService
        .findClaimById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
