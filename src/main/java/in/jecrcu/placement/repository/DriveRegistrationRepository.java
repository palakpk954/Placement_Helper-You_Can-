package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.DriveRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveRegistrationRepository extends JpaRepository<DriveRegistration, Long> {

    List<DriveRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId);

    List<DriveRegistration> findByUpcomingDriveIdOrderByRegisteredAtDesc(Long driveId);

    Optional<DriveRegistration> findByUserIdAndUpcomingDriveId(Long userId, Long upcomingDriveId);

    boolean existsByUserIdAndUpcomingDriveId(Long userId, Long upcomingDriveId);
}
