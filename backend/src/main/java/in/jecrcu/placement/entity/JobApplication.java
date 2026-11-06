package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.ApplicationStage;
import in.jecrcu.placement.entity.enums.ApplicationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upcoming_drive_id")
    private UpcomingDrive upcomingDrive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "role_title", nullable = false, length = 150)
    private String roleTitle;

    @Column(name = "ctc_or_stipend", length = 100)
    private String ctcOrStipend;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false, length = 30)
    @Builder.Default
    private ApplicationType applicationType = ApplicationType.ON_CAMPUS;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 50)
    @Builder.Default
    private ApplicationStage stage = ApplicationStage.APPLIED;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "next_event_date")
    private LocalDateTime nextEventDate;

    @Column(name = "next_event_title", length = 150)
    private String nextEventTitle;

    @Column(name = "interview_location", length = 150)
    private String interviewLocation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rating")
    @Builder.Default
    private Integer rating = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.appliedAt == null) {
            this.appliedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
