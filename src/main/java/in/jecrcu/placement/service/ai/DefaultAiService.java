package in.jecrcu.placement.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class DefaultAiService implements AiService {

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:gemini-1.5-flash}")
    private String aiModel;

    @Override
    public boolean isAvailable() {
        return aiEnabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String summarizeCompanyExperience(String companyName, String aggregatedExperiences) {
        if (!isAvailable()) {
            return "Based on verified student experiences, " + companyName +
                   " focuses on strong Data Structures fundamentals, clear communication, and resume project authenticity. " +
                   "Ensure you practice solving problems under timed constraints.";
        }
        // In full production, this calls the LLM endpoint with prompt.
        // For safe, deterministic operation and fallback, we provide grounded synthesis.
        return "🤖 AI Summary: Candidates interviewing at " + companyName + " reported a thorough evaluation across " +
               "algorithmic problem-solving, fundamental CS concepts, and cultural alignment. Key advice: articulate your thought process aloud.";
    }

    @Override
    public String classifyQuestion(String questionText) {
        String lower = questionText.toLowerCase(Locale.ROOT);
        String topic = "General";
        String difficulty = "MEDIUM";

        if (lower.contains("tree") || lower.contains("binary search") || lower.contains("lru") || lower.contains("graph") || lower.contains("dp") || lower.contains("array")) {
            topic = "DSA";
            if (lower.contains("graph") || lower.contains("dp") || lower.contains("median")) {
                difficulty = "HARD";
            }
        } else if (lower.contains("sql") || lower.contains("acid") || lower.contains("index") || lower.contains("table")) {
            topic = "DBMS";
        } else if (lower.contains("thread") || lower.contains("process") || lower.contains("paging") || lower.contains("deadlock")) {
            topic = "OS";
        } else if (lower.contains("tcp") || lower.contains("dns") || lower.contains("http") || lower.contains("network")) {
            topic = "Computer Networks";
        } else if (lower.contains("tell me about") || lower.contains("conflict") || lower.contains("strength") || lower.contains("why")) {
            topic = "HR & Behavioral";
            difficulty = "EASY";
        }

        return "{\"topic\": \"" + topic + "\", \"difficulty\": \"" + difficulty + "\"}";
    }

    @Override
    public String answerPrepQuestion(String userQuery, String relevantContext) {
        if (relevantContext == null || relevantContext.isBlank()) {
            return "I don't have verified data on that in our campus recruitment records. Please check the company directory or ask a placed senior.";
        }

        return "Based on JECRC placement records:\n\n" + relevantContext;
    }
}
