package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.RoadmapItemRepository;
import in.jecrcu.placement.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final CompanyRepository companyRepository;

    public List<Roadmap> getUserRoadmaps(Long userId) {
        return roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Roadmap> getRoadmapById(Long roadmapId) {
        return roadmapRepository.findById(roadmapId);
    }

    @Transactional
    public Roadmap generateRoadmap(User user, Long companyId, int weeks) {
        if (weeks < 1) weeks = 1;
        if (weeks > 8) weeks = 8;

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // Delete existing roadmap for this user/company if present
        roadmapRepository.findByUserIdAndCompanyId(user.getId(), companyId)
                .ifPresent(roadmapRepository::delete);

        Roadmap roadmap = Roadmap.builder()
                .user(user)
                .company(company)
                .weeks(weeks)
                .build();
        Roadmap saved = roadmapRepository.save(roadmap);

        // Core curriculum templates to distribute across selected weeks
        List<String> topics = List.of(
                "Aptitude & Speed Math (Percentages, P&C, Probability, Logic)",
                "Arrays & Strings: Two Pointers, Sliding Window, Prefix Sums",
                "Linked Lists, Stacks, Queues & Rotated Binary Search",
                "Trees & Binary Search Trees (LCA, Traversals, Path Sums)",
                "Graphs: BFS, DFS, Cycle Detection & Shortest Paths",
                "Dynamic Programming: 0/1 Knapsack, LCS, House Robber patterns",
                "DBMS (SQL Joins, Indexing) & OS (Process, Threads, Memory Paging)",
                "System Design Basics, Resume Projects Deep Dive & HR STAR Stories"
        );

        List<RoadmapItem> items = new ArrayList<>();
        for (int w = 1; w <= weeks; w++) {
            int topicIndex = Math.min((w - 1) * topics.size() / weeks, topics.size() - 1);
            String topicName = topics.get(topicIndex);

            RoadmapItem item = RoadmapItem.builder()
                    .roadmap(saved)
                    .weekNumber(w)
                    .topic("Week " + w + ": " + topicName)
                    .targetQuestions(5 + (w * 2))
                    .completed(false)
                    .build();
            items.add(roadmapItemRepository.save(item));
        }

        saved.setItems(items);
        return saved;
    }

    @Transactional
    public boolean toggleItemCompletion(Long itemId) {
        RoadmapItem item = roadmapItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap item not found"));
        item.setCompleted(!item.getCompleted());
        roadmapItemRepository.save(item);
        return item.getCompleted();
    }
}
