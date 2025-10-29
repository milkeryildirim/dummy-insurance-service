package tech.yildirim.insurance.dummy.claim;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.yildirim.insurance.api.generated.controller.ClaimsApi;
import tech.yildirim.insurance.api.generated.model.AdjusterReportDto;
import tech.yildirim.insurance.api.generated.model.AssignAdjusterRequestDto;
import tech.yildirim.insurance.api.generated.model.AutoClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDecisionDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.api.generated.model.ClaimDto.ClaimTypeEnum;
import tech.yildirim.insurance.api.generated.model.CustomerInvoiceDto;
import tech.yildirim.insurance.api.generated.model.HealthClaimDto;
import tech.yildirim.insurance.api.generated.model.HomeClaimDto;

/**
 * REST Controller for managing auto claims. Implements the generated {@link ClaimsApi} interface.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClaimsController implements ClaimsApi {

  private final ClaimService claimService;
  private final AdjusterReportService adjusterReportService;
  private final CustomerInvoiceService customerInvoiceService;
  private final ClaimDecisionService claimDecisionService;

  // ========== AUTO CLAIM BASIC OPERATIONS ==========

  @Override
  public ResponseEntity<AutoClaimDto> createAutoClaim(AutoClaimDto autoClaimDto) {
    log.info("REST request to create auto claim for policy {}", autoClaimDto.getPolicyId());

    try {
      AutoClaimDto createdClaim =
          (AutoClaimDto) claimService.submitClaim(autoClaimDto.getPolicyId(), autoClaimDto);
      log.info(
          "Successfully created auto claim with id {} and number {}",
          createdClaim.getId(),
          createdClaim.getClaimNumber());
      return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating auto claim: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<List<AutoClaimDto>> getAllAutoClaims(
      Integer page, Integer size, String status) {
    log.info(
        "REST request to get all auto claims - page: {}, size: {}, status: {}", page, size, status);

    try {
      List<ClaimDto> claims = claimService.getAllClaimsByType(ClaimTypeEnum.AUTO_CLAIM_DTO);
      List<AutoClaimDto> autoClaims = claims.stream().map(AutoClaimDto.class::cast).toList();

      if (page != null || size != null || status != null) {
        log.warn("Pagination and status filtering not yet implemented");
      }

      log.info("Retrieved {} auto claims", autoClaims.size());
      return ResponseEntity.ok(autoClaims);
    } catch (Exception e) {
      log.error("Error retrieving auto claims: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AutoClaimDto> getAutoClaimById(Long id) {
    log.info("REST request to get auto claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            claimDto -> {
              log.info("Found auto claim with id: {}", id);
              return ResponseEntity.ok((AutoClaimDto) claimDto);
            })
        .orElseGet(
            () -> {
              log.warn("Auto claim with id: {} not found", id);
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
      log.error("Error updating auto claim with id: {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteAutoClaim(Long id) {
    log.info("REST request to delete auto claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            existingClaim -> {
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
              log.warn("Auto claim with id: {} not found", id);
              return ResponseEntity.notFound().build();
            });
  }

  @Override
  public ResponseEntity<AutoClaimDto> assignAdjusterToAutoClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster {} to auto claim {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);

    try {
      ClaimDto updatedClaim =
          claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
      log.info(
          "Successfully assigned adjuster {} to auto claim {}",
          assignAdjusterRequestDto.getEmployeeId(),
          id);
      return ResponseEntity.ok((AutoClaimDto) updatedClaim);
    } catch (Exception e) {
      log.error("Error assigning adjuster to auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== AUTO CLAIM ADJUSTER REPORTS ==========

  @Override
  public ResponseEntity<List<AdjusterReportDto>> getAutoClaimAdjusterReports(Long id) {
    log.info("REST request to get adjuster reports for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      List<AdjusterReportDto> reports = adjusterReportService.findAdjusterReportsByClaimId(id);
      log.info("Found {} adjuster reports for auto claim {}", reports.size(), id);
      return ResponseEntity.ok(reports);
    } catch (Exception e) {
      log.error("Error retrieving adjuster reports for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> createAutoClaimAdjusterReport(
      Long id, AdjusterReportDto report, MultipartFile pdfFile) {
    log.info("REST request to create adjuster report for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canAddAdjusterReports(id)) {
        return ResponseEntity.badRequest().build();
      }

      AdjusterReportDto createdReport =
          adjusterReportService.createAdjusterReport(id, report, pdfFile);
      log.info(
          "Successfully created adjuster report {} for auto claim {}", createdReport.getId(), id);
      return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating adjuster report for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> getAutoClaimAdjusterReportById(Long id, Long reportId) {
    log.info("REST request to get adjuster report {} for auto claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return adjusterReportService
          .findAdjusterReportByClaimIdAndReportId(id, reportId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Error retrieving adjuster report {} for auto claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> updateAutoClaimAdjusterReport(
      Long id, Long reportId, AdjusterReportDto adjusterReportDto) {
    log.info("REST request to update adjuster report {} for auto claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      AdjusterReportDto updatedReport =
          adjusterReportService.updateAdjusterReport(id, reportId, adjusterReportDto);
      return ResponseEntity.ok(updatedReport);
    } catch (Exception e) {
      log.error(
          "Error updating adjuster report {} for auto claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteAutoClaimAdjusterReport(Long id, Long reportId) {
    log.info("REST request to delete adjuster report {} for auto claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      adjusterReportService.deleteAdjusterReport(id, reportId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error deleting adjuster report {} for auto claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== AUTO CLAIM CUSTOMER INVOICES ==========

  @Override
  public ResponseEntity<List<CustomerInvoiceDto>> getAutoClaimCustomerInvoices(Long id) {
    log.info("REST request to get customer invoices for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      List<CustomerInvoiceDto> invoices = customerInvoiceService.findCustomerInvoicesByClaimId(id);
      return ResponseEntity.ok(invoices);
    } catch (Exception e) {
      log.error("Error retrieving customer invoices for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> createAutoClaimCustomerInvoice(
      Long id, CustomerInvoiceDto invoice, MultipartFile pdfFile) {
    log.info("REST request to create customer invoice for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canAddCustomerInvoices(id)) {
        return ResponseEntity.badRequest().build();
      }

      CustomerInvoiceDto createdInvoice =
          customerInvoiceService.createCustomerInvoice(id, invoice, pdfFile);
      return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating customer invoice for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> getAutoClaimCustomerInvoiceById(
      Long id, Long invoiceId) {
    log.info("REST request to get customer invoice {} for auto claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return customerInvoiceService
          .findCustomerInvoiceByClaimIdAndInvoiceId(id, invoiceId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Error retrieving customer invoice {} for auto claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> updateAutoClaimCustomerInvoice(
      Long id, Long invoiceId, CustomerInvoiceDto customerInvoiceDto) {
    log.info("REST request to update customer invoice {} for auto claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      CustomerInvoiceDto updatedInvoice =
          customerInvoiceService.updateCustomerInvoice(id, invoiceId, customerInvoiceDto);
      return ResponseEntity.ok(updatedInvoice);
    } catch (Exception e) {
      log.error(
          "Error updating customer invoice {} for auto claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteAutoClaimCustomerInvoice(Long id, Long invoiceId) {
    log.info("REST request to delete customer invoice {} for auto claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      customerInvoiceService.deleteCustomerInvoice(id, invoiceId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error deleting customer invoice {} for auto claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== AUTO CLAIM DECISIONS ==========

  @Override
  public ResponseEntity<ClaimDecisionDto> getAutoClaimDecision(Long id) {
    log.info("REST request to get decision for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return claimDecisionService
          .findClaimDecisionByClaimId(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error("Error retrieving decision for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<ClaimDecisionDto> createAutoClaimDecision(
      Long id, ClaimDecisionDto claimDecisionDto) {
    log.info("REST request to create decision for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canMakeDecision(id)) {
        return ResponseEntity.badRequest().build();
      }

      ClaimDecisionDto createdDecision =
          claimDecisionService.createClaimDecision(id, claimDecisionDto);
      return new ResponseEntity<>(createdDecision, HttpStatus.CREATED);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      log.error("Error creating decision for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<ClaimDecisionDto> updateAutoClaimDecision(
      Long id, ClaimDecisionDto claimDecisionDto) {
    log.info("REST request to update decision for auto claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      ClaimDecisionDto updatedDecision =
          claimDecisionService.updateClaimDecision(id, claimDecisionDto);
      return ResponseEntity.ok(updatedDecision);
    } catch (Exception e) {
      log.error("Error updating decision for auto claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HOME CLAIM OPERATIONS ==========

  @Override
  public ResponseEntity<HomeClaimDto> createHomeClaim(HomeClaimDto homeClaimDto) {
    log.info("REST request to create home claim for policy {}", homeClaimDto.getPolicyId());

    try {
      HomeClaimDto createdClaim =
          (HomeClaimDto) claimService.submitClaim(homeClaimDto.getPolicyId(), homeClaimDto);
      return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating home claim: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<List<HomeClaimDto>> getAllHomeClaims(
      Integer page, Integer size, String status) {
    log.info("REST request to get all home claims");

    try {
      List<ClaimDto> claims = claimService.getAllClaimsByType(ClaimTypeEnum.HOME_CLAIM_DTO);
      List<HomeClaimDto> homeClaims = claims.stream().map(HomeClaimDto.class::cast).toList();
      return ResponseEntity.ok(homeClaims);
    } catch (Exception e) {
      log.error("Error retrieving home claims: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<HomeClaimDto> getHomeClaimById(Long id) {
    log.info("REST request to get home claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(claimDto -> ResponseEntity.ok((HomeClaimDto) claimDto))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<HomeClaimDto> updateHomeClaim(Long id, HomeClaimDto homeClaimDto) {
    log.info("REST request to update home claim with id: {}", id);

    try {
      HomeClaimDto updatedClaim = (HomeClaimDto) claimService.updateClaim(id, homeClaimDto);
      return ResponseEntity.ok(updatedClaim);
    } catch (Exception e) {
      log.error("Error updating home claim with id: {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteHomeClaim(Long id) {
    log.info("REST request to delete home claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            existingClaim -> {
              try {
                claimService.deleteClaim(id);
                return ResponseEntity.noContent().<Void>build();
              } catch (Exception e) {
                log.error("Error deleting home claim with id: {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().<Void>build();
              }
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<HomeClaimDto> assignAdjusterToHomeClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster {} to home claim {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);

    try {
      ClaimDto updatedClaim =
          claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
      return ResponseEntity.ok((HomeClaimDto) updatedClaim);
    } catch (Exception e) {
      log.error("Error assigning adjuster to home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HOME CLAIM ADJUSTER REPORTS ==========

  @Override
  public ResponseEntity<List<AdjusterReportDto>> getHomeClaimAdjusterReports(Long id) {
    log.info("REST request to get adjuster reports for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      List<AdjusterReportDto> reports = adjusterReportService.findAdjusterReportsByClaimId(id);
      return ResponseEntity.ok(reports);
    } catch (Exception e) {
      log.error("Error retrieving adjuster reports for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> createHomeClaimAdjusterReport(
      Long id, AdjusterReportDto report, MultipartFile pdfFile) {
    log.info("REST request to create adjuster report for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canAddAdjusterReports(id)) {
        return ResponseEntity.badRequest().build();
      }

      AdjusterReportDto createdReport =
          adjusterReportService.createAdjusterReport(id, report, pdfFile);
      return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating adjuster report for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> getHomeClaimAdjusterReportById(Long id, Long reportId) {
    log.info("REST request to get adjuster report {} for home claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return adjusterReportService
          .findAdjusterReportByClaimIdAndReportId(id, reportId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Error retrieving adjuster report {} for home claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> updateHomeClaimAdjusterReport(
      Long id, Long reportId, AdjusterReportDto adjusterReportDto) {
    log.info("REST request to update adjuster report {} for home claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      AdjusterReportDto updatedReport =
          adjusterReportService.updateAdjusterReport(id, reportId, adjusterReportDto);
      return ResponseEntity.ok(updatedReport);
    } catch (Exception e) {
      log.error(
          "Error updating adjuster report {} for home claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteHomeClaimAdjusterReport(Long id, Long reportId) {
    log.info("REST request to delete adjuster report {} for home claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      adjusterReportService.deleteAdjusterReport(id, reportId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error deleting adjuster report {} for home claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HOME CLAIM CUSTOMER INVOICES ==========

  @Override
  public ResponseEntity<List<CustomerInvoiceDto>> getHomeClaimCustomerInvoices(Long id) {
    log.info("REST request to get customer invoices for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      List<CustomerInvoiceDto> invoices = customerInvoiceService.findCustomerInvoicesByClaimId(id);
      return ResponseEntity.ok(invoices);
    } catch (Exception e) {
      log.error("Error retrieving customer invoices for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> createHomeClaimCustomerInvoice(
      Long id, CustomerInvoiceDto invoice, MultipartFile pdfFile) {
    log.info("REST request to create customer invoice for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canAddCustomerInvoices(id)) {
        return ResponseEntity.badRequest().build();
      }

      CustomerInvoiceDto createdInvoice =
          customerInvoiceService.createCustomerInvoice(id, invoice, pdfFile);
      return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating customer invoice for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> getHomeClaimCustomerInvoiceById(
      Long id, Long invoiceId) {
    log.info("REST request to get customer invoice {} for home claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return customerInvoiceService
          .findCustomerInvoiceByClaimIdAndInvoiceId(id, invoiceId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Error retrieving customer invoice {} for home claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> updateHomeClaimCustomerInvoice(
      Long id, Long invoiceId, CustomerInvoiceDto customerInvoiceDto) {
    log.info("REST request to update customer invoice {} for home claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      CustomerInvoiceDto updatedInvoice =
          customerInvoiceService.updateCustomerInvoice(id, invoiceId, customerInvoiceDto);
      return ResponseEntity.ok(updatedInvoice);
    } catch (Exception e) {
      log.error(
          "Error updating customer invoice {} for home claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteHomeClaimCustomerInvoice(Long id, Long invoiceId) {
    log.info("REST request to delete customer invoice {} for home claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      customerInvoiceService.deleteCustomerInvoice(id, invoiceId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error deleting customer invoice {} for home claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HOME CLAIM DECISIONS ==========

  @Override
  public ResponseEntity<ClaimDecisionDto> getHomeClaimDecision(Long id) {
    log.info("REST request to get decision for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return claimDecisionService
          .findClaimDecisionByClaimId(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error("Error retrieving decision for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<ClaimDecisionDto> createHomeClaimDecision(
      Long id, ClaimDecisionDto claimDecisionDto) {
    log.info("REST request to create decision for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canMakeDecision(id)) {
        return ResponseEntity.badRequest().build();
      }

      ClaimDecisionDto createdDecision =
          claimDecisionService.createClaimDecision(id, claimDecisionDto);
      return new ResponseEntity<>(createdDecision, HttpStatus.CREATED);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      log.error("Error creating decision for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<ClaimDecisionDto> updateHomeClaimDecision(
      Long id, ClaimDecisionDto claimDecisionDto) {
    log.info("REST request to update decision for home claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      ClaimDecisionDto updatedDecision =
          claimDecisionService.updateClaimDecision(id, claimDecisionDto);
      return ResponseEntity.ok(updatedDecision);
    } catch (Exception e) {
      log.error("Error updating decision for home claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HEALTH CLAIM OPERATIONS ==========

  @Override
  public ResponseEntity<HealthClaimDto> createHealthClaim(HealthClaimDto healthClaimDto) {
    log.info("REST request to create health claim for policy {}", healthClaimDto.getPolicyId());

    try {
      HealthClaimDto createdClaim =
          (HealthClaimDto) claimService.submitClaim(healthClaimDto.getPolicyId(), healthClaimDto);
      return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating health claim: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<List<HealthClaimDto>> getAllHealthClaims(
      Integer page, Integer size, String status) {
    log.info("REST request to get all health claims");

    try {
      List<ClaimDto> claims = claimService.getAllClaimsByType(ClaimTypeEnum.HEALTH_CLAIM_DTO);
      List<HealthClaimDto> healthClaims = claims.stream().map(HealthClaimDto.class::cast).toList();
      return ResponseEntity.ok(healthClaims);
    } catch (Exception e) {
      log.error("Error retrieving health claims: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<HealthClaimDto> getHealthClaimById(Long id) {
    log.info("REST request to get health claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(claimDto -> ResponseEntity.ok((HealthClaimDto) claimDto))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<HealthClaimDto> updateHealthClaim(Long id, HealthClaimDto healthClaimDto) {
    log.info("REST request to update health claim with id: {}", id);

    try {
      HealthClaimDto updatedClaim = (HealthClaimDto) claimService.updateClaim(id, healthClaimDto);
      return ResponseEntity.ok(updatedClaim);
    } catch (Exception e) {
      log.error("Error updating health claim with id: {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteHealthClaim(Long id) {
    log.info("REST request to delete health claim with id: {}", id);

    return claimService
        .findClaimById(id)
        .map(
            existingClaim -> {
              try {
                claimService.deleteClaim(id);
                return ResponseEntity.noContent().<Void>build();
              } catch (Exception e) {
                log.error("Error deleting health claim with id: {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().<Void>build();
              }
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<HealthClaimDto> assignAdjusterToHealthClaim(
      Long id, AssignAdjusterRequestDto assignAdjusterRequestDto) {
    log.info(
        "REST request to assign adjuster {} to health claim {}",
        assignAdjusterRequestDto.getEmployeeId(),
        id);

    try {
      ClaimDto updatedClaim =
          claimService.assignAdjuster(id, assignAdjusterRequestDto.getEmployeeId());
      return ResponseEntity.ok((HealthClaimDto) updatedClaim);
    } catch (Exception e) {
      log.error("Error assigning adjuster to health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HEALTH CLAIM ADJUSTER REPORTS ==========

  @Override
  public ResponseEntity<List<AdjusterReportDto>> getHealthClaimAdjusterReports(Long id) {
    log.info("REST request to get adjuster reports for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      List<AdjusterReportDto> reports = adjusterReportService.findAdjusterReportsByClaimId(id);
      return ResponseEntity.ok(reports);
    } catch (Exception e) {
      log.error("Error retrieving adjuster reports for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> createHealthClaimAdjusterReport(
      Long id, AdjusterReportDto report, MultipartFile pdfFile) {
    log.info("REST request to create adjuster report for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canAddAdjusterReports(id)) {
        return ResponseEntity.badRequest().build();
      }

      AdjusterReportDto createdReport =
          adjusterReportService.createAdjusterReport(id, report, pdfFile);
      return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating adjuster report for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> getHealthClaimAdjusterReportById(
      Long id, Long reportId) {
    log.info("REST request to get adjuster report {} for health claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return adjusterReportService
          .findAdjusterReportByClaimIdAndReportId(id, reportId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Error retrieving adjuster report {} for health claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<AdjusterReportDto> updateHealthClaimAdjusterReport(
      Long id, Long reportId, AdjusterReportDto adjusterReportDto) {
    log.info("REST request to update adjuster report {} for health claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      AdjusterReportDto updatedReport =
          adjusterReportService.updateAdjusterReport(id, reportId, adjusterReportDto);
      return ResponseEntity.ok(updatedReport);
    } catch (Exception e) {
      log.error(
          "Error updating adjuster report {} for health claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteHealthClaimAdjusterReport(Long id, Long reportId) {
    log.info("REST request to delete adjuster report {} for health claim {}", reportId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      adjusterReportService.deleteAdjusterReport(id, reportId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error deleting adjuster report {} for health claim {}: {}",
          reportId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HEALTH CLAIM CUSTOMER INVOICES ==========

  @Override
  public ResponseEntity<List<CustomerInvoiceDto>> getHealthClaimCustomerInvoices(Long id) {
    log.info("REST request to get customer invoices for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      List<CustomerInvoiceDto> invoices = customerInvoiceService.findCustomerInvoicesByClaimId(id);
      return ResponseEntity.ok(invoices);
    } catch (Exception e) {
      log.error(
          "Error retrieving customer invoices for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> createHealthClaimCustomerInvoice(
      Long id, CustomerInvoiceDto invoice, MultipartFile pdfFile) {
    log.info("REST request to create customer invoice for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canAddCustomerInvoices(id)) {
        return ResponseEntity.badRequest().build();
      }

      CustomerInvoiceDto createdInvoice =
          customerInvoiceService.createCustomerInvoice(id, invoice, pdfFile);
      return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Error creating customer invoice for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> getHealthClaimCustomerInvoiceById(
      Long id, Long invoiceId) {
    log.info("REST request to get customer invoice {} for health claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return customerInvoiceService
          .findCustomerInvoiceByClaimIdAndInvoiceId(id, invoiceId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Error retrieving customer invoice {} for health claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<CustomerInvoiceDto> updateHealthClaimCustomerInvoice(
      Long id, Long invoiceId, CustomerInvoiceDto customerInvoiceDto) {
    log.info("REST request to update customer invoice {} for health claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      CustomerInvoiceDto updatedInvoice =
          customerInvoiceService.updateCustomerInvoice(id, invoiceId, customerInvoiceDto);
      return ResponseEntity.ok(updatedInvoice);
    } catch (Exception e) {
      log.error(
          "Error updating customer invoice {} for health claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<Void> deleteHealthClaimCustomerInvoice(Long id, Long invoiceId) {
    log.info("REST request to delete customer invoice {} for health claim {}", invoiceId, id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      customerInvoiceService.deleteCustomerInvoice(id, invoiceId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error deleting customer invoice {} for health claim {}: {}",
          invoiceId,
          id,
          e.getMessage(),
          e);
      return ResponseEntity.notFound().build();
    }
  }

  // ========== HEALTH CLAIM DECISIONS ==========

  @Override
  public ResponseEntity<ClaimDecisionDto> getHealthClaimDecision(Long id) {
    log.info("REST request to get decision for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return claimDecisionService
          .findClaimDecisionByClaimId(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error("Error retrieving decision for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public ResponseEntity<ClaimDecisionDto> createHealthClaimDecision(
      Long id, ClaimDecisionDto claimDecisionDto) {
    log.info("REST request to create decision for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      if (!claimService.canMakeDecision(id)) {
        return ResponseEntity.badRequest().build();
      }

      ClaimDecisionDto createdDecision =
          claimDecisionService.createClaimDecision(id, claimDecisionDto);
      return new ResponseEntity<>(createdDecision, HttpStatus.CREATED);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      log.error("Error creating decision for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<ClaimDecisionDto> updateHealthClaimDecision(
      Long id, ClaimDecisionDto claimDecisionDto) {
    log.info("REST request to update decision for health claim {}", id);

    try {
      if (claimService.findClaimById(id).isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      ClaimDecisionDto updatedDecision =
          claimDecisionService.updateClaimDecision(id, claimDecisionDto);
      return ResponseEntity.ok(updatedDecision);
    } catch (Exception e) {
      log.error("Error updating decision for health claim {}: {}", id, e.getMessage(), e);
      return ResponseEntity.notFound().build();
    }
  }
}
