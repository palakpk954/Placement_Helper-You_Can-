package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_drives")
    @Builder.Default
    private Boolean emailDrives = true;

    @Column(name = "email_deadlines")
    @Builder.Default
    private Boolean emailDeadlines = true;

    @Column(name = "email_experiences")
    @Builder.Default
    private Boolean emailExperiences = true;

    @Column(name = "email_bookings")
    @Builder.Default
    private Boolean emailBookings = true;
}
