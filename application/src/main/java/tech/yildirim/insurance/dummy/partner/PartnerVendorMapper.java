package tech.yildirim.insurance.dummy.partner;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import tech.yildirim.insurance.api.generated.model.PartnerVendorDto;

/**
 * MapStruct mapper for converting between PartnerVendor entity and PartnerVendorDto. Handles
 * bidirectional mapping with proper field transformations.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PartnerVendorMapper {

  /**
   * Converts PartnerVendor entity to PartnerVendorDto.
   *
   * @param partnerVendor the entity to convert
   * @return the converted DTO
   */
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  PartnerVendorDto toDto(PartnerVendor partnerVendor);

  /**
   * Converts PartnerVendorDto to PartnerVendor entity for updates. Excludes auto-generated fields
   * like timestamps.
   *
   * @param partnerVendorDto the DTO to convert
   * @return the converted entity
   */
  @Named("toEntityForUpdate")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PartnerVendor toEntity(PartnerVendorDto partnerVendorDto);

  /**
   * Converts a list of PartnerVendor entities to PartnerVendorDto list.
   *
   * @param partnerVendors the list of entities to convert
   * @return the list of converted DTOs
   */
  List<PartnerVendorDto> toDtoList(List<PartnerVendor> partnerVendors);

  /**
   * Updates an existing PartnerVendor entity with data from PartnerVendorDto. Preserves entity ID
   * and timestamps while updating other fields.
   *
   * @param partnerVendorDto the DTO containing update data
   * @param partnerVendor the existing entity to update
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromDto(
      PartnerVendorDto partnerVendorDto, @MappingTarget PartnerVendor partnerVendor);

  /**
   * Creates a new PartnerVendor entity for creation operations. Sets all fields except ID and
   * timestamps which will be handled by JPA.
   *
   * @param partnerVendorDto the DTO containing creation data
   * @return the new entity ready for persistence
   */
  @Named("toNewEntity")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PartnerVendor toNewEntity(PartnerVendorDto partnerVendorDto);

  // ========== DATE/TIME CONVERSION METHODS ==========

  /**
   * Converts ZonedDateTime to OffsetDateTime for DTO mapping.
   *
   * @param zonedDateTime the ZonedDateTime from entity
   * @return the corresponding OffsetDateTime for DTO
   */
  default OffsetDateTime map(ZonedDateTime zonedDateTime) {
    if (zonedDateTime == null) {
      return null;
    }
    return zonedDateTime.toOffsetDateTime();
  }

  /**
   * Converts OffsetDateTime to ZonedDateTime for entity mapping.
   *
   * @param offsetDateTime the OffsetDateTime from DTO
   * @return the corresponding ZonedDateTime for entity
   */
  default ZonedDateTime map(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }
    return offsetDateTime.toZonedDateTime();
  }

  // ========== ENUM CONVERSION METHODS ==========

  /**
   * Converts VendorType enum from entity to DTO enum.
   *
   * @param vendorType the entity enum value
   * @return the corresponding DTO enum value
   */
  default PartnerVendorDto.VendorTypeEnum toVendorTypeDto(VendorType vendorType) {
    if (vendorType == null) {
      return null;
    }
    return PartnerVendorDto.VendorTypeEnum.valueOf(vendorType.name());
  }

  /**
   * Converts VendorType enum from DTO to entity enum.
   *
   * @param vendorTypeDto the DTO enum value
   * @return the corresponding entity enum value
   */
  default VendorType toVendorTypeEntity(PartnerVendorDto.VendorTypeEnum vendorTypeDto) {
    if (vendorTypeDto == null) {
      return null;
    }
    return VendorType.valueOf(vendorTypeDto.name());
  }

  /**
   * Converts VendorStatus enum from entity to DTO enum.
   *
   * @param vendorStatus the entity enum value
   * @return the corresponding DTO enum value
   */
  default PartnerVendorDto.StatusEnum toVendorStatusDto(VendorStatus vendorStatus) {
    if (vendorStatus == null) {
      return null;
    }
    return PartnerVendorDto.StatusEnum.valueOf(vendorStatus.name());
  }

  /**
   * Converts VendorStatus enum from DTO to entity enum.
   *
   * @param vendorStatusDto the DTO enum value
   * @return the corresponding entity enum value
   */
  default VendorStatus toVendorStatusEntity(PartnerVendorDto.StatusEnum vendorStatusDto) {
    if (vendorStatusDto == null) {
      return null;
    }
    return VendorStatus.valueOf(vendorStatusDto.name());
  }
}
