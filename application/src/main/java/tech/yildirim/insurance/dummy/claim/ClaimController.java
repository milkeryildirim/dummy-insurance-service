package tech.yildirim.insurance.dummy.claim;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.ClaimsApi;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;

/**
 * REST Controller for direct management of claims. Implements the generated {@link ClaimsApi}
 * interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClaimController implements ClaimsApi {

  private final ClaimService claimService;

  @Override
  public ResponseEntity<ClaimDto> getClaimById(Long id) {
    log.info("REST request to get claim with id: {}", id);
    return claimService
        .findClaimById(id)
        .map(
            claim -> {
              log.info("Found claim with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok(claim);
            })
        .orElseGet(
            () -> {
              log.warn("Claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<ClaimDto> assignAdjusterToClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster with employeeId: {} to claim with id: {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);
    ClaimDto updatedClaim =
        claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
    log.info("Successfully assigned adjuster to claim {}, returning HTTP 200 OK", id);
    return ResponseEntity.ok(updatedClaim);
  }
}
