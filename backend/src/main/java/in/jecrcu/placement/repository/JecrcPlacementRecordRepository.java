package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.JecrcPlacementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JecrcPlacementRecordRepository extends JpaRepository<JecrcPlacementRecord, Long> {

    List<JecrcPlacementRecord> findByAcademicYearOrderBySelectionsDesc(String academicYear);

    List<JecrcPlacementRecord> findByAcademicYearAndCompanyNameContainingIgnoreCaseOrderBySelectionsDesc(String academicYear, String search);

    List<JecrcPlacementRecord> findByCompanyNameContainingIgnoreCaseOrderByAcademicYearDesc(String search);

    @Query("SELECT DISTINCT r.academicYear FROM JecrcPlacementRecord r ORDER BY r.academicYear DESC")
    List<String> findDistinctAcademicYears();

    long countByAcademicYear(String academicYear);

    @Query("SELECT COALESCE(SUM(r.selections), 0) FROM JecrcPlacementRecord r WHERE r.academicYear = :academicYear")
    Integer sumSelectionsByAcademicYear(String academicYear);
}
