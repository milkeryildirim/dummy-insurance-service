package tech.yildirim.insurance.dummy.agency;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.yildirim.insurance.api.generated.model.AddressDto;
import tech.yildirim.insurance.api.generated.model.AgencyDto;
import tech.yildirim.insurance.dummy.common.Address;

/** Mapper for the entity {@link Agency} and its DTO {@link AgencyDto}. */
@Mapper(componentModel = "spring")
public interface AgencyMapper {

  AgencyDto toDto(Agency agency);

  List<AgencyDto> toDtoList(List<Agency> agencies);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "policies", ignore = true)
  Agency toEntity(AgencyDto agencyDto);

  Address toEntity(AddressDto addressDto);

  AddressDto toDto(Address address);
}
