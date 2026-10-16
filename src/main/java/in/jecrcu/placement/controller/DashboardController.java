package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final EligibilityService eligibilityService;
    private final PracticeService practiceService;
    private final BookmarkService bookmarkService;
    private final NotificationService notificationService;
    private final StatsService statsService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userDetails.getId()).orElse(null);
        model.addAttribute("currentUser", user);

        // 1. Upcoming drives with eligibility
        var upcomingEligible = eligibilityService.evaluateAllUpcoming(
                user != null ? user.getBranch() : "CSE",
                user != null ? user.getCgpa() : null,
                user != null ? user.getActiveBacklogs() : 0
        );
        model.addAttribute("upcomingEvaluations", upcomingEligible);

        // Count eligible upcoming drives
        long eligibleUpcomingCount = upcomingEligible.stream().filter(EligibilityService.EligibilityResult::isEligible).count();
        model.addAttribute("eligibleUpcomingCount", eligibleUpcomingCount);

        // 2. Practice progress stats
        var practiceStats = practiceService.getStudentProgress(userDetails.getId());
        model.addAttribute("practiceStats", practiceStats);

        // 3. User bookmarks
        List<Company> bookmarkedCompanies = bookmarkService.getBookmarkedCompanies(userDetails.getId());
        List<Question> bookmarkedQuestions = bookmarkService.getBookmarkedQuestions(userDetails.getId());
        model.addAttribute("bookmarkedCompanies", bookmarkedCompanies);
        model.addAttribute("bookmarkedQuestions", bookmarkedQuestions);

        // 4. Notifications
        List<Notification> notifications = notificationService.getUserNotifications(userDetails.getId());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(userDetails.getId()));

        // 5. Overall portal highlights
        model.addAttribute("overallStats", statsService.getOverallStats());

        return "dashboard";
    }
}
