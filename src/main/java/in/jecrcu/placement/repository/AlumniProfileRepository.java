package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.AlumniProfile;
import in.jecrcu.placement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {

    Optional<AlumniProfile> findByUser(User user);
    Optional<AlumniProfile> findByUserId(Long userId);

    List<AlumniProfile> findByVisibleTrueOrderByGraduationYearDesc();

    @Query("SELECT a FROM AlumniProfile a WHERE a.visible = true " +
           "AND (:company IS NULL OR LOWER(a.companyName) LIKE LOWER(CONCAT('%', :company, '%'))) " +
           "AND (:branch IS NULL OR a.branch = :branch) " +
           "AND (:year IS NULL OR a.graduationYear = :year) " +
           "ORDER BY a.graduationYear DESC")
    List<AlumniProfile> searchAlumni(
            @Param("company") String company,
            @Param("branch") String branch,
            @Param("year") Integer year
    );

    List<AlumniProfile> findByCompanyNameIgnoreCaseAndVisibleTrue(String companyName);
}
