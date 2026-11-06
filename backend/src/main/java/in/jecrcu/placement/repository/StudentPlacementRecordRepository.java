package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.StudentPlacementRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPlacementRecordRepository extends JpaRepository<StudentPlacementRecord, Long> {

    Optional<StudentPlacementRecord> findByRollNumber(String rollNumber);

    Optional<StudentPlacementRecord> findByEmailIgnoreCase(String email);

    List<StudentPlacementRecord> findByPlacementStatus(String placementStatus);

    long countByPlacementStatus(String placementStatus);

    @Query("SELECT r FROM StudentPlacementRecord r WHERE r.placementStatus != 'PLACED' AND r.eligibleProfiles >= 25 AND r.totalApplications <= 2 ORDER BY r.eligibleProfiles DESC")
    List<StudentPlacementRecord> findLowApplicationRisk();

    @Query("SELECT r FROM StudentPlacementRecord r WHERE r.placementStatus != 'PLACED' AND r.totalApplications >= 10 AND r.screeningCount <= 1 ORDER BY r.totalApplications DESC")
    List<StudentPlacementRecord> findScreeningBottleneckRisk();

    @Query("SELECT r FROM StudentPlacementRecord r WHERE r.placementStatus != 'PLACED' AND r.piCount >= 2 AND r.offersCount = 0 ORDER BY r.piCount DESC")
    List<StudentPlacementRecord> findInterviewConversionRisk();

    @Query("SELECT r.placedCompany, COUNT(r), AVG(r.ctcLpa), MAX(r.ctcLpa) " +
           "FROM StudentPlacementRecord r " +
           "WHERE r.placementStatus = 'PLACED' AND r.placedCompany IS NOT NULL AND r.placedCompany NOT IN ('-', '') " +
           "GROUP BY r.placedCompany " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findTopRecruiters();

    @Query("SELECT r.course, COUNT(r), " +
           "SUM(CASE WHEN r.placementStatus = 'PLACED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.placementStatus = 'BLOCKED' THEN 1 ELSE 0 END) " +
           "FROM StudentPlacementRecord r " +
           "GROUP BY r.course " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findCourseStatistics();

    @Query("SELECT r.mentor, COUNT(r), " +
           "SUM(CASE WHEN r.placementStatus = 'PLACED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.placementStatus = 'BLOCKED' THEN 1 ELSE 0 END) " +
           "FROM StudentPlacementRecord r " +
           "WHERE r.mentor IS NOT NULL AND r.mentor != '' " +
           "GROUP BY r.mentor " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findMentorStatistics();

    @Query("SELECT AVG(r.ctcLpa) FROM StudentPlacementRecord r WHERE r.placementStatus = 'PLACED' AND r.ctcLpa IS NOT NULL")
    BigDecimal findAveragePackage();

    @Query("SELECT MAX(r.ctcLpa) FROM StudentPlacementRecord r WHERE r.placementStatus = 'PLACED' AND r.ctcLpa IS NOT NULL")
    BigDecimal findHighestPackage();

    @Query("SELECT DISTINCT r.course FROM StudentPlacementRecord r ORDER BY r.course")
    List<String> findDistinctCourses();

    @Query("SELECT DISTINCT r.mentor FROM StudentPlacementRecord r WHERE r.mentor IS NOT NULL AND r.mentor != '' ORDER BY r.mentor")
    List<String> findDistinctMentors();

    @Query("SELECT r FROM StudentPlacementRecord r WHERE " +
           "(:search IS NULL OR LOWER(r.studentName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.placedCompany) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR r.placementStatus = :status) AND " +
           "(:course IS NULL OR r.course = :course) AND " +
           "(:mentor IS NULL OR r.mentor = :mentor)")
    Page<StudentPlacementRecord> filterStudents(String search, String status, String course, String mentor, Pageable pageable);
}
