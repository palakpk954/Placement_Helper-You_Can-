package in.jecrcu.placement.entity;

import in.jecrcu.placement.entity.enums.Difficulty;
import in.jecrcu.placement.entity.enums.RoundType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    private Drive drive;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false)
    private RoundType roundType;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();
}
