package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.Round;
import in.jecrcu.placement.entity.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByRound(Round round);
    List<Question> findByRoundId(Long roundId);

    @Query("SELECT q FROM Question q " +
           "JOIN q.round r " +
           "JOIN r.drive d " +
           "JOIN d.company c " +
           "WHERE (:companyId IS NULL OR c.id = :companyId) " +
           "AND (:topic IS NULL OR LOWER(q.topicTags) LIKE LOWER(CONCAT('%', :topic, '%'))) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:keyword IS NULL OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY q.id DESC")
    List<Question> searchQuestions(
            @Param("companyId") Long companyId,
            @Param("topic") String topic,
            @Param("difficulty") Difficulty difficulty,
            @Param("keyword") String keyword
    );

    @Query("SELECT q.topicTags FROM Question q")
    List<String> findAllTopicTags();

    @Query("SELECT q FROM Question q WHERE LOWER(q.topicTags) LIKE LOWER(CONCAT('%', :topic, '%'))")
    List<Question> findByTopicContaining(@Param("topic") String topic);
}
