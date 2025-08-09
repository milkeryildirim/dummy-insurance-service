package tech.yildirim.insurance.dummy.customer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for the {@link Customer} entity. */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  /**
   * Finds customers where the search term appears in either the first name or the last name,
   * case-insensitively. This is an example of a custom JPQL query.
   *
   * @param name the search term to look for.
   * @return a list of customers matching the search term.
   */
  @Query(
      "SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
  List<Customer> searchByName(@Param("name") String name);
}
