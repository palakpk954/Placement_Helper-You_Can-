package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Experience;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.ExperienceService;
import in.jecrcu.placement.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;
    private final CompanyService companyService;
    private final ExperienceService experienceService;

    /**
     * Dedicated full-page AI Placement Preparation Assistant
     */
    @GetMapping("/ai-assistant")
    public String aiAssistantPage(Model model) {
        model.addAttribute("aiEnabled", aiService.isEnabled());
        model.addAttribute("aiAvailable", aiService.isAvailable());
        model.addAttribute("topCompanies", companyService.getAllCompanies());
        return "ai/assistant";
    }

    /**
     * REST endpoint for interactive student chat
     */
    @PostMapping("/api/ai/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chatWithAssistant(@RequestBody(required = false) Map<String, String> payload) {
        if (!aiService.isEnabled()) {
            return ResponseEntity.ok(Map.of(
                    "reply", "The institutional AI assistant is currently disabled by administrator configuration.",
                    "status", "disabled"
            ));
        }

        String userMessage = payload != null ? payload.get("message") : "";
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        String reply = aiService.answerPrepQuestion(userMessage.trim());

        return ResponseEntity.ok(Map.of(
                "reply", reply,
                "timestamp", LocalDateTime.now().toString(),
                "poweredBy", aiService.isAvailable() ? "Gemini 1.5 Flash + Grounded Records" : "Grounded Campus Recruitment Database",
                "status", "success"
        ));
    }

    /**
     * REST endpoint for question topic & difficulty auto-classification
     */
    @PostMapping("/api/ai/classify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autoClassifyQuestion(@RequestBody(required = false) Map<String, String> payload) {
        String questionText = payload != null ? payload.get("question") : "";
        Map<String, Object> classification = aiService.classifyQuestion(questionText);
        return ResponseEntity.ok(classification);
    }

    /**
     * REST endpoint to fetch company experience summary on demand
     */
    @GetMapping("/api/ai/company-summary/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getCompanyAiSummary(@PathVariable("id") Long id) {
        Company company = companyService.findById(id).orElse(null);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        List<Experience> experiences = experienceService.getApprovedExperiencesForCompany(id);
        String summary = aiService.summarizeCompanyExperience(company.getName(), experiences);

        return ResponseEntity.ok(Map.of(
                "companyName", company.getName(),
                "summary", summary
        ));
    }
}
