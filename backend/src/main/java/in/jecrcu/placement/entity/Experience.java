package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.ExperienceStatus;
import in.jecrcu.placement.entity.enums.Outcome;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    private Drive drive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean anonymous = false;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String tips;

    @Column(name = "mistakes_to_avoid", columnDefinition = "TEXT")
    private String mistakesToAvoid;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Outcome outcome = Outcome.SELECTED;

    @Column(name = "difficulty_rating")
    @Builder.Default
    private Integer difficultyRating = 3;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExperienceStatus status = ExperienceStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Builder.Default
    private Integer upvotes = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (upvotes == null) {
            upvotes = 0;
        }
        if (anonymous == null) {
            anonymous = false;
        }
        if (difficultyRating == null) {
            difficultyRating = 3;
        }
    }
}
