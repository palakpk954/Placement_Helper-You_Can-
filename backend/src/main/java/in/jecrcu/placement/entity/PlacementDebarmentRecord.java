package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "placement_debarment_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDebarmentRecord {

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

    @Column(length = 50)
    private String program;

    @Column(length = 150)
    private String college;

    @Column(length = 150)
    private String course;

    @Column(name = "absent_count")
    @Builder.Default
    private Integer absentCount = 1;

    @Column(name = "companies_details", columnDefinition = "TEXT")
    private String companiesDetails;

    @Column(name = "penalty_status", length = 50)
    @Builder.Default
    private String penaltyStatus = "ACTIVE_PENALTY";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

