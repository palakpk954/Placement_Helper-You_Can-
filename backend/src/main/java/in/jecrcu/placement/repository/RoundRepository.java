package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByDriveOrderByRoundNumberAsc(Drive drive);
    List<Round> findByDriveIdOrderByRoundNumberAsc(Long driveId);
}
