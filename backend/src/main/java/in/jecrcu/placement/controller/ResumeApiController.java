package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.service.ResumeService;
import in.jecrcu.placement.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeApiController {

    private final AiService aiService;
    private final ResumeService resumeService;

    @PostMapping("/enhance-bullet")
    public ResponseEntity<Map<String, Object>> enhanceBullet(
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String bulletText = payload != null ? payload.get("bulletText") : "";
        String domain = payload != null ? payload.get("domain") : "General Software Engineering";
        Map<String, Object> result = aiService.enhanceResumeBullet(bulletText, domain);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/match-jd")
    public ResponseEntity<Map<String, Object>> matchJobDescription(
            @RequestBody(required = false) Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String resumeText = payload != null ? payload.get("resumeText") : "";
        String jobDescription = payload != null ? payload.get("jobDescription") : "";

        // If client didn't supply resumeText, try loading user's latest uploaded resume text
        if ((resumeText == null || resumeText.isBlank()) && userDetails != null) {
            resumeText = resumeService.getLatestResumeForUser(userDetails.getId())
                    .map(resumeService::extractResumeText)
                    .orElse("");
        }

        Map<String, Object> result = aiService.matchJobDescription(resumeText, jobDescription);
        return ResponseEntity.ok(result);
    }
}
