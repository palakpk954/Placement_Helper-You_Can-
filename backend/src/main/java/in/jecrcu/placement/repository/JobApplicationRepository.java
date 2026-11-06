package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.JobApplication;
import in.jecrcu.placement.entity.enums.ApplicationStage;
import in.jecrcu.placement.entity.enums.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUserIdOrderByAppliedAtDesc(Long userId);

    List<JobApplication> findByUserIdAndStageOrderByAppliedAtDesc(Long userId, ApplicationStage stage);

    Optional<JobApplication> findByUserIdAndUpcomingDriveId(Long userId, Long upcomingDriveId);

    List<JobApplication> findByUserIdAndApplicationTypeOrderByAppliedAtDesc(Long userId, ApplicationType applicationType);

    @Query("SELECT j FROM JobApplication j " +
           "WHERE j.user.id = :userId " +
           "AND (:type IS NULL OR j.applicationType = :type) " +
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(j.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(j.roleTitle) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY j.appliedAt DESC")
    List<JobApplication> searchApplications(
            @Param("userId") Long userId,
            @Param("type") ApplicationType type,
            @Param("query") String query
    );

    long countByUserId(Long userId);

    long countByUserIdAndStage(Long userId, ApplicationStage stage);

    @Query("SELECT j FROM JobApplication j " +
           "WHERE j.user.id = :userId " +
           "AND j.nextEventDate IS NOT NULL " +
           "AND j.nextEventDate >= CURRENT_TIMESTAMP " +
           "ORDER BY j.nextEventDate ASC")
    List<JobApplication> findUpcomingInterviews(@Param("userId") Long userId);
}
