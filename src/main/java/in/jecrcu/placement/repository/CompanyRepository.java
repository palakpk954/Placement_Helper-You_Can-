package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Company;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);
    Optional<Company> findByNameIgnoreCase(String name);

    List<Company> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT c.industry FROM Company c ORDER BY c.industry ASC")
    List<String> findDistinctIndustries();

    @Query("SELECT DISTINCT d.driveYear FROM Drive d ORDER BY d.driveYear DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT DISTINCT d.driveMonth FROM Drive d ORDER BY d.driveMonth ASC")
    List<String> findDistinctMonths();

    @Query("SELECT DISTINCT d.roles FROM Drive d")
    List<String> findDistinctRoles();
}
