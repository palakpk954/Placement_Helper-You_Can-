package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roadmap_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private String topic;

    @Column(name = "target_questions")
    @Builder.Default
    private Integer targetQuestions = 5;

    @Builder.Default
    private Boolean completed = false;

    @Transient
    public String getDescription() {
        if (targetQuestions != null && targetQuestions > 0) {
            return "Complete " + targetQuestions + " high-frequency placement practice problems and review core concepts.";
        }
        return "Complete weekly targeted curriculum and placement mock questions.";
    }
}
