package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.PracticeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, Long> {

    List<PracticeAttempt> findByUserId(Long userId);

    Optional<PracticeAttempt> findByUserIdAndQuestionId(Long userId, Long questionId);

    @Query("SELECT p.status, COUNT(p) FROM PracticeAttempt p WHERE p.user.id = :userId GROUP BY p.status")
    List<Object[]> countAttemptsByStatusForUser(@Param("userId") Long userId);

    @Query("SELECT q.topicTags, COUNT(p) FROM PracticeAttempt p JOIN p.question q WHERE p.user.id = :userId AND p.status = 'SOLVED' GROUP BY q.topicTags")
    List<Object[]> countSolvedByTopicForUser(@Param("userId") Long userId);
}
