package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;

    @Builder.Default
    private String tier = "Mass Recruiters";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("driveYear DESC, id DESC")
    private List<Drive> drives = new ArrayList<>();

    @Transient
    private Double averageDifficulty;

    @Transient
    private Long totalVisits;

    @Transient
    private String mostRecentVisit;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
