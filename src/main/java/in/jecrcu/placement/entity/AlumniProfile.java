package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumni_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "job_role", nullable = false)
    private String jobRole;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @Column(nullable = false)
    private String branch;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "topics_can_help")
    private String topicsCanHelp;

    @Builder.Default
    private Boolean visible = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
