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

    // TODO: Implement proper pagination and status filtering when service supports it
    // For now, get all claims and filter auto claims manually
    try {
      // This is a simplified implementation - in a real scenario, you would have a service method
      // that specifically retrieves auto claims with pagination and filtering
      log.warn("Pagination and status filtering not yet fully implemented for auto claims");

      // Return empty list for now - this should be replaced with actual service call
      // when getAllAutoClaims method is available in ClaimService
      return ResponseEntity.ok(List.of());
    } catch (Exception e) {
      log.error("Error retrieving auto claims: {}", e.getMessage());
      return ResponseEntity.ok(List.of());
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

              // TODO: Implement delete operation in ClaimService
              log.info(
                  "Auto claim with id: {} would be deleted (delete method not yet implemented in service)",
                  id);
              return ResponseEntity.noContent().<Void>build();
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
