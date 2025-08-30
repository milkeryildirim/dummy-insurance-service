package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.HealthClaimsApi;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;

/**
 * REST Controller for managing health claims. Implements the generated {@link HealthClaimsApi}
 * interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthClaimsController implements HealthClaimsApi {

  private final ClaimService claimService;

  @Override
  public ResponseEntity<HealthClaimDto> createHealthClaim(HealthClaimDto healthClaimDto) {
    log.info("REST request to create health claim for policy {}", healthClaimDto.getPolicyId());

    HealthClaimDto createdClaim =
        (HealthClaimDto) claimService.submitClaim(healthClaimDto.getPolicyId(), healthClaimDto);

    log.info(
        "Successfully created health claim with id {} and number {}",
        createdClaim.getId(),
        createdClaim.getClaimNumber());

    return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<HealthClaimDto>> getAllHealthClaims(
      Integer page, Integer size, String status) {
    log.info(
        "REST request to get all health claims - page: {}, size: {}, status: {}",
        page,
        size,
        status);

    try {
      // Get all health claims using the service method
      List<ClaimDto> claims = claimService.getAllClaimsByType(ClaimTypeEnum.HEALTH_CLAIM_DTO);

      // Cast to HealthClaimDto list (polymorphic relationship ensures this is safe)
      List<HealthClaimDto> healthClaims = claims.stream().map(HealthClaimDto.class::cast).toList();

      // TODO: Implement pagination and status filtering
      // For now, return all health claims without pagination or status filtering
      if (page != null || size != null || status != null) {
        log.warn(
            "Pagination (page: {}, size: {}) and status filtering (status: {}) not yet implemented",
            page,
            size,
            status);
      }

      log.info("Retrieved {} health claims", healthClaims.size());
      return ResponseEntity.ok(healthClaims);
    } catch (RuntimeException e) {
      log.error("Error retrieving health claims: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public ResponseEntity<HealthClaimDto> getHealthClaimById(Long id) {
    log.info("REST request to get health claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            claimDto -> {
              log.info("Found health claim with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok((HealthClaimDto) claimDto);
            })
        .orElseGet(
            () -> {
              log.warn("Health claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<Void> deleteHealthClaim(Long id) {
    log.info("REST request to delete health claim with id: {}", id);

    // Check if the claim exists first
    return claimService
        .findClaimById(id)
        .map(
            existingClaim -> {
              if (!(existingClaim instanceof HealthClaimDto)) {
                log.warn("Claim with id: {} is not a health claim", id);
                return ResponseEntity.notFound().<Void>build();
              }

              try {
                claimService.deleteClaim(id);
                log.info("Successfully deleted health claim with id: {}", id);
                return ResponseEntity.noContent().<Void>build();
              } catch (Exception e) {
                log.error("Error deleting health claim with id: {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().<Void>build();
              }
            })
        .orElseGet(
            () -> {
              log.warn("Health claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<HealthClaimDto> updateHealthClaim(Long id, HealthClaimDto healthClaimDto) {
    log.info("REST request to update health claim with id: {}", id);

    try {
      HealthClaimDto updatedClaim = (HealthClaimDto) claimService.updateClaim(id, healthClaimDto);
      log.info("Successfully updated health claim with id: {}", id);
      return ResponseEntity.ok(updatedClaim);
    } catch (Exception e) {
      log.warn("Failed to update health claim with id: {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<HealthClaimDto> assignAdjusterToHealthClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster {} to health claim {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);

    try {
      var updatedClaim = claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
      log.info(
          "Successfully assigned adjuster {} to health claim {}",
          assignAdjusterRequestDto.getEmployeeId(),
          id);

      return ResponseEntity.ok((HealthClaimDto) updatedClaim);
    } catch (Exception e) {
      log.warn("Failed to assign adjuster to health claim {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
