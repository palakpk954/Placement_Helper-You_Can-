package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.MockSlot;
import in.jecrcu.placement.entity.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MockSlotRepository extends JpaRepository<MockSlot, Long> {
    List<MockSlot> findByStatusAndStartTimeAfterOrderByStartTimeAsc(SlotStatus status, LocalDateTime now);
    List<MockSlot> findByMentorIdOrderByStartTimeDesc(Long mentorId);
}
