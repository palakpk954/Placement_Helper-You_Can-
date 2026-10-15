package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.PracticeAttempt;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Difficulty;
import in.jecrcu.placement.entity.enums.PracticeStatus;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.PracticeService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;
    private final CompanyService companyService;
    private final UserService userService;

    @GetMapping
    public String practiceMode(
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "difficulty", required = false) String difficultyStr,
            @RequestParam(value = "index", required = false, defaultValue = "0") int index,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Difficulty diff = null;
        if (difficultyStr != null && !difficultyStr.isBlank() && !difficultyStr.equalsIgnoreCase("ALL")) {
            try {
                diff = Difficulty.valueOf(difficultyStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        List<Question> questions = practiceService.searchQuestions(companyId, topic, diff, null);
        List<Company> companies = companyService.getAllCompanies();

        if (index < 0) index = 0;
        if (!questions.isEmpty() && index >= questions.size()) {
            index = questions.size() - 1;
        }

        Question currentQuestion = questions.isEmpty() ? null : questions.get(index);
        PracticeAttempt userAttempt = null;
        if (currentQuestion != null && userDetails != null) {
            userAttempt = practiceService.getUserAttempt(userDetails.getId(), currentQuestion.getId()).orElse(null);
        }

        var progress = (userDetails != null) ? practiceService.getStudentProgress(userDetails.getId()) : null;

        model.addAttribute("questions", questions);
        model.addAttribute("currentQuestion", currentQuestion);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("userAttempt", userAttempt);
        model.addAttribute("progress", progress);

        model.addAttribute("companies", companies);
        model.addAttribute("selectedCompanyId", companyId);
        model.addAttribute("selectedTopic", topic);
        model.addAttribute("selectedDifficulty", difficultyStr);

        return "practice/index";
    }

    @PostMapping("/attempt")
    public String recordAttempt(
            @RequestParam("questionId") Long questionId,
            @RequestParam("status") String statusStr,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "nextIndex", required = false, defaultValue = "0") int nextIndex,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails != null) {
            User user = userService.findById(userDetails.getId()).orElse(null);
            if (user != null) {
                PracticeStatus status = PracticeStatus.SOLVED;
                try {
                    status = PracticeStatus.valueOf(statusStr.toUpperCase());
                } catch (Exception ignored) {}

                practiceService.recordAttempt(user, questionId, status, notes);
                redirectAttributes.addFlashAttribute("successMessage", "Question marked as " + status.name() + "!");
            }
        }

        StringBuilder redirect = new StringBuilder("redirect:/practice?index=").append(nextIndex);
        if (companyId != null) redirect.append("&companyId=").append(companyId);
        if (topic != null) redirect.append("&topic=").append(topic);
        if (difficulty != null) redirect.append("&difficulty=").append(difficulty);

        return redirect.toString();
    }
}
