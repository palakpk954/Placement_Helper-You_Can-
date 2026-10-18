package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    @OneToOne(mappedBy = "mockSlot", cascade = CascadeType.ALL)
    private MockBooking booking;
}
