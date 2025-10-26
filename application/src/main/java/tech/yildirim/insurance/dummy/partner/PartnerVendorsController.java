package tech.yildirim.insurance.dummy.partner;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import tech.yildirim.insurance.api.generated.controller.PartnerVendorsApi;
import tech.yildirim.insurance.api.generated.model.PartnerVendorDto;
import tech.yildirim.insurance.api.generated.model.PartnerVendorsGet200Response;
import tech.yildirim.insurance.api.generated.model.PartnerVendorsIdStatusPatchRequest;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

/**
 * Implementation of the generated PartnerVendorsApi interface. Provides REST endpoints for managing
 * partner vendors.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PartnerVendorsController implements PartnerVendorsApi {

  private final PartnerVendorService partnerVendorService;
  private final PartnerVendorMapper partnerVendorMapper;

  private static final String PARTNER_VENDOR_NOT_FOUND_MESSAGE =
      "Partner vendor with ID: {} not found";

  @Override
  public Optional<NativeWebRequest> getRequest() {
    return PartnerVendorsApi.super.getRequest();
  }

  // ========== GET ALL PARTNER VENDORS ==========

  @Override
  public ResponseEntity<PartnerVendorsGet200Response> partnerVendorsGet(
      Integer page, Integer size, @Nullable String vendorType, @Nullable String status) {

    log.info(
        "REST request to get all partner vendors - page: {}, size: {}, vendorType: {}, status: {}",
        page,
        size,
        vendorType,
        status);

    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());
      Page<PartnerVendor> vendorPage;

      // Apply filters if provided
      if (vendorType != null && status != null) {
        VendorType type = VendorType.valueOf(vendorType.toUpperCase());
        VendorStatus vendorStatus = VendorStatus.valueOf(status.toUpperCase());
        List<PartnerVendor> allVendors =
            partnerVendorService.findPartnerVendorsByType(type).stream()
                .filter(vendor -> vendor.getStatus() == vendorStatus)
                .toList();
        // For now, we'll get all and filter - in production, you'd implement proper repository
        // methods
        vendorPage = partnerVendorService.findAllPartnerVendors(pageable);
        // Filter the content
        List<PartnerVendor> filteredContent =
            vendorPage.getContent().stream()
                .filter(
                    vendor -> vendor.getVendorType() == type && vendor.getStatus() == vendorStatus)
                .toList();
        vendorPage = createPageFromList(filteredContent, pageable, allVendors.size());
      } else if (vendorType != null) {
        VendorType type = VendorType.valueOf(vendorType.toUpperCase());
        List<PartnerVendor> allVendors = partnerVendorService.findPartnerVendorsByType(type);
        vendorPage = partnerVendorService.findAllPartnerVendors(pageable);
        List<PartnerVendor> filteredContent =
            vendorPage.getContent().stream()
                .filter(vendor -> vendor.getVendorType() == type)
                .toList();
        vendorPage = createPageFromList(filteredContent, pageable, allVendors.size());
      } else if (status != null) {
        VendorStatus vendorStatus = VendorStatus.valueOf(status.toUpperCase());
        List<PartnerVendor> allVendors =
            partnerVendorService.findPartnerVendorsByStatus(vendorStatus);
        vendorPage = partnerVendorService.findAllPartnerVendors(pageable);
        List<PartnerVendor> filteredContent =
            vendorPage.getContent().stream()
                .filter(vendor -> vendor.getStatus() == vendorStatus)
                .toList();
        vendorPage = createPageFromList(filteredContent, pageable, allVendors.size());
      } else {
        vendorPage = partnerVendorService.findAllPartnerVendors(pageable);
      }

      // Convert to DTOs
      List<PartnerVendorDto> content =
          vendorPage.getContent().stream().map(partnerVendorMapper::toDto).toList();

      // Create response object
      PartnerVendorsGet200Response response = new PartnerVendorsGet200Response();
      response.setContent(content);
      response.setTotalElements(vendorPage.getTotalElements());
      response.setTotalPages(vendorPage.getTotalPages());
      response.setSize(vendorPage.getSize());
      response.setNumber(vendorPage.getNumber());
      response.setFirst(vendorPage.isFirst());
      response.setLast(vendorPage.isLast());

      log.info("Retrieved {} partner vendors", content.size());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid enum value in request parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error retrieving partner vendors: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // ========== CREATE PARTNER VENDOR ==========

  @Override
  public ResponseEntity<PartnerVendorDto> partnerVendorsPost(PartnerVendorDto partnerVendorDto) {
    log.info(
        "REST request to create partner vendor with code: {}", partnerVendorDto.getVendorCode());

    try {
      PartnerVendor partnerVendor = partnerVendorMapper.toNewEntity(partnerVendorDto);
      PartnerVendor savedVendor = partnerVendorService.createPartnerVendor(partnerVendor);
      PartnerVendorDto responseDto = partnerVendorMapper.toDto(savedVendor);

      log.info("Successfully created partner vendor with ID: {}", savedVendor.getId());
      return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.warn(
          "Failed to create partner vendor - vendor code already exists: {}",
          partnerVendorDto.getVendorCode());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating partner vendor: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // ========== GET PARTNER VENDOR BY ID ==========

  @Override
  public ResponseEntity<PartnerVendorDto> partnerVendorsIdGet(Long id) {
    log.info("REST request to get partner vendor with ID: {}", id);

    return partnerVendorService
        .findPartnerVendorById(id)
        .map(
            partnerVendor -> {
              log.info("Found partner vendor with ID: {}", id);
              return ResponseEntity.ok(partnerVendorMapper.toDto(partnerVendor));
            })
        .orElseGet(
            () -> {
              log.warn(PARTNER_VENDOR_NOT_FOUND_MESSAGE, id);
              return ResponseEntity.notFound().build();
            });
  }

  // ========== UPDATE PARTNER VENDOR ==========

  @Override
  public ResponseEntity<PartnerVendorDto> partnerVendorsIdPut(
      Long id, PartnerVendorDto partnerVendorDto) {
    log.info("REST request to update partner vendor with ID: {}", id);

    try {
      PartnerVendor updateData = partnerVendorMapper.toEntity(partnerVendorDto);
      PartnerVendor updatedVendor = partnerVendorService.updatePartnerVendor(id, updateData);
      PartnerVendorDto responseDto = partnerVendorMapper.toDto(updatedVendor);

      log.info("Successfully updated partner vendor with ID: {}", id);
      return ResponseEntity.ok(responseDto);
    } catch (ResourceNotFoundException e) {
      log.error(PARTNER_VENDOR_NOT_FOUND_MESSAGE, id);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error updating partner vendor with ID: {}: {}", id, e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  // ========== DELETE PARTNER VENDOR ==========

  @Override
  public ResponseEntity<Void> partnerVendorsIdDelete(Long id) {
    log.info("REST request to delete partner vendor with ID: {}", id);

    try {
      partnerVendorService.deletePartnerVendor(id);
      log.info("Successfully deleted partner vendor with ID: {}", id);
      return ResponseEntity.noContent().build();
    } catch (ResourceNotFoundException e) {
      log.error(PARTNER_VENDOR_NOT_FOUND_MESSAGE, id);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error deleting partner vendor with ID: {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // ========== UPDATE PARTNER VENDOR STATUS ==========

  @Override
  public ResponseEntity<PartnerVendorDto> partnerVendorsIdStatusPatch(
      Long id, PartnerVendorsIdStatusPatchRequest partnerVendorsIdStatusPatchRequest) {

    log.info(
        "REST request to update partner vendor status for ID: {} to: {}",
        id,
        partnerVendorsIdStatusPatchRequest.getStatus());

    try {
      VendorStatus newStatus =
          VendorStatus.valueOf(partnerVendorsIdStatusPatchRequest.getStatus().name());
      PartnerVendor updatedVendor = partnerVendorService.updatePartnerVendorStatus(id, newStatus);
      PartnerVendorDto responseDto = partnerVendorMapper.toDto(updatedVendor);

      log.info("Successfully updated partner vendor status for ID: {}", id);
      return ResponseEntity.ok(responseDto);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid status value: {}", partnerVendorsIdStatusPatchRequest.getStatus());
      return ResponseEntity.badRequest().build();
    } catch (ResourceNotFoundException e) {
      log.error(PARTNER_VENDOR_NOT_FOUND_MESSAGE, id);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error updating partner vendor status for ID: {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // ========== SEARCH PARTNER VENDORS ==========

  @Override
  public ResponseEntity<List<PartnerVendorDto>> partnerVendorsSearchGet(
      @Nullable String companyName,
      @Nullable String vendorCode,
      @Nullable String vendorType,
      Boolean activeOnly) {

    log.info(
        "REST request to search partner vendors - companyName: {}, vendorCode: {}, vendorType: {}, activeOnly: {}",
        companyName,
        vendorCode,
        vendorType,
        activeOnly);

    try {
      List<PartnerVendor> vendors;

      if (vendorCode != null && !vendorCode.trim().isEmpty()) {
        vendors =
            partnerVendorService
                .findPartnerVendorByCode(vendorCode.trim())
                .map(List::of)
                .orElse(List.of());
      } else if (companyName != null && !companyName.trim().isEmpty()) {
        vendors = partnerVendorService.searchPartnerVendorsByCompanyName(companyName.trim());

        // Filter by activeOnly if requested
        if (Boolean.TRUE.equals(activeOnly)) {
          vendors =
              vendors.stream().filter(vendor -> vendor.getStatus() == VendorStatus.ACTIVE).toList();
        }
      } else if (vendorType != null) {
        VendorType type = VendorType.valueOf(vendorType.toUpperCase());
        if (Boolean.TRUE.equals(activeOnly)) {
          vendors = partnerVendorService.findActivePartnerVendorsByType(type);
        } else {
          vendors = partnerVendorService.findPartnerVendorsByType(type);
        }
      } else if (Boolean.TRUE.equals(activeOnly)) {
        vendors = partnerVendorService.findPartnerVendorsByStatus(VendorStatus.ACTIVE);
      } else {
        log.warn("Search request without any search criteria");
        return ResponseEntity.badRequest().build();
      }

      List<PartnerVendorDto> dtos = partnerVendorMapper.toDtoList(vendors);
      log.info("Found {} matching partner vendors", dtos.size());
      return ResponseEntity.ok(dtos);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid enum value in search parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error searching partner vendors: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // ========== HELPER METHODS ==========

  private Page<PartnerVendor> createPageFromList(
      List<PartnerVendor> content, Pageable pageable, long totalElements) {
    return new org.springframework.data.domain.PageImpl<>(content, pageable, totalElements);
  }
}
