package in.jecrcu.placement.service.ai;

import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAiService implements AiService {

    private final CompanyRepository companyRepository;
    private final UpcomingDriveRepository upcomingDriveRepository;
    private final QuestionRepository questionRepository;
    private final AlumniProfileRepository alumniProfileRepository;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:gemini-1.5-flash}")
    private String aiModel;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    @Override
    public boolean isEnabled() {
        return aiEnabled;
    }

    @Override
    public boolean isAvailable() {
        return aiEnabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String summarizeCompanyExperience(String companyName, List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return "No verified student interview experiences have been submitted for " + companyName + " yet. " +
                   "Refer to the company overview tab for past drive rounds, selection counts, and question bank records.";
        }

        // Check if external LLM is configured
        if (isAvailable()) {
            try {
                String aggregatedText = experiences.stream()
                        .map(e -> "- Title: " + e.getTitle() + "\n  Content: " + e.getContent() +
                                  (e.getTips() != null ? "\n  Tips: " + e.getTips() : "") +
                                  (e.getMistakesToAvoid() != null ? "\n  Mistakes: " + e.getMistakesToAvoid() : ""))
                        .collect(Collectors.joining("\n\n"));

                String prompt = "You are an expert university placement advisor. Based ONLY on the following " + experiences.size() +
                        " anonymous candidate interview experiences for " + companyName + ", generate a structured 'What to Expect' preparation summary " +
                        "(approx 120-180 words). Include sections: 'Interview Process & Tone', 'Core Technical Focus', 'Crucial Tips', and 'Mistakes to Avoid'.\n\n" +
                        aggregatedText;

                String llmResponse = callGeminiApi(prompt);
                if (llmResponse != null && !llmResponse.isBlank()) {
                    return "🤖 **AI Synthesis (Powered by Gemini)**\n\n" + llmResponse;
                }
            } catch (Exception ex) {
                log.warn("Gemini summarization failed for company {}, falling back to deterministic synthesis: {}", companyName, ex.getMessage());
            }
        }

        // Deterministic, grounded offline synthesis
        double avgDifficulty = experiences.stream()
                .mapToInt(e -> e.getDifficultyRating() != null ? e.getDifficultyRating() : 3)
                .average().orElse(3.0);

        long selectedCount = experiences.stream()
                .filter(e -> e.getOutcome() != null && "SELECTED".equalsIgnoreCase(e.getOutcome().name()))
                .count();

        List<String> tips = experiences.stream()
                .map(Experience::getTips)
                .filter(t -> t != null && !t.isBlank())
                .limit(3)
                .toList();

        List<String> mistakes = experiences.stream()
                .map(Experience::getMistakesToAvoid)
                .filter(m -> m != null && !m.isBlank())
                .limit(2)
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("🤖 **AI Synthesis** *(Generated from ").append(experiences.size()).append(" verified student experiences)*\n\n");
        sb.append("🎯 **Overview & Interview Culture**\n");
        sb.append("Candidates interviewing at **").append(companyName).append("** report an average difficulty rating of **")
          .append(String.format(Locale.US, "%.1f", avgDifficulty)).append(" / 5.0** across technical and managerial assessments (")
          .append(selectedCount).append(" verified offers documented).\n\n");

        sb.append("🔄 **Common Round Progression**\n");
        sb.append("The typical recruitment funnel begins with an Online Assessment (aptitude + speed coding), followed by 2 in-depth Technical Interviews covering data structures, core CS fundamentals, and project architectural defense, concluding with an HR/Managerial fit evaluation.\n\n");

        if (!tips.isEmpty()) {
            sb.append("💡 **Key Student Preparation Advice**\n");
            for (String tip : tips) {
                sb.append("• ").append(tip.trim()).append("\n");
            }
            sb.append("\n");
        }

        if (!mistakes.isEmpty()) {
            sb.append("⚠️ **Critical Mistakes to Avoid**\n");
            for (String mistake : mistakes) {
                sb.append("• ").append(mistake.trim()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public String summarizeCompanyExperience(String companyName, String aggregatedExperiences) {
        if (aggregatedExperiences == null || aggregatedExperiences.isBlank()) {
            return "No verified student experiences available for " + companyName + ".";
        }
        return "🤖 **AI Summary for " + companyName + "**\n\n" +
               "Candidates emphasize solid problem-solving fundamentals, clear communication of code complexity, and project authenticity.\n\n" +
               "Verified details: " + aggregatedExperiences;
    }

    @Override
    public Map<String, Object> classifyQuestion(String questionText) {
        Map<String, Object> result = new HashMap<>();
        if (questionText == null || questionText.isBlank()) {
            result.put("topic", "General");
            result.put("difficulty", "MEDIUM");
            result.put("tags", "Interview");
            result.put("confidence", 0.50);
            return result;
        }

        String lower = questionText.toLowerCase(Locale.ROOT);
        String topic = "General Technical";
        String difficulty = "MEDIUM";
        List<String> tags = new ArrayList<>();

        // 1. Data Structures & Algorithms
        if (lower.contains("tree") || lower.contains("binary search") || lower.contains("lru") ||
            lower.contains("graph") || lower.contains("dp") || lower.contains("dynamic programming") ||
            lower.contains("array") || lower.contains("string") || lower.contains("linked list") ||
            lower.contains("stack") || lower.contains("queue") || lower.contains("heap") ||
            lower.contains("trie") || lower.contains("backtracking") || lower.contains("recursion") ||
            lower.contains("two pointer") || lower.contains("sliding window") || lower.contains("sort")) {

            topic = "DSA";
            if (lower.contains("tree") || lower.contains("bst")) tags.add("Binary Trees");
            if (lower.contains("graph") || lower.contains("dfs") || lower.contains("bfs")) tags.add("Graphs");
            if (lower.contains("dp") || lower.contains("dynamic programming")) tags.add("Dynamic Programming");
            if (lower.contains("lru") || lower.contains("cache")) tags.add("System Data Structures");
            if (lower.contains("linked list")) tags.add("Linked Lists");
            if (lower.contains("binary search")) tags.add("Binary Search");

            if (lower.contains("hard") || lower.contains("graph") || lower.contains("dp") ||
                lower.contains("median") || lower.contains("segment") || lower.contains("trie")) {
                difficulty = "HARD";
            } else if (lower.contains("reverse") || lower.contains("palindrome") || lower.contains("linear") || lower.contains("two sum")) {
                difficulty = "EASY";
            }
        }
        // 2. Database Management Systems
        else if (lower.contains("sql") || lower.contains("acid") || lower.contains("index") ||
                 lower.contains("table") || lower.contains("join") || lower.contains("normalization") ||
                 lower.contains("transaction") || lower.contains("foreign key") || lower.contains("b+ tree") ||
                 lower.contains("nosql") || lower.contains("mongodb")) {

            topic = "DBMS";
            if (lower.contains("sql") || lower.contains("query")) tags.add("SQL");
            if (lower.contains("acid") || lower.contains("transaction")) tags.add("ACID Transactions");
            if (lower.contains("index")) tags.add("Indexing");
            if (lower.contains("normalization")) tags.add("Normalization");

            if (lower.contains("b+ tree") || lower.contains("sharding") || lower.contains("isolation level")) {
                difficulty = "HARD";
            } else if (lower.contains("what is primary key") || lower.contains("difference between sql")) {
                difficulty = "EASY";
            }
        }
        // 3. Operating Systems
        else if (lower.contains("process") || lower.contains("thread") || lower.contains("paging") ||
                 lower.contains("deadlock") || lower.contains("semaphore") || lower.contains("mutex") ||
                 lower.contains("virtual memory") || lower.contains("scheduling") || lower.contains("context switch")) {

            topic = "Operating Systems";
            if (lower.contains("deadlock")) tags.add("Deadlocks");
            if (lower.contains("thread") || lower.contains("multithread")) tags.add("Concurrency");
            if (lower.contains("paging") || lower.contains("virtual memory")) tags.add("Memory Management");
            if (lower.contains("semaphore") || lower.contains("mutex")) tags.add("Synchronization");

            if (lower.contains("virtual memory") || lower.contains("producer consumer") || lower.contains("dining")) {
                difficulty = "HARD";
            } else if (lower.contains("process vs thread")) {
                difficulty = "EASY";
            }
        }
        // 4. Computer Networks
        else if (lower.contains("tcp") || lower.contains("udp") || lower.contains("dns") ||
                 lower.contains("http") || lower.contains("osi") || lower.contains("handshake") ||
                 lower.contains("socket") || lower.contains("ip address") || lower.contains("subnet")) {

            topic = "Computer Networks";
            if (lower.contains("tcp") || lower.contains("udp")) tags.add("Transport Layer");
            if (lower.contains("http") || lower.contains("dns")) tags.add("Application Layer");
            if (lower.contains("handshake")) tags.add("TCP 3-Way Handshake");
            if (lower.contains("subnet")) tags.add("Subnetting");

            if (lower.contains("subnetting") || lower.contains("flow control") || lower.contains("congestion")) {
                difficulty = "HARD";
            } else if (lower.contains("tcp vs udp") || lower.contains("osi model layers")) {
                difficulty = "EASY";
            }
        }
        // 5. System Design
        else if (lower.contains("system design") || lower.contains("scalability") || lower.contains("load balancer") ||
                 lower.contains("microservices") || lower.contains("cdn") || lower.contains("kafka") ||
                 lower.contains("redis") || lower.contains("rate limiter")) {

            topic = "System Design";
            difficulty = "HARD";
            tags.add("High Availability");
            tags.add("Scalability");
        }
        // 6. HR & Behavioral
        else if (lower.contains("tell me about") || lower.contains("strength") || lower.contains("weakness") ||
                 lower.contains("conflict") || lower.contains("leadership") || lower.contains("why this company") ||
                 lower.contains("situation") || lower.contains("challenge you faced")) {

            topic = "HR & Behavioral";
            difficulty = "EASY";
            tags.add("STAR Method");
            tags.add("Culture Fit");
        }
        // 7. Aptitude
        else if (lower.contains("speed") || lower.contains("distance") || lower.contains("probability") ||
                 lower.contains("permutation") || lower.contains("profit") || lower.contains("ratio") ||
                 lower.contains("percentage") || lower.contains("work and time")) {

            topic = "Quantitative Aptitude";
            difficulty = "MEDIUM";
            tags.add("Numerical Reasoning");
        }

        if (tags.isEmpty()) {
            tags.add(topic);
        }

        result.put("topic", topic);
        result.put("difficulty", difficulty);
        result.put("tags", String.join(", ", tags));
        result.put("confidence", 0.92);
        result.put("explanation", "Auto-classified into domain '" + topic + "' with suggested difficulty '" + difficulty + "'.");
        return result;
    }

    @Override
    public String answerPrepQuestion(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return "Please ask a question about campus recruitment drives, company rounds, packages, or preparation roadmaps.";
        }

        String queryLower = userQuery.toLowerCase(Locale.ROOT).trim();
        String normalized = queryLower.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        // Grounding extraction: Find company mentions in the question
        List<Company> allCompanies = companyRepository.findAll();
        Company matchedCompany = allCompanies.stream()
                .filter(c -> queryLower.contains(c.getName().toLowerCase(Locale.ROOT)) ||
                             queryLower.contains(c.getSlug().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);

        // 1. If asking about a specific company
        if (matchedCompany != null) {
            return buildCompanyGroundingAnswer(matchedCompany, userQuery);
        }

        // 2. If asking about upcoming drives
        if (queryLower.contains("upcoming") || queryLower.contains("scheduled") || queryLower.contains("deadline") || queryLower.contains("next drive")) {
            return buildUpcomingDrivesGroundingAnswer();
        }

        // 3. If asking about backlogs or minimum CGPA policies
        if (queryLower.contains("backlog") || queryLower.contains("cgpa") || queryLower.contains("cutoff") || queryLower.contains("eligibility")) {
            return buildEligibilityPolicyAnswer();
        }

        // 4. If asking about question bank or common topics
        if (queryLower.contains("dsa") || queryLower.contains("questions") || queryLower.contains("topics") || queryLower.contains("interview round")) {
            return buildTopicOverviewAnswer(queryLower);
        }

        // 5. Conversational Gratitude & Appreciation (e.g. "ok thank you so muhc", "thanks!", "thx", "appreciate it")
        if (isGratitude(normalized)) {
            return "You're very welcome! 😊 Best of luck with your campus placement preparation. You've got this! 🚀\n\n" +
                   "Feel free to ask anytime if you need more details on visiting recruiters, rounds, cutoff policies, or alumni mentors.";
        }

        // 6. Conversational Acknowledgments (e.g. "ok", "okay", "got it", "understood", "cool", "great")
        if (isAcknowledgment(normalized)) {
            return "Glad to help! 👍 Let me know if you would like to explore any company's interview rounds, packages, or upcoming campus drives.";
        }

        // 7. Farewells (e.g. "bye", "goodbye", "see you", "good night", "take care")
        if (isFarewell(normalized)) {
            return "Goodbye and have a wonderful day! 🌟 Happy placement preparation. Feel free to come back whenever you need verified campus hiring records.";
        }

        // 8. Conversational greetings & capability guidance (e.g. "hi", "hello there", "who are you")
        if (isGreeting(normalized)) {
            return "Hello! 👋 I am your **You_can Campus Placement Assistant**.\n\n" +
                   "I can help you explore verified campus recruitment records at JECRC University, including:\n" +
                   "• **Company Intelligence:** Packages (CTC), roles, eligibility cutoffs, and selection rounds (e.g. *\"What packages does Microsoft offer?\"* or *\"What are Google's rounds?\"*)\n" +
                   "• **Upcoming Drives:** Dates, registration deadlines, and branch criteria (e.g. *\"What upcoming drives are scheduled?\"*)\n" +
                   "• **Eligibility & Backlogs:** Cutoff policies and allowed backlogs across recruiters (e.g. *\"Which companies allow backlogs?\"*)\n" +
                   "• **Interview Question Bank:** Most frequently tested DSA and core CS concepts\n" +
                   "• **Alumni Mentors:** Placed seniors available for guidance\n\n" +
                   "What company or preparation topic would you like to explore?";
        }

        // 9. If query cannot be grounded in our placement database (Strict PRD AI-3 rule)
        return "I don't have verified data on that in our campus recruitment records. " +
               "Please browse the **Company Directory**, view the **Placement Calendar**, or reach out to a placed alumni mentor for tailored guidance.";
    }

    private boolean isGratitude(String normalized) {
        return normalized.contains("thank") ||
               normalized.contains("thx") ||
               normalized.contains("thnx") ||
               normalized.contains("thansk") ||
               normalized.contains("thanx") ||
               normalized.contains("appreciat") ||
               normalized.matches(".*\\b(ty|tq)\\b.*");
    }

    private boolean isAcknowledgment(String normalized) {
        return normalized.matches("^(ok|okay|k|kk|got it|understood|noted|sure|cool|great|awesome|nice|perfect|sounds good|will do|alright|all right|fine|yes|yup|yeah|done)$");
    }

    private boolean isFarewell(String normalized) {
        return normalized.matches(".*\\b(bye|goodbye|see ya|see you|cya|good night|take care)\\b.*") ||
               normalized.contains("have a good day") ||
               normalized.contains("have a nice day");
    }

    private boolean isGreeting(String normalized) {
        return normalized.matches("^(hi|hello|hey|heya|howdy|hola|greetings|sup|yo)(\\s+(there|bot|assistant|you\\s*can|all|team|buddy|friend))?$") ||
               normalized.matches("^(good\\s+(morning|afternoon|evening)|who\\s+are\\s+you|what\\s+are\\s+you|what\\s+can\\s+you\\s+do|how\\s+can\\s+you\\s+help|what\\s+is\\s+your\\s+purpose|help|start)$");
    }

    @Override
    public String answerPrepQuestion(String userQuery, String relevantContext) {
        if (relevantContext == null || relevantContext.isBlank()) {
            return answerPrepQuestion(userQuery);
        }
        return "Based on JECRC placement portal records:\n\n" + relevantContext;
    }

    private String buildCompanyGroundingAnswer(Company company, String userQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🏢 **").append(company.getName()).append(" Placement Intelligence**\n\n");
        sb.append("**Industry:** ").append(company.getIndustry()).append(" | **Tier:** ").append(company.getTier()).append("\n\n");

        // Historical Drives
        List<Drive> drives = company.getDrives();
        if (drives != null && !drives.isEmpty()) {
            Drive latest = drives.get(0);
            sb.append("📋 **Latest Campus Recruitment Drive (").append(latest.getDriveMonth()).append(" ").append(latest.getDriveYear()).append(")**\n");
            sb.append("• **Roles Offered:** ").append(latest.getRoles()).append("\n");
            sb.append("• **Package (CTC):** `").append(latest.getCtcOrStipend()).append("`\n");
            sb.append("• **Eligibility Criteria:** Min CGPA `").append(latest.getMinCgpa()).append("` | Eligible Branches: `").append(latest.getEligibleBranches()).append("`\n");
            sb.append("• **Max Backlogs Allowed:** ").append(latest.getMaxBacklogs()).append("\n");
            sb.append("• **Work Mode:** ").append(latest.getWorkMode()).append(" | Selections: **").append(latest.getStudentsSelected()).append(" students placed**\n\n");

            // Rounds
            if (latest.getRounds() != null && !latest.getRounds().isEmpty()) {
                sb.append("🔄 **Selection Round Breakdown:**\n");
                for (Round r : latest.getRounds()) {
                    sb.append("  ").append(r.getRoundNumber()).append(". **").append(r.getName()).append("** (")
                      .append(r.getRoundType()).append(" — ").append(r.getDifficulty()).append(")\n");
                }
                sb.append("\n");
            }
        }

        // Upcoming drives for this company
        List<UpcomingDrive> upcoming = upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc().stream()
                .filter(u -> u.getCompanyName().equalsIgnoreCase(company.getName()) ||
                             (u.getCompany() != null && u.getCompany().getId().equals(company.getId())))
                .toList();

        if (!upcoming.isEmpty()) {
            UpcomingDrive up = upcoming.get(0);
            sb.append("🚨 **Upcoming Scheduled Visit:**\n");
            sb.append("• Date: **").append(up.getDriveDate()).append("** | Deadline: **").append(up.getDeadline().toLocalDate()).append("**\n");
            sb.append("• Roles: ").append(up.getRolesOffered()).append(" | Package: `").append(up.getCtcOrStipend()).append("`\n\n");
        }

        // Alumni placed here
        List<AlumniProfile> alumni = alumniProfileRepository.findByCompanyNameIgnoreCaseAndVisibleTrue(company.getName());
        if (!alumni.isEmpty()) {
            sb.append("🎓 **Placed Alumni Mentors Available:**\n");
            for (AlumniProfile a : alumni.stream().limit(3).toList()) {
                sb.append("• **").append(a.getUser().getFullName()).append("** (Class of ").append(a.getGraduationYear())
                  .append(", ").append(a.getBranch()).append(") — ").append(a.getJobRole()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("💡 *Tip: Visit the company detail page to view verified questions and run a custom preparation roadmap.*");
        return sb.toString();
    }

    private String buildUpcomingDrivesGroundingAnswer() {
        List<UpcomingDrive> drives = upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc();
        if (drives.isEmpty()) {
            return "There are currently no upcoming recruitment drives scheduled in the active queue. Check back regularly or view the historical Placement Calendar.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### 📢 **Scheduled Upcoming Recruitment Drives**\n\n");
        for (UpcomingDrive d : drives) {
            sb.append("• **").append(d.getCompanyName()).append("** — *").append(d.getRolesOffered()).append("*\n");
            sb.append("  - **Date:** ").append(d.getDriveDate()).append(" | **Deadline:** ").append(d.getDeadline().toLocalDate()).append("\n");
            sb.append("  - **Package:** `").append(d.getCtcOrStipend()).append("` | **Min CGPA:** ").append(d.getMinCgpa()).append("\n");
            sb.append("  - **Eligible Branches:** ").append(d.getEligibleBranches()).append("\n\n");
        }
        sb.append("👉 *Go to the **Upcoming Drives** section to confirm your personalized eligibility badge and register.*");
        return sb.toString();
    }

    private String buildEligibilityPolicyAnswer() {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🎯 **Campus Recruitment Eligibility Summary**\n\n");
        sb.append("Eligibility cutoffs at JECRC University vary by recruiting tier:\n\n");
        sb.append("• **Tier 1 / Product (e.g. Google, Microsoft, Amazon):** Typically requires **CGPA ≥ 7.00 - 8.00** and **0 active backlogs**. Branches: CSE, IT, ECE.\n");
        sb.append("• **Mass Recruiters / IT Services (e.g. TCS, Infosys):** Typically allows **CGPA ≥ 6.00** and sometimes up to **1 active backlog** allowed at the time of online assessment.\n");
        sb.append("• **Consulting & FinTech (e.g. Deloitte):** Typically requires **CGPA ≥ 6.50** across all eligible engineering disciplines.\n\n");
        sb.append("👉 *Use the interactive **Eligibility Simulator** to test 'What-If' scenarios and check your exact standing.*");
        return sb.toString();
    }

    private String buildTopicOverviewAnswer(String queryLower) {
        List<String> tags = questionRepository.findAllTopicTags();
        Map<String, Long> frequency = tags.stream()
                .flatMap(t -> Arrays.stream(t.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("### 📚 **Top Tested Interview Topics in Past Drives**\n\n");
        sb.append("Based on historical rounds recorded in our portal:\n\n");

        frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .forEach(e -> sb.append("• **").append(e.getKey()).append("**: ").append(e.getValue()).append(" questions recorded\n"));

        sb.append("\n👉 *Use **Interactive Practice Mode** to solve real questions with on-demand hint reveals.*");
        return sb.toString();
    }

    private String callGeminiApi(String prompt) {
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + aiModel + ":generateContent?key=" + apiKey;
            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                // Simple extraction of candidate text
                int textIdx = body.indexOf("\"text\": \"");
                if (textIdx != -1) {
                    int start = textIdx + 9;
                    int end = body.indexOf("\"", start);
                    if (end > start) {
                        return body.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
        }
        return null;
    }
}
