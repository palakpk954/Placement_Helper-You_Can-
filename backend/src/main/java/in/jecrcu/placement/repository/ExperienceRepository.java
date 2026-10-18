package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Experience;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ExperienceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByStatusOrderByCreatedAtDesc(ExperienceStatus status);

    List<Experience> findByAuthorOrderByCreatedAtDesc(User author);

    @Query("SELECT e FROM Experience e " +
           "JOIN e.drive d " +
           "WHERE d.company.id = :companyId " +
           "AND e.status = 'APPROVED' " +
           "ORDER BY e.upvotes DESC, e.id DESC")
    List<Experience> findApprovedByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(e) FROM Experience e WHERE e.author.id = :authorId AND e.status = 'APPROVED'")
    long countApprovedByAuthorId(@Param("authorId") Long authorId);

    boolean existsByAuthorIdAndDriveId(Long authorId, Long driveId);
}
