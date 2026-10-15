package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "upcoming_drives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "drive_date", nullable = false)
    private LocalDate driveDate;

    @Column(name = "roles_offered", nullable = false)
    private String rolesOffered;

    @Column(name = "min_cgpa", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal minCgpa = BigDecimal.valueOf(6.00);

    @Column(name = "eligible_branches", nullable = false)
    private String eligibleBranches;

    @Column(name = "max_backlogs")
    @Builder.Default
    private Integer maxBacklogs = 0;

    @Column(name = "ctc_or_stipend", nullable = false)
    private String ctcOrStipend;

    @Column(name = "registration_link")
    private String registrationLink;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public String getRoles() {
        return rolesOffered;
    }

    public Integer getMaxActiveBacklogs() {
        return maxBacklogs;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
