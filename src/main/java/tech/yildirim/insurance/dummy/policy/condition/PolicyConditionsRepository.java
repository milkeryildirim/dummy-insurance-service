package tech.yildirim.insurance.dummy.policy.condition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for the {@link PolicyConditions} entity. */
@Repository
public interface PolicyConditionsRepository extends JpaRepository<PolicyConditions, Long> {}
