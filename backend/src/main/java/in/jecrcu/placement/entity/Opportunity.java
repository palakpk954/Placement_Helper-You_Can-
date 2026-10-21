package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.OpportunityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityType type;

    @Column(name = "category")
    @Builder.Default
    private String category = "Technology";

    @Column(name = "mode")
    @Builder.Default
    private String mode = "Remote";

    @Column(name = "location")
    private String location;

    @Column(name = "compensation_or_prize", nullable = false)
    private String compensationOrPrize;

    @Column(name = "eligible_batches")
    @Builder.Default
    private String eligibleBatches = "All Batches";

    @Column(name = "eligible_branches")
    @Builder.Default
    private String eligibleBranches = "All Branches";

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "registration_url", nullable = false, length = 1000)
    private String registrationUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "perks")
    private String perks;

    @Builder.Default
    private Boolean featured = false;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.featured == null) this.featured = false;
        if (this.mode == null) this.mode = "Remote";
        if (this.category == null) this.category = "Technology";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
