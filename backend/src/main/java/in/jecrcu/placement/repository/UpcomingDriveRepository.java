package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.UpcomingDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UpcomingDriveRepository extends JpaRepository<UpcomingDrive, Long> {

    List<UpcomingDrive> findByActiveTrueOrderByDriveDateAsc();

    List<UpcomingDrive> findByActiveTrueAndDeadlineAfterOrderByDriveDateAsc(LocalDateTime now);

    @Query("SELECT u FROM UpcomingDrive u WHERE u.active = true AND u.deadline BETWEEN :start AND :end")
    List<UpcomingDrive> findDrivesWithDeadlineApproaching(LocalDateTime start, LocalDateTime end);
}
