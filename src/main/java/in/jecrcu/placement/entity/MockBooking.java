package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mock_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private MockSlot mockSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;
}
