package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.PlacementDebarmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlacementDebarmentRecordRepository extends JpaRepository<PlacementDebarmentRecord, Long> {

    List<PlacementDebarmentRecord> findByRollNumber(String rollNumber);

    List<PlacementDebarmentRecord> findByPenaltyStatus(String penaltyStatus);

    List<PlacementDebarmentRecord> findAllByOrderByAbsentCountDesc();

    long countByPenaltyStatus(String penaltyStatus);
}

