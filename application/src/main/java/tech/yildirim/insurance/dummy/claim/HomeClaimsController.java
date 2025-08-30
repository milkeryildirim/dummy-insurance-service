package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.api.generated.controller.HomeClaimsApi;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;

/**
 * REST Controller for managing home claims. Implements the generated {@link HomeClaimsApi}
 * interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeClaimsController implements HomeClaimsApi {

  private final ClaimService claimService;

  @Override
  public ResponseEntity<HomeClaimDto> createHomeClaim(HomeClaimDto homeClaimDto) {
    log.info("REST request to create home claim for policy {}", homeClaimDto.getPolicyId());

    HomeClaimDto createdClaim =
        (HomeClaimDto) claimService.submitClaim(homeClaimDto.getPolicyId(), homeClaimDto);

    log.info(
        "Successfully created home claim with id {} and number {}",
        createdClaim.getId(),
        createdClaim.getClaimNumber());

    return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<List<HomeClaimDto>> getAllHomeClaims(
      Integer page, Integer size, String status) {
    log.info(
        "REST request to get all home claims - page: {}, size: {}, status: {}", page, size, status);

    try {
      // Get all home claims using the service method
      List<ClaimDto> claims = claimService.getAllClaimsByType(ClaimTypeEnum.HOME_CLAIM_DTO);

      // Cast to HomeClaimDto list (polymorphic relationship ensures this is safe)
      List<HomeClaimDto> homeClaims = claims.stream().map(HomeClaimDto.class::cast).toList();

      // TODO: Implement pagination and status filtering
      // For now, return all home claims without pagination or status filtering
      if (page != null || size != null || status != null) {
        log.warn(
            "Pagination (page: {}, size: {}) and status filtering (status: {}) not yet implemented",
            page,
            size,
            status);
      }

      log.info("Retrieved {} home claims", homeClaims.size());
      return ResponseEntity.ok(homeClaims);
    } catch (RuntimeException e) {
      log.error("Error retrieving home claims: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public ResponseEntity<HomeClaimDto> getHomeClaimById(Long id) {
    log.info("REST request to get home claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            claimDto -> {
              log.info("Found home claim with id: {}, returning HTTP 200 OK", id);
              return ResponseEntity.ok((HomeClaimDto) claimDto);
            })
        .orElseGet(
            () -> {
              log.warn("Home claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<Void> deleteHomeClaim(Long id) {
    log.info("REST request to delete home claim with id: {}", id);

    // Check if the claim exists first
    return claimService
        .findClaimById(id)
        .map(
            existingClaim -> {
              if (!(existingClaim instanceof HomeClaimDto)) {
                log.warn("Claim with id: {} is not a home claim", id);
                return ResponseEntity.notFound().<Void>build();
              }

              try {
                claimService.deleteClaim(id);
                log.info("Successfully deleted home claim with id: {}", id);
                return ResponseEntity.noContent().<Void>build();
              } catch (Exception e) {
                log.error("Error deleting home claim with id: {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().<Void>build();
              }
            })
        .orElseGet(
            () -> {
              log.warn("Home claim with id: {} not found, returning HTTP 404 NOT FOUND", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<HomeClaimDto> updateHomeClaim(Long id, HomeClaimDto homeClaimDto) {
    log.info("REST request to update home claim with id: {}", id);

    try {
      HomeClaimDto updatedClaim = (HomeClaimDto) claimService.updateClaim(id, homeClaimDto);
      log.info("Successfully updated home claim with id: {}", id);
      return ResponseEntity.ok(updatedClaim);
    } catch (Exception e) {
      log.warn("Failed to update home claim with id: {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<HomeClaimDto> assignAdjusterToHomeClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster {} to home claim {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);

    try {
      var updatedClaim = claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
      log.info(
          "Successfully assigned adjuster {} to home claim {}",
          assignAdjusterRequestDto.getEmployeeId(),
          id);

      return ResponseEntity.ok((HomeClaimDto) updatedClaim);
    } catch (Exception e) {
      log.warn("Failed to assign adjuster to home claim {}: {}", id, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
