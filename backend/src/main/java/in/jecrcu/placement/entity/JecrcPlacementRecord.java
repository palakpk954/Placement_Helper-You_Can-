package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jecrc_placement_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JecrcPlacementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "package_lpa", nullable = false)
    private String packageLpa;

    @Builder.Default
    @Column(nullable = false)
    private Integer selections = 0;

    @Builder.Default
    private String tier = "Mass & Elite";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
