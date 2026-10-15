package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.MockBooking;
import in.jecrcu.placement.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockBookingRepository extends JpaRepository<MockBooking, Long> {
    List<MockBooking> findByStudentIdOrderByMockSlotStartTimeDesc(Long studentId);
    Optional<MockBooking> findByMockSlotId(Long slotId);
    long countByMockSlotMentorIdAndStatus(Long mentorId, BookingStatus status);
}
