package in.jecrcu.placement.service.ai;

import in.jecrcu.placement.entity.Experience;
import java.util.List;
import java.util.Map;

/**
 * Institutional AI Intelligence Service interface.
 * Implements Phase 4 requirements:
 * - AI-1: Company Experience Summarization
 * - AI-2: Question Topic & Difficulty Classification
 * - AI-3: Grounded Placement Preparation Chatbot
 * - AI-4: Privacy Guardrails & PII Scrubbing
 * - AI-5: Offline Graceful Fallback
 */
public interface AiService {

    /**
     * Checks if AI features are enabled system-wide.
     */
    boolean isEnabled();

    /**
     * Checks if live external LLM API key (e.g. Gemini) is configured.
     */
    boolean isAvailable();

    /**
     * AI-1: Synthesizes approved student experiences into a structured "What to Expect" summary.
     */
    String summarizeCompanyExperience(String companyName, List<Experience> experiences);

    /**
     * AI-1: Overload for pre-aggregated raw text synthesis.
     */
    String summarizeCompanyExperience(String companyName, String aggregatedExperiences);

    /**
     * AI-2: Analyzes question text to suggest topic domain, difficulty rating, and tags.
     */
    Map<String, Object> classifyQuestion(String questionText);

    /**
     * AI-3: Answers student placement preparation queries strictly grounded in portal data.
     */
    String answerPrepQuestion(String userQuery);

    /**
     * AI-3: Overload for answering queries with explicitly supplied context.
     */
    String answerPrepQuestion(String userQuery, String relevantContext);
}
