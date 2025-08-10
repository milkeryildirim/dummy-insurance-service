package tech.yildirim.insurance.dummy.agency;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.AgencyDto;

/** Implementation of the {@link AgencyService} interface. */
@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

  private final AgencyRepository agencyRepository;
  private final AgencyMapper agencyMapper;

  @Override
  @Transactional(readOnly = true)
  public List<AgencyDto> findAllAgencies() {
    return agencyMapper.toDtoList(agencyRepository.findAll());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AgencyDto> findAgencyById(Long id) {
    return agencyRepository.findById(id).map(agencyMapper::toDto);
  }

  @Override
  @Transactional
  public AgencyDto createAgency(AgencyDto agencyDto) {
    agencyRepository
        .findByAgencyCode(agencyDto.getAgencyCode())
        .ifPresent(
            a -> {
              throw new DataIntegrityViolationException(
                  "Agency Code '" + agencyDto.getAgencyCode() + "' already exists.");
            });

    Agency agency = agencyMapper.toEntity(agencyDto);
    Agency savedAgency = agencyRepository.save(agency);
    return agencyMapper.toDto(savedAgency);
  }
}
