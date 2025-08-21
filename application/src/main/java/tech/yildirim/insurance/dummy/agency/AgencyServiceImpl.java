package tech.yildirim.insurance.dummy.agency;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.yildirim.insurance.api.generated.model.AgencyDto;

/** Implementation of the {@link AgencyService} interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgencyServiceImpl implements AgencyService {

  private final AgencyRepository agencyRepository;
  private final AgencyMapper agencyMapper;

  @Override
  @Transactional(readOnly = true)
  public List<AgencyDto> findAllAgencies() {
    log.info("Request to find all agencies");
    List<Agency> agencies = agencyRepository.findAll();
    log.info("Found {} agencies", agencies.size());
    return agencyMapper.toDtoList(agencies);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AgencyDto> findAgencyById(Long id) {
    log.info("Request to find agency with id: {}", id);
    return agencyRepository.findById(id).map(agencyMapper::toDto);
  }

  @Override
  @Transactional
  public AgencyDto createAgency(AgencyDto agencyDto) {
    log.info("Request to create agency: {}", agencyDto.getAgencyCode());
    agencyRepository
        .findByAgencyCode(agencyDto.getAgencyCode())
        .ifPresent(
            a -> {
              log.error("Agency with code '{}' already exists.", a.getAgencyCode());
              throw new DataIntegrityViolationException(
                  "Agency Code '" + agencyDto.getAgencyCode() + "' already exists.");
            });

    Agency agency = agencyMapper.toEntity(agencyDto);
    Agency savedAgency = agencyRepository.save(agency);
    log.info("Successfully created agency with id {}", savedAgency.getId());
    return agencyMapper.toDto(savedAgency);
  }
}
