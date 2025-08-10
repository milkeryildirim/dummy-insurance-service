package tech.yildirim.insurance.dummy.agency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tech.yildirim.insurance.api.generated.model.AgencyDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Agency Service Unit Tests")
class AgencyServiceImplTest {
  @Mock private AgencyRepository agencyRepository;
  @Mock private AgencyMapper agencyMapper;

  @InjectMocks private AgencyServiceImpl agencyService;

  @Test
  @DisplayName("Should throw DataIntegrityViolationException when agency code already exists")
  void createAgency_whenCodeExists_shouldThrowException() {
    // Given
    AgencyDto inputDto = new AgencyDto().agencyCode("AG-DUP-001");
    when(agencyRepository.findByAgencyCode("AG-DUP-001")).thenReturn(Optional.of(new Agency()));

    // When & Then
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class,
            () -> {
              agencyService.createAgency(inputDto);
            });

    assertThat(exception.getMessage()).contains("already exists");
    verify(agencyRepository, never()).save(any());
  }
}
