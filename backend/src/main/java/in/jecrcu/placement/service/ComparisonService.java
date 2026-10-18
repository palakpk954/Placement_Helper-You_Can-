package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.Round;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final CompanyService companyService;

    @Getter
    public static class CompanyComparisonProfile {
        private final Company company;
        private final int totalVisits;
        private final String latestRoles;
        private final String latestCtc;
        private final double maxCtc;
        private final String minCgpa;
        private final String eligibleBranches;
        private final String workMode;
        private final int typicalRoundsCount;
        private final List<String> roundNames;
        private final Set<String> topTopics;
        private final double difficultyRating;

        public CompanyComparisonProfile(Company c, double maxCtc) {
            this.company = c;
            List<Drive> drives = c.getDrives();
            this.totalVisits = drives != null ? drives.size() : 0;
            this.maxCtc = maxCtc;
            this.difficultyRating = c.getAverageDifficulty() != null ? c.getAverageDifficulty() : 3.5;

            if (drives != null && !drives.isEmpty()) {
                Drive latest = drives.get(0);
                this.latestRoles = latest.getRoles();
                this.latestCtc = latest.getCtcOrStipend();
                this.minCgpa = latest.getMinCgpa() != null ? latest.getMinCgpa().toString() : "6.00";
                this.eligibleBranches = latest.getEligibleBranches();
                this.workMode = latest.getWorkMode() != null ? latest.getWorkMode().name() : "HYBRID";

                List<Round> rounds = latest.getRounds();
                this.typicalRoundsCount = rounds != null ? rounds.size() : 0;
                this.roundNames = rounds != null ? rounds.stream().map(Round::getName).collect(Collectors.toList()) : List.of();

                Set<String> topics = new TreeSet<>();
                if (rounds != null) {
                    for (Round r : rounds) {
                        if (r.getQuestions() != null) {
                            for (Question q : r.getQuestions()) {
                                if (q.getTopicTags() != null) {
                                    for (String t : q.getTopicTags().split(",")) {
                                        topics.add(t.trim());
                                    }
                                }
                            }
                        }
                    }
                }
                this.topTopics = topics;
            } else {
                this.latestRoles = "N/A";
                this.latestCtc = "N/A";
                this.minCgpa = "N/A";
                this.eligibleBranches = "N/A";
                this.workMode = "N/A";
                this.typicalRoundsCount = 0;
                this.roundNames = List.of();
                this.topTopics = Set.of();
            }
        }
    }

    public Optional<CompanyComparisonProfile> getProfile(Long companyId) {
        return companyService.findById(companyId)
                .map(c -> new CompanyComparisonProfile(c, companyService.extractMaxCtc(c)));
    }
}
