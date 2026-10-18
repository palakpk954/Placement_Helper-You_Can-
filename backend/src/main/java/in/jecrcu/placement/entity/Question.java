package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "topic_tags", nullable = false)
    private String topicTags;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "answer_or_hint", columnDefinition = "TEXT")
    private String answerOrHint;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
