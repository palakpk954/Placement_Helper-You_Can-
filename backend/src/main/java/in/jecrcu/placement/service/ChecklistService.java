package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.Round;
import in.jecrcu.placement.entity.enums.RoundType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final CompanyService companyService;

    @Getter
    public static class ChecklistSection {
        private final String title;
        private final String icon;
        private final List<ChecklistItem> items;

        public ChecklistSection(String title, String icon, List<ChecklistItem> items) {
            this.title = title;
            this.icon = icon;
            this.items = items;
        }
    }

    @Getter
    public static class ChecklistItem {
        private final String text;
        private final String priority; // High, Medium, Low
        private final String category;

        public ChecklistItem(String text, String priority, String category) {
            this.text = text;
            this.priority = priority;
            this.category = category;
        }
    }

    public List<ChecklistSection> generateChecklist(Long companyId) {
        Company company = companyService.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        List<ChecklistSection> sections = new ArrayList<>();
        List<Drive> drives = company.getDrives();

        Set<RoundType> roundTypesFound = new HashSet<>();
        Set<String> topicsFound = new HashSet<>();

        if (drives != null) {
            for (Drive d : drives) {
                if (d.getRounds() != null) {
                    for (Round r : d.getRounds()) {
                        roundTypesFound.add(r.getRoundType());
                        if (r.getQuestions() != null) {
                            for (Question q : r.getQuestions()) {
                                if (q.getTopicTags() != null) {
                                    for (String t : q.getTopicTags().split(",")) {
                                        topicsFound.add(t.trim().toLowerCase());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Online Assessment / Aptitude
        List<ChecklistItem> oaItems = new ArrayList<>();
        if (roundTypesFound.contains(RoundType.APTITUDE) || roundTypesFound.contains(RoundType.ONLINE_ASSESSMENT)) {
            oaItems.add(new ChecklistItem("Quantitative Aptitude (Speed Math, Percentages, P&C, Probability)", "High", "Aptitude"));
            oaItems.add(new ChecklistItem("Logical Reasoning & Data Interpretation", "Medium", "Aptitude"));
            oaItems.add(new ChecklistItem("Timed pseudo-code & output tracing questions", "High", "Coding OA"));
            oaItems.add(new ChecklistItem("Practice 2 timed Medium problems on array manipulation and string parsing", "High", "Coding OA"));
        } else {
            oaItems.add(new ChecklistItem("Complete company's coding assessment platform orientation", "High", "Coding OA"));
            oaItems.add(new ChecklistItem("Practice standard LeetCode Medium problems with 45-min timer", "High", "Coding OA"));
        }
        sections.add(new ChecklistSection("Round 1: Screening & Online Assessment", "bi-laptop", oaItems));

        // Section 2: Data Structures & Algorithms
        List<ChecklistItem> dsaItems = new ArrayList<>();
        dsaItems.add(new ChecklistItem("Two Pointers & Sliding Window patterns (O(N) time complexities)", "High", "DSA"));
        dsaItems.add(new ChecklistItem("Binary Search on sorted and rotated arrays", "High", "DSA"));
        dsaItems.add(new ChecklistItem("Binary Tree Traversals (Inorder, Preorder, Level Order, LCA)", "High", "DSA"));
        if (topicsFound.stream().anyMatch(t -> t.contains("graph"))) {
            dsaItems.add(new ChecklistItem("Graph BFS/DFS, Cycle Detection, and Shortest Paths (Dijkstra/Bellman-Ford)", "High", "Graphs"));
        }
        if (topicsFound.stream().anyMatch(t -> t.contains("dynamic programming") || t.contains("dp"))) {
            dsaItems.add(new ChecklistItem("Dynamic Programming (0/1 Knapsack, Coin Change, LCS, Longest Palindrome)", "High", "DP"));
        }
        sections.add(new ChecklistSection("Technical Core: Data Structures & Algorithms", "bi-code-slash", dsaItems));

        // Section 3: Core Computer Science Fundamentals
        List<ChecklistItem> csItems = new ArrayList<>();
        csItems.add(new ChecklistItem("DBMS: Normalization (1NF to BCNF) and Indexing mechanics (B-Tree vs Hash)", "High", "DBMS"));
        csItems.add(new ChecklistItem("SQL: Write complex queries using GROUP BY, HAVING, and Window Functions (DENSE_RANK)", "High", "SQL"));
        csItems.add(new ChecklistItem("OS: Process vs Thread, CPU Scheduling, Virtual Memory, and Paging", "High", "OS"));
        csItems.add(new ChecklistItem("Networks: TCP 3-way handshake, DNS flow, and HTTP/HTTPS differences", "Medium", "Computer Networks"));
        csItems.add(new ChecklistItem("OOP: SOLID Principles and Design Patterns (Singleton, Factory, Strategy)", "High", "OOP"));
        sections.add(new ChecklistSection("Technical Core: CS Fundamentals & OOP", "bi-cpu", csItems));

        // Section 4: Projects & System Design
        List<ChecklistItem> projItems = new ArrayList<>();
        projItems.add(new ChecklistItem("Prepare 2-minute executive elevator pitch for your primary capstone project", "High", "Projects"));
        projItems.add(new ChecklistItem("Be ready to draw full architectural diagram (Client -> API Gateway -> Backend -> DB -> Cache)", "High", "Architecture"));
        projItems.add(new ChecklistItem("Identify key trade-offs and bottleneck decisions made in your implementation", "High", "Projects"));
        if (roundTypesFound.contains(RoundType.CASE_STUDY)) {
            projItems.add(new ChecklistItem("System Design: Review TinyURL, Rate Limiter, and Caching strategies", "High", "System Design"));
        }
        sections.add(new ChecklistSection("Projects & System Architecture", "bi-diagram-3", projItems));

        // Section 5: Behavioral & HR Fitment
        List<ChecklistItem> hrItems = new ArrayList<>();
        hrItems.add(new ChecklistItem("Prepare STAR story: 'Tell me about yourself and your journey into software engineering'", "High", "HR"));
        hrItems.add(new ChecklistItem("Prepare STAR story: 'Describe a significant technical obstacle you overcame'", "High", "HR"));
        hrItems.add(new ChecklistItem("Prepare STAR story: 'Tell me about a disagreement with a teammate and how you resolved it'", "High", "HR"));
        hrItems.add(new ChecklistItem("Research " + company.getName() + "'s core business values, culture, and recent news", "High", "Company Knowledge"));
        hrItems.add(new ChecklistItem("Formulate 3 thoughtful questions to ask the interviewer at the end", "Medium", "HR"));
        sections.add(new ChecklistSection("HR, Behavioral & Cultural Fitment", "bi-person-check", hrItems));

        return sections;
    }
}
