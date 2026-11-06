package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_placement_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPlacementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roll_number", nullable = false, length = 50)
    private String rollNumber;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(length = 50)
    private String section;

    @Column(length = 150)
    private String mentor;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String mobile;

    @Column(length = 20)
    private String gender;

    @Column(name = "placement_status", nullable = false, length = 50)
    @Builder.Default
    private String placementStatus = "ENROLLED";

    @Column(length = 150)
    private String college;

    @Column(length = 50)
    private String program;

    @Column(length = 150)
    private String course;

    @Column(name = "eligible_profiles")
    @Builder.Default
    private Integer eligibleProfiles = 0;

    @Column(name = "not_eligible_profiles")
    @Builder.Default
    private Integer notEligibleProfiles = 0;

    @Column(name = "eligible_during_window")
    @Builder.Default
    private Integer eligibleDuringWindow = 0;

    @Column(name = "total_applications")
    @Builder.Default
    private Integer totalApplications = 0;

    @Column(name = "ppt_count")
    @Builder.Default
    private Integer pptCount = 0;

    @Column(name = "screening_count")
    @Builder.Default
    private Integer screeningCount = 0;

    @Column(name = "gd_count")
    @Builder.Default
    private Integer gdCount = 0;

    @Column(name = "pi_count")
    @Builder.Default
    private Integer piCount = 0;

    @Column(name = "other_stages_count")
    @Builder.Default
    private Integer otherStagesCount = 0;

    @Column(name = "attempts_count")
    @Builder.Default
    private Integer attemptsCount = 0;

    @Column(name = "offers_count")
    @Builder.Default
    private Integer offersCount = 0;

    @Column(name = "placed_company", length = 150)
    private String placedCompany;

    @Column(name = "placed_role_title", length = 255)
    private String placedRoleTitle;

    @Column(name = "placed_profile_opened", length = 255)
    private String placedProfileOpened;

    @Column(length = 100)
    private String ctc;

    @Column(name = "ctc_lpa", precision = 6, scale = 2)
    private BigDecimal ctcLpa;

    @Column(length = 20)
    private String ppo;

    @Column(name = "job_location", length = 150)
    private String jobLocation;

    @Column(length = 150)
    private String sector;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isPlaced() {
        return "PLACED".equalsIgnoreCase(placementStatus);
    }

    public boolean isBlocked() {
        return "BLOCKED".equalsIgnoreCase(placementStatus);
    }
}

