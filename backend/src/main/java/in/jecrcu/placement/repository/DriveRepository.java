package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {

    List<Drive> findByCompanyOrderByDriveYearDesc(Company company);

    List<Drive> findByDriveYearOrderByDriveMonthAsc(Integer year);

    List<Drive> findByDriveYearAndDriveMonthIgnoreCase(Integer year, String month);

    @Query("SELECT d FROM Drive d ORDER BY d.driveYear DESC, d.id DESC")
    List<Drive> findAllOrdered();

    @Query("SELECT d.company.name, SUM(d.studentsSelected) as total " +
           "FROM Drive d GROUP BY d.company.name ORDER BY total DESC")
    List<Object[]> findTopRecruitersBySelections();

    @Query("SELECT d.driveYear, COUNT(DISTINCT d.company.id) " +
           "FROM Drive d GROUP BY d.driveYear ORDER BY d.driveYear ASC")
    List<Object[]> countCompaniesPerYear();

    @Query("SELECT d.driveMonth, COUNT(d) " +
           "FROM Drive d WHERE d.driveYear = :year GROUP BY d.driveMonth")
    List<Object[]> countDrivesPerMonthForYear(@Param("year") Integer year);
}
