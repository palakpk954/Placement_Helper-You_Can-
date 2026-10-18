package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.PracticeAttempt;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Difficulty;
import in.jecrcu.placement.entity.enums.PracticeStatus;
import in.jecrcu.placement.repository.PracticeAttemptRepository;
import in.jecrcu.placement.repository.QuestionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final QuestionRepository questionRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;

    public List<Question> searchQuestions(Long companyId, String topic, Difficulty difficulty, String keyword) {
        return questionRepository.searchQuestions(companyId, topic, difficulty, keyword);
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    @Transactional
    public PracticeAttempt recordAttempt(User user, Long questionId, PracticeStatus status, String notes) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Optional<PracticeAttempt> existing = practiceAttemptRepository.findByUserIdAndQuestionId(user.getId(), questionId);
        PracticeAttempt attempt;
        if (existing.isPresent()) {
            attempt = existing.get();
            attempt.setStatus(status);
            if (notes != null) attempt.setNotes(notes);
            attempt.setUpdatedAt(LocalDateTime.now());
        } else {
            attempt = PracticeAttempt.builder()
                    .user(user)
                    .question(question)
                    .status(status)
                    .notes(notes)
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        return practiceAttemptRepository.save(attempt);
    }

    public Optional<PracticeAttempt> getUserAttempt(Long userId, Long questionId) {
        return practiceAttemptRepository.findByUserIdAndQuestionId(userId, questionId);
    }

    @Getter
    public static class PracticeProgressStats {
        private final long totalAttempted;
        private final long solvedCount;
        private final long unsureCount;
        private final long skippedCount;
        private final Map<String, Integer> topicSolvedCounts;
        private final List<String> weakTopics;

        public PracticeProgressStats(long totalAttempted, long solved, long unsure, long skipped,
                                     Map<String, Integer> topicSolved, List<String> weakTopics) {
            this.totalAttempted = totalAttempted;
            this.solvedCount = solved;
            this.unsureCount = unsure;
            this.skippedCount = skipped;
            this.topicSolvedCounts = topicSolved;
            this.weakTopics = weakTopics;
        }
    }

    public PracticeProgressStats getStudentProgress(Long userId) {
        List<PracticeAttempt> attempts = practiceAttemptRepository.findByUserId(userId);
        long solved = 0;
        long unsure = 0;
        long skipped = 0;

        Map<String, Integer> topicSolved = new HashMap<>();
        Map<String, Integer> topicUnsureOrSkipped = new HashMap<>();

        for (PracticeAttempt a : attempts) {
            Question q = a.getQuestion();
            String tags = q.getTopicTags();
            if (a.getStatus() == PracticeStatus.SOLVED) {
                solved++;
                if (tags != null) {
                    for (String t : tags.split(",")) {
                        String clean = t.trim();
                        topicSolved.put(clean, topicSolved.getOrDefault(clean, 0) + 1);
                    }
                }
            } else if (a.getStatus() == PracticeStatus.UNSURE) {
                unsure++;
                if (tags != null) {
                    for (String t : tags.split(",")) {
                        String clean = t.trim();
                        topicUnsureOrSkipped.put(clean, topicUnsureOrSkipped.getOrDefault(clean, 0) + 1);
                    }
                }
            } else if (a.getStatus() == PracticeStatus.SKIPPED) {
                skipped++;
                if (tags != null) {
                    for (String t : tags.split(",")) {
                        String clean = t.trim();
                        topicUnsureOrSkipped.put(clean, topicUnsureOrSkipped.getOrDefault(clean, 0) + 1);
                    }
                }
            }
        }

        // Highlight weak topics: high unsure/skipped or low solve ratio
        List<String> weakTopics = topicUnsureOrSkipped.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        return new PracticeProgressStats(attempts.size(), solved, unsure, skipped, topicSolved, weakTopics);
    }
}
