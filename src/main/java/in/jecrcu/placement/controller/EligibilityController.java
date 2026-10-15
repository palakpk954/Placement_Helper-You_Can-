package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.EligibilityService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;
    private final UserService userService;

    @GetMapping("/eligibility")
    public String eligibilityPage(
            @RequestParam(value = "simBranch", required = false) String simBranch,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "simCgpa", required = false) BigDecimal simCgpa,
            @RequestParam(value = "cgpa", required = false) BigDecimal cgpa,
            @RequestParam(value = "simBacklogs", required = false) Integer simBacklogs,
            @RequestParam(value = "backlogs", required = false) Integer backlogs,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;

        String rawBranch = (simBranch != null && !simBranch.isBlank()) ? simBranch : ((branch != null && !branch.isBlank()) ? branch : null);
        BigDecimal rawCgpa = (simCgpa != null) ? simCgpa : cgpa;
        Integer rawBacklogs = (simBacklogs != null) ? simBacklogs : backlogs;

        boolean isSimulated = (rawBranch != null || rawCgpa != null || rawBacklogs != null);
        String activeBranch = rawBranch != null ? rawBranch : (user != null && user.getBranch() != null ? user.getBranch() : "CSE");
        BigDecimal activeCgpa = rawCgpa != null ? rawCgpa : (user != null && user.getCgpa() != null ? user.getCgpa() : BigDecimal.valueOf(7.50));
        Integer activeBacklogs = rawBacklogs != null ? rawBacklogs : (user != null && user.getActiveBacklogs() != null ? user.getActiveBacklogs() : 0);

        var upcomingResults = eligibilityService.evaluateAllUpcoming(activeBranch, activeCgpa, activeBacklogs);
        var pastResults = eligibilityService.evaluateAllPast(activeBranch, activeCgpa, activeBacklogs);

        model.addAttribute("currentUser", user);
        model.addAttribute("branch", activeBranch);
        model.addAttribute("cgpa", activeCgpa);
        model.addAttribute("backlogs", activeBacklogs);
        model.addAttribute("isSimulated", isSimulated);

        model.addAttribute("upcomingResults", upcomingResults);
        model.addAttribute("pastResults", pastResults);

        return "eligibility/index";
    }
}
