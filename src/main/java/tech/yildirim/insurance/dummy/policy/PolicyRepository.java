package tech.yildirim.insurance.dummy.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Policy} entity.
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

}

