package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.enums.Difficulty;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QuestionBankController {

    private final PracticeService practiceService;
    private final CompanyService companyService;

    @GetMapping("/questions")
    public String questionBank(
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "difficulty", required = false) String difficultyStr,
            @RequestParam(value = "search", required = false) String search,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Difficulty difficulty = null;
        if (difficultyStr != null && !difficultyStr.isBlank() && !difficultyStr.equalsIgnoreCase("ALL")) {
            try {
                difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        List<Question> questions = practiceService.searchQuestions(companyId, topic, difficulty, search);
        List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("questions", questions);
        model.addAttribute("companies", companies);
        model.addAttribute("selectedCompanyId", companyId);
        model.addAttribute("selectedTopic", topic);
        model.addAttribute("selectedDifficulty", difficultyStr);
        model.addAttribute("searchKeyword", search);

        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getId());
        }

        return "questions/index";
    }
}
