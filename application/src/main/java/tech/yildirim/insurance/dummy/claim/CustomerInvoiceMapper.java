package tech.yildirim.insurance.dummy.claim;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tech.yildirim.insurance.api.generated.model.CustomerInvoiceDto;

/** Mapper for the entity {@link CustomerInvoice} and its DTO {@link CustomerInvoiceDto}. */
@Mapper(componentModel = "spring")
public interface CustomerInvoiceMapper {

  /**
   * Maps a CustomerInvoice entity to a CustomerInvoiceDto.
   *
   * @param customerInvoice The source entity.
   * @return The target DTO.
   */
  @Mapping(target = "claimId", source = "claim.id")
  CustomerInvoiceDto toDto(CustomerInvoice customerInvoice);

  /**
   * Maps a list of CustomerInvoice entities to a list of CustomerInvoiceDtos.
   *
   * @param customerInvoices The list of source entities.
   * @return The list of target DTOs.
   */
  List<CustomerInvoiceDto> toDtoList(List<CustomerInvoice> customerInvoices);

  /**
   * Maps a CustomerInvoiceDto to a CustomerInvoice entity.
   *
   * @param customerInvoiceDto The source DTO.
   * @return The target entity.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claim", ignore = true)
  @Mapping(target = "uploadedAt", ignore = true)
  CustomerInvoice toEntity(CustomerInvoiceDto customerInvoiceDto);

  /**
   * Updates an existing CustomerInvoice entity from a CustomerInvoiceDto.
   *
   * @param customerInvoiceDto The source DTO with updated information.
   * @param customerInvoice The target entity to update.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "claim", ignore = true)
  @Mapping(target = "uploadedAt", ignore = true)
  void updateCustomerInvoiceFromDto(
      CustomerInvoiceDto customerInvoiceDto, @MappingTarget CustomerInvoice customerInvoice);

  /** Converts ZonedDateTime to OffsetDateTime. */
  default OffsetDateTime map(ZonedDateTime zonedDateTime) {
    return zonedDateTime != null ? zonedDateTime.toOffsetDateTime() : null;
  }

  /** Converts OffsetDateTime to ZonedDateTime. */
  default ZonedDateTime map(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toZonedDateTime() : null;
  }
}
