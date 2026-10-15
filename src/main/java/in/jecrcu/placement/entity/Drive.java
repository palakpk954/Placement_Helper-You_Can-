package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.JobType;
import in.jecrcu.placement.entity.enums.WorkMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "drive_year", nullable = false)
    private Integer driveYear;

    @Column(name = "drive_month", nullable = false)
    private String driveMonth;

    @Column(nullable = false)
    private String roles;

    @Column(name = "ctc_or_stipend", nullable = false)
    private String ctcOrStipend;

    @Column(name = "min_cgpa", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal minCgpa = BigDecimal.valueOf(6.00);

    @Column(name = "eligible_branches", nullable = false)
    private String eligibleBranches;

    @Column(name = "max_backlogs")
    @Builder.Default
    private Integer maxBacklogs = 0;

    @Column(name = "students_selected")
    @Builder.Default
    private Integer studentsSelected = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    @Builder.Default
    private JobType jobType = JobType.PLACEMENT;

    @Column(name = "joining_location")
    private String joiningLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode")
    @Builder.Default
    private WorkMode workMode = WorkMode.HYBRID;

    @Column(name = "bond_details")
    private String bondDetails;

    @Column(name = "conversion_offered")
    @Builder.Default
    private Boolean conversionOffered = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("roundNumber ASC")
    private List<Round> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("upvotes DESC")
    private List<Experience> experiences = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
