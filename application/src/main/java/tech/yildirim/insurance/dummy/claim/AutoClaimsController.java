package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.AutoClaimsApi;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;

/**
 * REST Controller for managing auto claims. Implements the generated {@link AutoClaimsApi}
 * interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AutoClaimsController implements AutoClaimsApi {

  private final ClaimService claimService;

  @Override
  public ResponseEntity<AutoClaimDto> createAutoClaim(AutoClaimDto autoClaimDto) {
    log.info("REST request to create auto claim for policy {}", autoClaimDto.getPolicyId());

    AutoClaimDto createdClaim =
        (AutoClaimDto) claimService.submitClaim(autoClaimDto.getPolicyId(), autoClaimDto);

    log.info(
        "Successfully created auto claim with id {} and number {}",
        createdClaim.getId(),
        createdClaim.getClaimNumber());

    return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<AutoClaimDto>> getAllAutoClaims(
      Integer page, Integer size, String status) {
    log.info(
        "REST request to get all auto claims - page: {}, size: {}, status: {}", page, size, status);

    try {
      // Get all auto claims using the service method
      List<ClaimDto> claims = claimService.getAllClaimsByType(ClaimTypeEnum.AUTO_CLAIM_DTO);

      // Cast to AutoClaimDto list (polymorphic relationship ensures this is safe)
      List<AutoClaimDto> autoClaims = claims.stream().map(AutoClaimDto.class::cast).toList();

      // TODO: Implement pagination and status filtering
      // For now, return all auto claims without pagination or status filtering
      if (page != null || size != null || status != null) {
        log.warn(
            "Pagination (page: {}, size: {}) and status filtering (status: {}) not yet implemented",
            page,
            size,
            status);
      }

      log.info("Retrieved {} auto claims", autoClaims.size());
      return ResponseEntity.ok(autoClaims);
    } catch (RuntimeException e) {
      log.error("Error retrieving auto claims: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public ResponseEntity<AutoClaimDto> getAutoClaimById(Long id) {
    log.info("REST request to get auto claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            claimDto -> {
              log.info("Found auto claim with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok((AutoClaimDto) claimDto);
            })
        .orElseGet(
            () -> {
              log.warn("Auto claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<Void> deleteAutoClaim(Long id) {
    log.info("REST request to delete auto claim with id: {}", id);

    // Check if the claim exists first
    return claimService
        .findClaimById(id)
        .map(
            existingClaim -> {
              if (!(existingClaim instanceof AutoClaimDto)) {
                log.warn("Claim with id: {} is not an auto claim", id);
                return ResponseEntity.notFound().<Void>build();
              }

              try {
                claimService.deleteClaim(id);
                log.info("Successfully deleted auto claim with id: {}", id);
                return ResponseEntity.noContent().<Void>build();
              } catch (Exception e) {
                log.error("Error deleting auto claim with id: {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().<Void>build();
              }
            })
        .orElseGet(
            () -> {
              log.warn("Auto claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<AutoClaimDto> updateAutoClaim(Long id, AutoClaimDto autoClaimDto) {
    log.info("REST request to update auto claim with id: {}", id);

    try {
      AutoClaimDto updatedClaim = (AutoClaimDto) claimService.updateClaim(id, autoClaimDto);
      log.info("Successfully updated auto claim with id: {}", id);
      return ResponseEntity.ok(updatedClaim);
    } catch (Exception e) {
      log.warn("Failed to update auto claim with id: {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<AutoClaimDto> assignAdjusterToAutoClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster {} to auto claim {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);

    try {
      var updatedClaim = claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
      log.info(
          "Successfully assigned adjuster {} to auto claim {}",
          assignAdjusterRequestDto.getEmployeeId(),
          id);

      return ResponseEntity.ok((AutoClaimDto) updatedClaim);
    } catch (Exception e) {
      log.warn("Failed to assign adjuster to auto claim {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
