package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "drive_registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "upcoming_drive_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upcoming_drive_id", nullable = false)
    private UpcomingDrive upcomingDrive;

    @Column(nullable = false)
    @Builder.Default
    private String status = "REGISTERED"; // REGISTERED, WITHDRAWN

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }
}
