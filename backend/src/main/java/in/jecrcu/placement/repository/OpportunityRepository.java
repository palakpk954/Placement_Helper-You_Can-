package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Opportunity;
import in.jecrcu.placement.entity.enums.OpportunityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    List<Opportunity> findByActiveTrueOrderByFeaturedDescDeadlineAsc();

    List<Opportunity> findByActiveTrueAndTypeOrderByFeaturedDescDeadlineAsc(OpportunityType type);

    @Query("SELECT o FROM Opportunity o WHERE o.active = true AND " +
           "(LOWER(o.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(o.organization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(o.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(o.eligibleBranches) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY o.featured DESC, o.deadline ASC")
    List<Opportunity> searchActive(@Param("query") String query);

    @Query("SELECT o FROM Opportunity o WHERE o.active = true AND o.type = :type AND " +
           "(LOWER(o.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(o.organization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(o.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(o.eligibleBranches) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY o.featured DESC, o.deadline ASC")
    List<Opportunity> searchActiveByType(@Param("type") OpportunityType type, @Param("query") String query);

    List<Opportunity> findAllByOrderByCreatedAtDesc();

    List<Opportunity> findTop4ByActiveTrueOrderByFeaturedDescDeadlineAsc();

    long countByActiveTrue();

    long countByTypeAndActiveTrue(OpportunityType type);
}
