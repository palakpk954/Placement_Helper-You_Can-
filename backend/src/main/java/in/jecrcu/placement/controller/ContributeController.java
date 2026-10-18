package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Outcome;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.DriveService;
import in.jecrcu.placement.service.ExperienceService;
import in.jecrcu.placement.service.RateLimitService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContributeController {

    private final CompanyService companyService;
    private final DriveService driveService;
    private final ExperienceService experienceService;
    private final UserService userService;
    private final RateLimitService rateLimitService;

    @GetMapping("/contribute")
    public String contributeForm(
            @RequestParam(value = "companyId", required = false) Long companyId,
            Model model
    ) {
        List<Company> companies = companyService.getAllCompanies();
        model.addAttribute("companies", companies);
        model.addAttribute("preselectedCompanyId", companyId);

        List<Drive> allDrives = driveService.getAllDrives();
        model.addAttribute("drives", allDrives);

        return "contribute/form";
    }

    @PostMapping("/contribute")
    public String submitExperience(
            @RequestParam("driveId") Long driveId,
            @RequestParam(value = "anonymous", required = false, defaultValue = "false") boolean anonymous,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "tips", required = false) String tips,
            @RequestParam(value = "mistakesToAvoid", required = false) String mistakesToAvoid,
            @RequestParam("outcome") String outcomeStr,
            @RequestParam(value = "difficultyRating", required = false, defaultValue = "3") int difficultyRating,
            @RequestParam(value = "roundNames", required = false) List<String> roundNames,
            @RequestParam(value = "roundTypes", required = false) List<String> roundTypes,
            @RequestParam(value = "roundDescriptions", required = false) List<String> roundDescriptions,
            @RequestParam(value = "questionTexts", required = false) List<String> questionTexts,
            @RequestParam(value = "questionTopics", required = false) List<String> questionTopics,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User author = userService.findById(userDetails.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!rateLimitService.isSubmissionAllowed(author.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Submission rate limit reached. Please wait a while before submitting another placement experience.");
                return "redirect:/contribute";
            }

            Outcome outcome = Outcome.SELECTED;
            try {
                outcome = Outcome.valueOf(outcomeStr.toUpperCase());
            } catch (Exception ignored) {}

            experienceService.submitExperience(
                    author, driveId, anonymous, title, content, tips, mistakesToAvoid,
                    outcome, difficultyRating, roundNames, roundTypes, roundDescriptions,
                    questionTexts, questionTopics
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Thank you for paying it forward! Your placement experience has been submitted and will appear after placement cell coordinator approval.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting experience: " + e.getMessage());
            return "redirect:/contribute";
        }
    }
}
