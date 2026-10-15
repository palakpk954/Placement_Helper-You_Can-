package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Integer weeks;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("weekNumber ASC")
    private List<RoadmapItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getTotalWeeks() {
        return this.weeks;
    }

    @Transient
    public int getCompletedCount() {
        if (items == null) return 0;
        return (int) items.stream().filter(item -> Boolean.TRUE.equals(item.getCompleted())).count();
    }

    @Transient
    public int getProgressPercentage() {
        if (items == null || items.isEmpty()) return 0;
        return (int) Math.round(((double) getCompletedCount() / items.size()) * 100);
    }

    @Transient
    public boolean isAllCompleted() {
        if (items == null || items.isEmpty()) return false;
        return getCompletedCount() == items.size();
    }
}
