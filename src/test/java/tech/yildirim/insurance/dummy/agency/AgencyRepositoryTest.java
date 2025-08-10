package tech.yildirim.insurance.dummy.agency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@DisplayName("Agency Repository Integration Tests")
class AgencyRepositoryTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private AgencyRepository agencyRepository;

  @Test
  @DisplayName("Should find an agency by its unique agency code")
  void shouldFindByAgencyCode() {
    // Given
    Agency agency = new Agency();
    agency.setAgencyCode("AG-UNIQUE-1");
    agency.setName("Unique Agency");
    agency.setContactEmail("unique@agency.com");
    agency.setContactPerson("Person");
    entityManager.persistAndFlush(agency);

    // When
    Optional<Agency> found = agencyRepository.findByAgencyCode("AG-UNIQUE-1");
    Optional<Agency> notFound = agencyRepository.findByAgencyCode("AG-NON-EXISTENT");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getAgencyCode()).isEqualTo("AG-UNIQUE-1");
    assertThat(notFound).isNotPresent();
  }
}
