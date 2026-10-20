package in.jecrcu.placement.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAiService implements AiService {

    private final CompanyRepository companyRepository;
    private final UpcomingDriveRepository upcomingDriveRepository;
    private final QuestionRepository questionRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final DriveRepository driveRepository;
    private final ExperienceRepository experienceRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:gemini-1.5-flash}")
    private String aiModel;

    @Value("${app.ai.temperature:0.2}")
    private double aiTemperature;

    @Value("${app.ai.max-tokens:1000}")
    private int aiMaxTokens;

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
    public String getModelName() {
        return aiModel != null && !aiModel.isBlank() ? aiModel : "gemini-1.5-flash";
    }

    @Override
    public String getProviderName() {
        return isAvailable() ? "Google Gemini (" + getModelName() + " Cloud)" : "Grounded Institutional Engine (Offline)";
    }

    @Override
    public String summarizeCompanyExperience(String companyName, List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return "No verified student interview experiences have been submitted for " + companyName + " yet. " +
                   "Refer to the company overview tab for past drive rounds, selection counts, and question bank records.";
        }

        // Live Cloud LLM synthesis (Gemini) when available
        if (isAvailable()) {
            try {
                // Privacy Guardrail AI-4: Strip student PII (author names, emails)
                String aggregatedText = experiences.stream()
                        .map(e -> "- Title: " + e.getTitle() + "\n  Content: " + e.getContent() +
                                  (e.getTips() != null ? "\n  Tips: " + e.getTips() : "") +
                                  (e.getMistakesToAvoid() != null ? "\n  Mistakes: " + e.getMistakesToAvoid() : ""))
                        .collect(Collectors.joining("\n\n"));

                String prompt = "You are an expert university placement advisor at JECRC University. Based ONLY on the following " +
                        experiences.size() + " anonymous candidate interview experiences for " + companyName + ", generate a structured " +
                        "'What to Expect' preparation summary (approx 120-180 words). Include sections: 'Interview Process & Tone', " +
                        "'Core Technical Focus', 'Crucial Tips', and 'Mistakes to Avoid'. Maintain an encouraging, professional tone.\n\n" +
                        aggregatedText;

                String llmResponse = callGeminiApi(prompt);
                if (llmResponse != null && !llmResponse.isBlank()) {
                    return "🤖 **AI Synthesis (Powered by Gemini)**\n\n" + llmResponse;
                }
            } catch (Exception ex) {
                log.warn("Gemini summarization failed for company {}, falling back to deterministic synthesis: {}", companyName, ex.getMessage());
            }
        }

        // Deterministic, grounded offline synthesis fallback (AI-5)
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

        // Live Cloud LLM classification if available
        if (isAvailable()) {
            try {
                String prompt = "You are a placement technical question classifier. Classify the following technical interview question:\n\n" +
                        "\"" + questionText + "\"\n\n" +
                        "Classify into:\n" +
                        "- topic: choose exactly one from [DSA, DBMS, Operating Systems, Computer Networks, System Design, HR & Behavioral, Quantitative Aptitude, General Technical]\n" +
                        "- difficulty: choose exactly one from [EASY, MEDIUM, HARD]\n" +
                        "- tags: comma-separated list of 1-3 specific concept tags\n" +
                        "- confidence: decimal between 0.80 and 0.99\n" +
                        "- explanation: single sentence rationale\n\n" +
                        "Return ONLY valid JSON matching this structure without any markdown formatting:\n" +
                        "{\"topic\": \"...\", \"difficulty\": \"...\", \"tags\": \"...\", \"confidence\": 0.95, \"explanation\": \"...\"}";

                String jsonResponse = callGeminiApi(prompt);
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    String cleanJson = jsonResponse.trim();
                    if (cleanJson.startsWith("```json")) {
                        cleanJson = cleanJson.substring(7);
                    }
                    if (cleanJson.startsWith("```")) {
                        cleanJson = cleanJson.substring(3);
                    }
                    if (cleanJson.endsWith("```")) {
                        cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
                    }
                    cleanJson = cleanJson.trim();

                    JsonNode node = objectMapper.readTree(cleanJson);
                    if (node.has("topic") && node.has("difficulty")) {
                        result.put("topic", node.path("topic").asText("General Technical"));
                        result.put("difficulty", node.path("difficulty").asText("MEDIUM"));
                        result.put("tags", node.path("tags").asText("Interview"));
                        result.put("confidence", node.path("confidence").asDouble(0.92));
                        result.put("explanation", node.path("explanation").asText("Classified via Gemini 1.5 Flash."));
                        return result;
                    }
                }
            } catch (Exception ex) {
                log.warn("Gemini question classification failed, using deterministic classifier: {}", ex.getMessage());
            }
        }

        // Deterministic keyword classifier fallback
        return runDeterministicClassification(questionText);
    }

    private Map<String, Object> runDeterministicClassification(String questionText) {
        Map<String, Object> result = new HashMap<>();
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

        // 1. Conversational Gratitude & Appreciation (instant response, zero external latency)
        if (isGratitude(normalized)) {
            return "You're very welcome! 😊 Best of luck with your campus placement preparation. You've got this! 🚀\n\n" +
                   "Feel free to ask anytime if you need more details on visiting recruiters, rounds, cutoff policies, or alumni mentors.";
        }

        // 2. Conversational Acknowledgments
        if (isAcknowledgment(normalized)) {
            return "Glad to help! 👍 Let me know if you would like to explore any company's interview rounds, packages, or upcoming campus drives.";
        }

        // 3. Farewells
        if (isFarewell(normalized)) {
            return "Goodbye and have a wonderful day! 🌟 Happy placement preparation. Feel free to come back whenever you need verified campus hiring records.";
        }

        // 4. Conversational greetings & capability guidance
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

        // Grounding extraction: Match company mentions in query
        List<Company> allCompanies = companyRepository.findAll();
        Company matchedCompany = allCompanies.stream()
                .filter(c -> queryLower.contains(c.getName().toLowerCase(Locale.ROOT)) ||
                             queryLower.contains(c.getSlug().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);

        // 5. If Cloud Gemini LLM is enabled & configured, execute Grounded RAG Pipeline (AI-1, AI-3)
        if (isAvailable()) {
            try {
                String ragAnswer = answerWithGemini(userQuery, matchedCompany);
                if (ragAnswer != null && !ragAnswer.isBlank()) {
                    return ragAnswer;
                }
            } catch (Exception ex) {
                log.warn("Gemini RAG answering failed, falling back to deterministic grounded engine: {}", ex.getMessage());
            }
        }

        // 6. Deterministic Grounded Fallback (AI-5)
        if (matchedCompany != null) {
            return buildCompanyGroundingAnswer(matchedCompany, userQuery);
        }

        if (queryLower.contains("upcoming") || queryLower.contains("scheduled") || queryLower.contains("deadline") || queryLower.contains("next drive")) {
            return buildUpcomingDrivesGroundingAnswer();
        }

        if (queryLower.contains("backlog") || queryLower.contains("cgpa") || queryLower.contains("cutoff") || queryLower.contains("eligibility")) {
            return buildEligibilityPolicyAnswer();
        }

        if (queryLower.contains("dsa") || queryLower.contains("questions") || queryLower.contains("topics") || queryLower.contains("interview round")) {
            return buildTopicOverviewAnswer(queryLower);
        }

        // 7. Strict Grounding Constraint (AI-3): Non-hallucination guardrail for ungrounded queries
        return "I don't have verified data on that in our campus recruitment records. " +
               "Please browse the **Company Directory**, view the **Placement Calendar**, or reach out to a placed alumni mentor for tailored guidance.";
    }

    @Override
    public String answerPrepQuestion(String userQuery, String relevantContext) {
        if (relevantContext == null || relevantContext.isBlank()) {
            return answerPrepQuestion(userQuery);
        }

        if (isAvailable()) {
            try {
                String prompt = buildSystemPrompt() + "\n\n" +
                        "### PROVIDED INSTITUTIONAL CONTEXT:\n" + relevantContext + "\n\n" +
                        "### STUDENT QUERY:\n" + userQuery + "\n\n" +
                        "Provide a helpful, grounded response based strictly on the context above:";

                String llmReply = callGeminiApi(prompt);
                if (llmReply != null && !llmReply.isBlank()) {
                    return llmReply;
                }
            } catch (Exception ex) {
                log.warn("Gemini query with custom context failed, using offline fallback: {}", ex.getMessage());
            }
        }

        return "Based on JECRC placement portal records:\n\n" + relevantContext;
    }

    /**
     * Executes RAG Retrieval + Strict Grounding Prompting with Gemini Cloud API
     */
    private String answerWithGemini(String userQuery, Company matchedCompany) {
        String institutionalContext = buildRagContext(matchedCompany);

        String prompt = buildSystemPrompt() + "\n\n" +
                "### VERIFIED INSTITUTIONAL RECORDS (CAMPUS DATABASE):\n" +
                institutionalContext + "\n\n" +
                "### STUDENT INQUIRY:\n" +
                "\"" + userQuery + "\"\n\n" +
                "Answer the student inquiry concisely and professionally. Follow all institutional grounding rules strictly.";

        return callGeminiApi(prompt);
    }

    private String buildSystemPrompt() {
        return "You are the official AI Placement Preparation Advisor for JECRC University (\"You_can\" Placement Portal).\n" +
               "Your mission is to provide accurate, empowering, and strictly grounded guidance to engineering students preparing for campus drives.\n\n" +
               "STRICT INSTITUTIONAL GROUNDING RULES:\n" +
               "1. Base all facts, packages (CTC), hiring rounds, eligibility criteria, and deadlines STRICTLY on the institutional records provided.\n" +
               "2. DO NOT fabricate or hallucinate compensation figures, interview rounds, or cutoff percentages not present in the verified records.\n" +
               "3. If the user asks about a recruiter or policy that does not exist in the records, politely state that our verified campus database currently does not have documentation for it, and recommend visiting the Company Directory or Placement Office.\n" +
               "4. Privacy by Design: Never disclose or request student personal identifiable information (emails, phone numbers, roll numbers).\n" +
               "5. Output formatting: Use crisp, well-structured GitHub-flavored Markdown with bold headings, bullet points, and code tags for CTC/CGPA.";
    }

    /**
     * Assembles clean, PII-scrubbed institutional context for RAG retrieval
     */
    private String buildRagContext(Company matchedCompany) {
        StringBuilder sb = new StringBuilder();

        if (matchedCompany != null) {
            sb.append("COMPANY RECORD: ").append(matchedCompany.getName())
              .append(" (Tier: ").append(matchedCompany.getTier())
              .append(", Industry: ").append(matchedCompany.getIndustry()).append(")\n");

            // Drives & Packages
            List<Drive> drives = matchedCompany.getDrives();
            if (drives == null || drives.isEmpty()) {
                drives = driveRepository.findByCompanyOrderByDriveYearDesc(matchedCompany);
            }
            if (drives != null && !drives.isEmpty()) {
                Drive latest = drives.get(0);
                sb.append("LATEST DRIVE: Year ").append(latest.getDriveYear()).append(" ").append(latest.getDriveMonth())
                  .append(", Roles: ").append(latest.getRoles())
                  .append(", Package (CTC): ").append(latest.getCtcOrStipend())
                  .append(", Min CGPA: ").append(latest.getMinCgpa())
                  .append(", Eligible Branches: ").append(latest.getEligibleBranches())
                  .append(", Max Backlogs Allowed: ").append(latest.getMaxBacklogs())
                  .append(", Selections: ").append(latest.getStudentsSelected()).append(" placed students.\n");

                if (latest.getRounds() != null && !latest.getRounds().isEmpty()) {
                    sb.append("SELECTION ROUNDS:\n");
                    for (Round r : latest.getRounds()) {
                        sb.append("  - Round ").append(r.getRoundNumber()).append(": ").append(r.getName())
                          .append(" (Type: ").append(r.getRoundType()).append(", Difficulty: ").append(r.getDifficulty()).append(")\n");
                    }
                }
            }

            // Upcoming visit
            List<UpcomingDrive> upcoming = upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc().stream()
                    .filter(u -> u.getCompanyName().equalsIgnoreCase(matchedCompany.getName()) ||
                                 (u.getCompany() != null && u.getCompany().getId().equals(matchedCompany.getId())))
                    .toList();
            if (!upcoming.isEmpty()) {
                UpcomingDrive up = upcoming.get(0);
                sb.append("UPCOMING SCHEDULED VISIT: Date: ").append(up.getDriveDate())
                  .append(", Registration Deadline: ").append(up.getDeadline().toLocalDate())
                  .append(", Roles: ").append(up.getRolesOffered())
                  .append(", CTC: ").append(up.getCtcOrStipend()).append("\n");
            }

            // Placed Alumni Mentors
            List<AlumniProfile> alumni = alumniProfileRepository.findByCompanyNameIgnoreCaseAndVisibleTrue(matchedCompany.getName());
            if (!alumni.isEmpty()) {
                sb.append("ALUMNI MENTORS (Placed Seniors):\n");
                for (AlumniProfile a : alumni.stream().limit(3).toList()) {
                    sb.append("  - ").append(a.getUser().getFullName()).append(" (Batch of ").append(a.getGraduationYear())
                      .append(", ").append(a.getBranch()).append(") Role: ").append(a.getJobRole()).append("\n");
                }
            }

            // Student interview tips (PII Scrubbed)
            List<Experience> approvedExps = experienceRepository.findApprovedByCompanyId(matchedCompany.getId());
            if (approvedExps != null && !approvedExps.isEmpty()) {
                sb.append("STUDENT INTERVIEW TIPS & EXPERIENCES (").append(approvedExps.size()).append(" verified):\n");
                for (Experience exp : approvedExps.stream().limit(2).toList()) {
                    if (exp.getTips() != null && !exp.getTips().isBlank()) {
                        sb.append("  - Tip: ").append(exp.getTips()).append("\n");
                    }
                    if (exp.getMistakesToAvoid() != null && !exp.getMistakesToAvoid().isBlank()) {
                        sb.append("  - Avoid: ").append(exp.getMistakesToAvoid()).append("\n");
                    }
                }
            }
        } else {
            // General institutional overview
            List<UpcomingDrive> upcoming = upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc();
            sb.append("UPCOMING SCHEDULED DRIVES AT JECRC:\n");
            for (UpcomingDrive d : upcoming.stream().limit(4).toList()) {
                sb.append("  - ").append(d.getCompanyName()).append(": ").append(d.getRolesOffered())
                  .append(", CTC: ").append(d.getCtcOrStipend()).append(", Min CGPA: ").append(d.getMinCgpa())
                  .append(", Date: ").append(d.getDriveDate()).append(", Deadline: ").append(d.getDeadline().toLocalDate()).append("\n");
            }

            sb.append("GENERAL ELIGIBILITY GUIDELINES:\n")
              .append("  - Tier 1 Super Dream (Google, Microsoft): CGPA >= 7.5 - 8.0, 0 active backlogs.\n")
              .append("  - Tier 2 / Mass Recruiters (TCS, Infosys): CGPA >= 6.0, up to 1 active backlog often permitted.\n")
              .append("  - FinTech & Consulting (Deloitte): CGPA >= 6.5 across CSE, IT, ECE, EE.\n");
        }

        return sb.toString();
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

    private String buildCompanyGroundingAnswer(Company company, String userQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🏢 **").append(company.getName()).append(" Placement Intelligence**\n\n");
        sb.append("**Industry:** ").append(company.getIndustry()).append(" | **Tier:** ").append(company.getTier()).append("\n\n");

        // Historical Drives
        List<Drive> drives = company.getDrives();
        if (drives == null || drives.isEmpty()) {
            drives = driveRepository.findByCompanyOrderByDriveYearDesc(company);
        }
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
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + getModelName() + ":generateContent?key=" + apiKey;

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> contentObj = Map.of("parts", List.of(textPart));
            Map<String, Object> genConfig = Map.of(
                    "temperature", aiTemperature,
                    "maxOutputTokens", aiMaxTokens
            );
            Map<String, Object> payload = Map.of(
                    "contents", List.of(contentObj),
                    "generationConfig", genConfig
            );

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        String text = parts.get(0).path("text").asText();
                        if (text != null && !text.isBlank()) {
                            return text.trim();
                        }
                    }
                }
            } else {
                log.warn("Gemini API returned HTTP status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Object> enhanceResumeBullet(String bulletText, String domain) {
        if (bulletText == null || bulletText.isBlank()) {
            return Map.of(
                    "weakness", "Please provide a resume bullet point to analyze.",
                    "originalScore", 0,
                    "enhancedScore", 0,
                    "metricDriven", "",
                    "technicalDepth", "",
                    "conciseOneLiner", "",
                    "tip", "Paste an experience or project description to generate optimized rewrites."
            );
        }

        String safeDomain = (domain != null && !domain.isBlank()) ? domain : "General Software Engineering";
        String cleanInput = bulletText.trim();

        // 1. Live Cloud LLM via Gemini if available
        if (isAvailable()) {
            try {
                String prompt = String.format(
                        "You are an expert technical resume coach and ATS optimization specialist for university computer science graduates.\n" +
                        "Analyze and improve the following single resume bullet point for a candidate targeting a %s role.\n\n" +
                        "Input bullet point: \"%s\"\n\n" +
                        "Respond ONLY in valid JSON with these exact keys:\n" +
                        "{\n" +
                        "  \"weakness\": \"1-sentence diagnosis of why the original bullet is weak (e.g. passive tone, lacks quantifiable impact)\",\n" +
                        "  \"originalScore\": 45,\n" +
                        "  \"enhancedScore\": 94,\n" +
                        "  \"metricDriven\": \"A high-impact STAR rewrite emphasizing quantifiable metrics, scale, or business impact.\",\n" +
                        "  \"technicalDepth\": \"A rewrite emphasizing architecture, design patterns, robust error handling, and modern technical stack.\",\n" +
                        "  \"conciseOneLiner\": \"A punchy, clean single-line bullet point starting with a powerful action verb.\",\n" +
                        "  \"tip\": \"1 actionable interview tip related to this point.\"\n" +
                        "}\nDo not wrap in backticks or markdown.",
                        safeDomain, cleanInput.replace("\"", "'")
                );

                String llmResponse = callGeminiApi(prompt);
                if (llmResponse != null && !llmResponse.isBlank()) {
                    String cleanJson = llmResponse.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)```$", "").trim();
                    JsonNode node = objectMapper.readTree(cleanJson);
                    if (node.has("metricDriven")) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("weakness", node.path("weakness").asText("Lacks quantifiable metrics and strong active verbs."));
                        result.put("originalScore", node.path("originalScore").asInt(48));
                        result.put("enhancedScore", node.path("enhancedScore").asInt(94));
                        result.put("metricDriven", node.path("metricDriven").asText());
                        result.put("technicalDepth", node.path("technicalDepth").asText());
                        result.put("conciseOneLiner", node.path("conciseOneLiner").asText());
                        result.put("tip", node.path("tip").asText("Prepare to explain the technical tradeoffs you made during implementation."));
                        result.put("provider", "Google Gemini");
                        return result;
                    }
                }
            } catch (Exception ex) {
                log.warn("Gemini resume bullet enhancement failed, using offline synthesis: {}", ex.getMessage());
            }
        }

        // 2. Deterministic Grounded Engine (Offline Fallback)
        return generateDeterministicBulletEnhancement(cleanInput, safeDomain);
    }

    private Map<String, Object> generateDeterministicBulletEnhancement(String input, String domain) {
        String lower = input.toLowerCase();
        List<String> detectedWeaknesses = new ArrayList<>();
        int score = 45;

        // Weak verb checks
        if (lower.startsWith("worked on") || lower.startsWith("helped") || lower.startsWith("did") ||
            lower.startsWith("created") || lower.startsWith("made") || lower.startsWith("responsible for") ||
            lower.startsWith("handled") || lower.startsWith("i worked") || lower.startsWith("i made")) {
            detectedWeaknesses.add("Starts with passive/generic phrasing instead of an industry-standard power verb");
        } else {
            score += 10;
        }

        // Metrics check
        boolean hasMetrics = input.matches(".*\\d+.*") || input.contains("%") || lower.contains("x faster");
        if (!hasMetrics) {
            detectedWeaknesses.add("Missing measurable business impact or performance metrics (e.g. %, latency, user volume)");
        } else {
            score += 15;
        }

        if (detectedWeaknesses.isEmpty()) {
            detectedWeaknesses.add("Good baseline; can be elevated with clearer architectural scope and technical depth");
        }

        String weakness = String.join("; ", detectedWeaknesses) + ".";

        // Clean input for synthesis
        String subject = input.replaceAll("^(?i)(i |we |worked on |helped to |responsible for |created |made )+", "").trim();
        if (subject.endsWith(".")) subject = subject.substring(0, subject.length() - 1);

        String metricDriven;
        String technicalDepth;
        String concise;

        if (domain.toLowerCase().contains("web") || domain.toLowerCase().contains("full")) {
            metricDriven = "Architected responsive " + subject + ", integrating RESTful APIs and optimizing component state rendering to improve page load speed by 35% across 2,000+ monthly active users.";
            technicalDepth = "Engineered scalable " + subject + " utilizing modular component architecture, robust JWT-based session security, and automated unit test suites achieving 85%+ code coverage.";
            concise = "Developed full-stack " + subject + " with responsive UI, cross-browser compatibility, and sub-second API response times.";
        } else if (domain.toLowerCase().contains("data") || domain.toLowerCase().contains("ai")) {
            metricDriven = "Spearheaded " + subject + ", engineering automated data preprocessing pipelines that processed 50,000+ records and improved predictive model accuracy to 91.4%.";
            technicalDepth = "Developed high-throughput data processing architecture for " + subject + " using vectorized NumPy/Pandas operations and cross-validated statistical benchmarks.";
            concise = "Implemented end-to-end " + subject + " pipeline with automated feature extraction and reproducible ML workflows.";
        } else if (domain.toLowerCase().contains("cloud") || domain.toLowerCase().contains("devops")) {
            metricDriven = "Orchestrated CI/CD container deployment for " + subject + " with Docker, reducing manual release cycle time by 60% and achieving 99.9% service uptime.";
            technicalDepth = "Engineered zero-downtime infrastructure for " + subject + " featuring health monitoring, automated rollback triggers, and secure secret management.";
            concise = "Automated continuous delivery and deployment pipeline for " + subject + " with containerized microservices.";
        } else {
            metricDriven = "Spearheaded development of " + subject + ", optimizing core algorithms and database queries to reduce execution latency by 42% under concurrent load.";
            technicalDepth = "Engineered robust " + subject + " adhering to SOLID object-oriented design principles, thread-safe concurrency patterns, and comprehensive edge-case handling.";
            concise = "Implemented high-performance " + subject + " featuring clean separation of concerns and reliable end-to-end testing.";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weakness", weakness);
        result.put("originalScore", Math.min(score, 65));
        result.put("enhancedScore", 94);
        result.put("metricDriven", metricDriven);
        result.put("technicalDepth", technicalDepth);
        result.put("conciseOneLiner", concise);
        result.put("tip", "In interviews, frame this using the STAR method: Situation, Task, Action you specifically took, and the Resulting Metric.");
        result.put("provider", "Institutional ATS Optimizer");
        return result;
    }

    @Override
    public Map<String, Object> matchJobDescription(String resumeText, String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) {
            return Map.of(
                    "matchScore", 0,
                    "verdict", "No Job Description Provided",
                    "matchedKeywords", List.of(),
                    "missingKeywords", List.of(),
                    "recommendations", List.of("Paste a job description or select an upcoming campus drive to calculate your keyword alignment.")
            );
        }

        String safeResume = (resumeText != null) ? resumeText : "";

        // Standardized Industry Skill Catalog
        List<String> dictionary = List.of(
                "Java", "Python", "C++", "C#", "JavaScript", "TypeScript", "SQL", "Go", "Kotlin", "Swift", "HTML", "CSS",
                "Spring Boot", "Spring", "React", "Angular", "Vue", "Node.js", "Express", "Django", "Flask", "FastAPI",
                "Hibernate", "JPA", "Next.js", "Tailwind", "Bootstrap", "Redux", "JUnit", "Mockito", "PyTorch", "TensorFlow", "Pandas", "NumPy",
                "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "DynamoDB", "Cassandra", "Elasticsearch",
                "AWS", "Azure", "GCP", "Docker", "Kubernetes", "CI/CD", "Git", "GitHub", "Linux", "Jenkins", "Terraform", "Kafka", "RabbitMQ", "Microservices", "REST API", "GraphQL",
                "Data Structures", "Algorithms", "OOP", "System Design", "Agile", "Scrum", "Operating Systems", "Computer Networks", "DBMS", "Multithreading"
        );

        Set<String> jdKeywords = new LinkedHashSet<>();
        String jdLower = " " + jobDescription.toLowerCase().replaceAll("[^a-z0-9+#.]", " ") + " ";

        for (String skill : dictionary) {
            String skillLower = " " + skill.toLowerCase() + " ";
            if (jdLower.contains(skillLower) || jdLower.contains(" " + skill.toLowerCase() + "s ")) {
                jdKeywords.add(skill);
            }
        }

        // If the JD had few standard dictionary terms, extract capitalized / technical words
        if (jdKeywords.size() < 3) {
            Pattern p = Pattern.compile("\\b([A-Z][a-zA-Z0-9+#]{2,15})\\b");
            var m = p.matcher(jobDescription);
            while (m.find()) {
                String candidate = m.group(1);
                if (!List.of("The", "You", "Our", "And", "For", "With", "Will", "Candidate", "Role", "Required", "Must", "Have", "Join", "Team").contains(candidate)) {
                    jdKeywords.add(candidate);
                }
            }
        }

        Set<String> matched = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();
        String resumeLower = " " + safeResume.toLowerCase().replaceAll("[^a-z0-9+#.]", " ") + " ";

        for (String kw : jdKeywords) {
            String kwLower = " " + kw.toLowerCase() + " ";
            if (resumeLower.contains(kwLower) || resumeLower.contains(" " + kw.toLowerCase() + "s ")) {
                matched.add(kw);
            } else {
                missing.add(kw);
            }
        }

        int totalJd = jdKeywords.size();
        int matchScore;
        if (totalJd == 0) {
            matchScore = 70;
        } else {
            matchScore = (int) Math.round(((double) matched.size() / totalJd) * 100);
        }

        String verdict;
        if (matchScore >= 80) {
            verdict = "Strong ATS Match (High probability of passing recruiter filters)";
        } else if (matchScore >= 55) {
            verdict = "Moderate Match (Competitive, but key required skills are absent)";
        } else {
            verdict = "Low ATS Match (High risk of automated screening rejection)";
        }

        List<String> recommendations = new ArrayList<>();
        if (!missing.isEmpty()) {
            List<String> topMissing = missing.stream().limit(3).toList();
            recommendations.add("Incorporate missing keywords like " + String.join(", ", topMissing) + " into your technical skills or project descriptions.");
        }
        if (safeResume.length() < 300) {
            recommendations.add("Your uploaded resume content appears short. Ensure projects, coursework, and technical tools are fully documented.");
        }
        recommendations.add("Align project bullet points to mirror the action verbs used in this specific job description.");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matchScore", matchScore);
        result.put("verdict", verdict);
        result.put("matchedKeywords", new ArrayList<>(matched));
        result.put("missingKeywords", new ArrayList<>(missing));
        result.put("totalJdKeywords", totalJd);
        result.put("recommendations", recommendations);
        return result;
    }
}

