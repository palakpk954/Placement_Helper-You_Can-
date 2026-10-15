package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.MockBooking;
import in.jecrcu.placement.entity.MockSlot;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.BookingStatus;
import in.jecrcu.placement.entity.enums.SlotStatus;
import in.jecrcu.placement.repository.MockBookingRepository;
import in.jecrcu.placement.repository.MockSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockSlotRepository slotRepository;
    private final MockBookingRepository bookingRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<MockSlot> getAvailableSlots() {
        return slotRepository.findByStatusAndStartTimeAfterOrderByStartTimeAsc(
                SlotStatus.AVAILABLE, LocalDateTime.now());
    }

    public List<MockSlot> getMentorSlots(Long mentorId) {
        return slotRepository.findByMentorIdOrderByStartTimeDesc(mentorId);
    }

    public List<MockBooking> getStudentBookings(Long studentId) {
        return bookingRepository.findByStudentIdOrderByMockSlotStartTimeDesc(studentId);
    }

    @Transactional
    public MockSlot createSlot(User mentor, String companyName, LocalDateTime startTime, LocalDateTime endTime) {
        MockSlot slot = MockSlot.builder()
                .mentor(mentor)
                .companyName(companyName)
                .startTime(startTime)
                .endTime(endTime)
                .status(SlotStatus.AVAILABLE)
                .build();
        return slotRepository.save(slot);
    }

    @Transactional
    public MockBooking bookSlot(User student, Long slotId, String notes) {
        MockSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalStateException("Slot is no longer available.");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        MockBooking booking = MockBooking.builder()
                .mockSlot(slot)
                .student(student)
                .notes(notes)
                .status(BookingStatus.CONFIRMED)
                .build();

        MockBooking saved = bookingRepository.save(booking);

        // Notify both parties (NOT-1)
        notificationService.notifyMockBooking(student, slot.getMentor(), slot.getCompanyName(), true);

        return saved;
    }

    @Transactional
    public void cancelBooking(Long bookingId, User actor) {
        MockBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        MockSlot slot = booking.getMockSlot();
        booking.setStatus(BookingStatus.CANCELLED);
        slot.setStatus(SlotStatus.CANCELLED);

        bookingRepository.save(booking);
        slotRepository.save(slot);

        // Notify mentor (NOT-1)
        notificationService.notifyMockBooking(booking.getStudent(), slot.getMentor(), slot.getCompanyName(), false);
    }

    @Transactional
    public void submitFeedback(Long bookingId, int rating, String feedback) {
        MockBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setRating(rating);
        booking.setFeedback(feedback);
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        // Award points (+30) to the mentor for completing a mock session
        userService.addPoints(booking.getMockSlot().getMentor().getId(), 30);
    }
}
