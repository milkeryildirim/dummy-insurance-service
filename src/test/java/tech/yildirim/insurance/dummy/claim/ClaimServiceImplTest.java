package tech.yildirim.insurance.dummy.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;
import tech.yildirim.insurance.dummy.policy.Policy;
import tech.yildirim.insurance.dummy.policy.PolicyRepository;
import tech.yildirim.insurance.api.generated.model.ClaimDto;
import tech.yildirim.insurance.dummy.policy.PolicyStatus;
import tech.yildirim.insurance.dummy.policy.PolicyType;

@ExtendWith(MockitoExtension.class)
@DisplayName("Claim Service Unit Tests")
class ClaimServiceImplTest {

  @Mock private ClaimRepository claimRepository;
  @Mock private PolicyRepository policyRepository;
  @Mock private ClaimMapper claimMapper;

  @InjectMocks private ClaimServiceImpl claimService;

  @Test
  @DisplayName("Should create an AutoClaim when submitting for an active AUTO policy")
  void submitClaim_forAutoPolicy_shouldCreateAutoClaim() {
    // Given: An active AUTO policy and a claim DTO
    long policyId = 1L;
    Policy autoPolicy = new Policy();
    autoPolicy.setId(policyId);
    autoPolicy.setType(PolicyType.AUTO);
    autoPolicy.setStatus(PolicyStatus.ACTIVE);

    ClaimDto inputDto = new ClaimDto().policyId(policyId);

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(autoPolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(claimMapper.toDto(any(Claim.class))).thenReturn(new ClaimDto());

    // When: The submitClaim method is called
    claimService.submitClaim(policyId, inputDto);

    // Then: Verify that an AutoClaim object was passed to the repository's save method
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());

    Claim capturedClaim = claimCaptor.getValue();
    assertThat(capturedClaim).isInstanceOf(AutoClaim.class);
    assertThat(capturedClaim.getPolicy()).isEqualTo(autoPolicy);
    assertThat(capturedClaim.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    assertThat(capturedClaim.getClaimNumber()).isNotNull();
  }

  @Test
  @DisplayName("Should create a HomeClaim when submitting for an active HOME policy")
  void submitClaim_forHomePolicy_shouldCreateHomeClaim() {
    // Given: An active HOME policy
    long policyId = 2L;
    Policy homePolicy = new Policy();
    homePolicy.setId(policyId);
    homePolicy.setType(PolicyType.HOME);
    homePolicy.setStatus(PolicyStatus.ACTIVE);
    ClaimDto inputDto = new ClaimDto().policyId(policyId);

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(homePolicy));
    when(claimRepository.save(any(Claim.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When: The submitClaim method is called
    claimService.submitClaim(policyId, inputDto);

    // Then: Verify that a HomeClaim object was created
    ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(claimRepository).save(claimCaptor.capture());
    assertThat(claimCaptor.getValue()).isInstanceOf(HomeClaim.class);
  }

  @Test
  @DisplayName("Should throw IllegalStateException when submitting a claim for an INACTIVE policy")
  void submitClaim_forInactivePolicy_shouldThrowException() {
    // Given: A PENDING (not ACTIVE) policy
    long policyId = 3L;
    Policy pendingPolicy = new Policy();
    pendingPolicy.setId(policyId);
    pendingPolicy.setStatus(PolicyStatus.PENDING);
    ClaimDto inputDto = new ClaimDto().policyId(policyId);

    when(policyRepository.findById(policyId)).thenReturn(Optional.of(pendingPolicy));

    // When & Then: Assert that the correct exception is thrown
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> claimService.submitClaim(policyId, inputDto));

    assertThat(exception.getMessage())
        .contains("Claims can only be submitted for policies with ACTIVE status");
    verify(claimRepository, never()).save(any());
  }

  @Test
  @DisplayName(
      "Should throw ResourceNotFoundException when submitting a claim for a non-existent policy")
  void submitClaim_forNonExistentPolicy_shouldThrowException() {
    // Given: A non-existent policy ID
    long nonExistentPolicyId = 99L;
    when(policyRepository.findById(nonExistentPolicyId)).thenReturn(Optional.empty());

    // When & Then: Assert that the correct exception is thrown
    assertThrows(
        ResourceNotFoundException.class,
        () -> claimService.submitClaim(nonExistentPolicyId, new ClaimDto()));
  }
}
