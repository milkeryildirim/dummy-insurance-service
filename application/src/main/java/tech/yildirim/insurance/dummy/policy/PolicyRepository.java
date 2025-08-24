package tech.yildirim.insurance.dummy.policy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for the {@link Policy} entity. */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

  /**
   * Finds all policies associated with a given customer ID. Spring Data JPA will automatically
   * generate the query for this method.
   *
   * @param customerId The ID of the customer.
   * @return A list of policies belonging to the customer.
   */
  List<Policy> findByCustomerId(Long customerId);

  /**
   * Finds a policy by its unique policy number.
   * @param policyNumber The unique number of the policy.
   * @return An Optional containing the found policy.
   */
  Optional<Policy> findByPolicyNumber(String policyNumber);
}
