package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.entity.enums.Difficulty;
import in.jecrcu.placement.entity.enums.ExperienceStatus;
import in.jecrcu.placement.entity.enums.Outcome;
import in.jecrcu.placement.entity.enums.RoundType;
import in.jecrcu.placement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final DriveRepository driveRepository;
    private final RoundRepository roundRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;

    public List<Experience> getApprovedExperiencesForCompany(Long companyId) {
        return experienceRepository.findApprovedByCompanyId(companyId);
    }

    public List<Experience> getPendingExperiences() {
        return experienceRepository.findByStatusOrderByCreatedAtDesc(ExperienceStatus.PENDING);
    }

    public List<Experience> getUserExperiences(User user) {
        return experienceRepository.findByAuthorOrderByCreatedAtDesc(user);
    }

    public Optional<Experience> findById(Long id) {
        return experienceRepository.findById(id);
    }

    @Transactional
    public Experience submitExperience(
            User author,
            Long driveId,
            boolean anonymous,
            String title,
            String content,
            String tips,
            String mistakesToAvoid,
            Outcome outcome,
            int difficultyRating,
            List<String> roundNames,
            List<String> roundTypes,
            List<String> roundDescriptions,
            List<String> questionTexts,
            List<String> questionTopics
    ) {
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found"));

        Experience exp = Experience.builder()
                .drive(drive)
                .author(author)
                .anonymous(anonymous)
                .title(title)
                .content(content)
                .tips(tips)
                .mistakesToAvoid(mistakesToAvoid)
                .outcome(outcome)
                .difficultyRating(difficultyRating)
                .status(ExperienceStatus.PENDING)
                .upvotes(0)
                .build();

        Experience saved = experienceRepository.save(exp);

        // Save round and question details if provided with the submission
        if (roundNames != null && !roundNames.isEmpty()) {
            for (int i = 0; i < roundNames.size(); i++) {
                String rName = roundNames.get(i);
                if (rName != null && !rName.isBlank()) {
                    RoundType type = RoundType.TECHNICAL;
                    if (roundTypes != null && i < roundTypes.size()) {
                        try {
                            type = RoundType.valueOf(roundTypes.get(i));
                        } catch (Exception ignored) {}
                    }
                    String rDesc = (roundDescriptions != null && i < roundDescriptions.size()) ? roundDescriptions.get(i) : "";

                    Round round = Round.builder()
                            .drive(drive)
                            .roundNumber(i + 1)
                            .name(rName)
                            .roundType(type)
                            .difficulty(Difficulty.MEDIUM)
                            .description(rDesc)
                            .build();
                    Round savedRound = roundRepository.save(round);

                    if (questionTexts != null && i < questionTexts.size()) {
                        String qText = questionTexts.get(i);
                        if (qText != null && !qText.isBlank()) {
                            String qTopic = (questionTopics != null && i < questionTopics.size()) ? questionTopics.get(i) : "General";
                            Question question = Question.builder()
                                    .round(savedRound)
                                    .questionText(qText)
                                    .topicTags(qTopic)
                                    .difficulty(Difficulty.MEDIUM)
                                    .build();
                            questionRepository.save(question);
                        }
                    }
                }
            }
        }

        log.info("Student {} submitted new experience for drive: {}", author.getEmail(), driveId);
        return saved;
    }

    @Transactional
    public void approveExperience(Long experienceId) {
        Experience exp = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found"));

        exp.setStatus(ExperienceStatus.APPROVED);
        exp.setRejectionReason(null);
        experienceRepository.save(exp);

        // Award points (+50) to the author
        userService.addPoints(exp.getAuthor().getId(), 50);
        log.info("Experience {} approved for author {}", experienceId, exp.getAuthor().getEmail());
    }

    @Transactional
    public void rejectExperience(Long experienceId, String reason) {
        Experience exp = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found"));

        exp.setStatus(ExperienceStatus.REJECTED);
        exp.setRejectionReason(reason);
        experienceRepository.save(exp);
        log.info("Experience {} rejected with reason: {}", experienceId, reason);
    }

    @Transactional
    public int upvoteExperience(Long experienceId) {
        Experience exp = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found"));

        int updated = (exp.getUpvotes() != null ? exp.getUpvotes() : 0) + 1;
        exp.setUpvotes(updated);
        experienceRepository.save(exp);

        // Award points (+5) to the author
        userService.addPoints(exp.getAuthor().getId(), 5);
        return updated;
    }

    public boolean isPossibleSpamOrDuplicate(Long driveId, String content) {
        Optional<Drive> drive = driveRepository.findById(driveId);
        if (drive.isEmpty()) return false;

        List<Experience> existing = drive.get().getExperiences();
        if (existing == null) return false;

        for (Experience exp : existing) {
            if (exp.getContent() != null && content != null) {
                double similarity = calculateJaccardSimilarity(exp.getContent(), content);
                if (similarity > 0.80) {
                    return true;
                }
            }
        }
        return false;
    }

    private double calculateJaccardSimilarity(String s1, String s2) {
        String[] w1 = s1.toLowerCase().split("\\s+");
        String[] w2 = s2.toLowerCase().split("\\s+");
        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(w1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(w2));

        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        if (union.isEmpty()) return 0.0;

        set1.retainAll(set2);
        return (double) set1.size() / union.size();
    }
}
