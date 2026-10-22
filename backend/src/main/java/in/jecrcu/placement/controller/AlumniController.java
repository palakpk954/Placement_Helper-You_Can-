package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.entity.enums.BookingStatus;
import in.jecrcu.placement.entity.enums.Role;
import in.jecrcu.placement.entity.enums.SlotStatus;
import in.jecrcu.placement.repository.CommentRepository;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import in.jecrcu.placement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/alumni")
@RequiredArgsConstructor
public class AlumniController {

    private final AlumniService alumniService;
    private final CompanyService companyService;
    private final UserService userService;
    private final MockInterviewService mockService;
    private final ExperienceService experienceService;
    private final CommentRepository commentRepository;
    private final UpcomingDriveRepository upcomingDriveRepository;

    @GetMapping("/dashboard")
    public String alumniDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session, Model model) {
        if (userDetails == null) return "redirect:/login";

        if (session != null && session.getAttribute("errorMessage") != null) {
            model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
            session.removeAttribute("errorMessage");
        }

        User user = userService.findById(userDetails.getId()).orElse(null);
        if (user == null) return "redirect:/login";

        AlumniProfile profile = alumniService.getProfileByUserId(user.getId()).orElse(null);
        model.addAttribute("currentUser", user);
        model.addAttribute("myProfile", profile);

        // 1. Hosted mock slots
        List<MockSlot> mySlots = mockService.getMentorSlots(user.getId());
        model.addAttribute("mySlots", mySlots);

        // 2. Booked mock sessions with students
        List<MockBooking> studentBookings = mockService.getMentorBookings(user.getId());
        model.addAttribute("studentBookings", studentBookings);

        // 3. Contributed experiences
        List<Experience> myExperiences = experienceService.getUserExperiences(user);
        model.addAttribute("myExperiences", myExperiences);

        // 4. Questions & Discussions from juniors about mentor's company
        if (profile != null && profile.getCompanyName() != null) {
            companyService.findByName(profile.getCompanyName()).ifPresent(comp -> {
                model.addAttribute("mentorCompany", comp);
                List<Comment> companyDiscussions = commentRepository.findByCompanyIdAndParentIsNullAndStatusOrderByCreatedAtDesc(comp.getId(), "VISIBLE");
                model.addAttribute("companyDiscussions", companyDiscussions);
            });
        }

        // 5. Impact metrics
        long totalSlots = mySlots.size();
        long availableSlots = mySlots.stream().filter(s -> s.getStatus() == SlotStatus.AVAILABLE).count();
        long bookedSlots = mySlots.stream().filter(s -> s.getStatus() == SlotStatus.BOOKED).count();
        long completedMocks = studentBookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();

        double avgRating = studentBookings.stream()
                .filter(b -> b.getRating() != null && b.getRating() > 0)
                .mapToInt(MockBooking::getRating)
                .average()
                .orElse(5.0);

        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("bookedSlots", bookedSlots);
        model.addAttribute("completedMocks", completedMocks);
        model.addAttribute("avgRating", String.format(java.util.Locale.US, "%.1f", avgRating));

        // 6. Upcoming campus drives for recruiter context
        model.addAttribute("upcomingDrives", upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc());

        // 7. Companies list for hosting new slots
        model.addAttribute("companies", companyService.getAllCompanies());

        return "alumni/dashboard";
    }

    @GetMapping
    public String directory(
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "year", required = false) Integer year,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        List<AlumniProfile> alumniList = alumniService.searchAlumni(company, branch, year);
        List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("alumniList", alumniList);
        model.addAttribute("companies", companies);
        model.addAttribute("selectedCompany", company);
        model.addAttribute("selectedBranch", branch);
        model.addAttribute("selectedYear", year);

        if (userDetails != null) {
            alumniService.getProfileByUserId(userDetails.getId()).ifPresent(p -> model.addAttribute("myProfile", p));
        }

        return "alumni/index";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("companyName") String companyName,
            @RequestParam("jobRole") String jobRole,
            @RequestParam("graduationYear") Integer graduationYear,
            @RequestParam("branch") String branch,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "topicsCanHelp", required = false) String topicsCanHelp,
            @RequestParam(value = "visible", defaultValue = "false") boolean visible,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findById(userDetails.getId()).orElse(null);
        if (user != null) {
            if (user.getRole() == Role.ROLE_STUDENT) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Only graduated alumni and placed seniors can register as alumni mentors. Current students can share placement experiences under Contribute Experience.");
                return "redirect:/alumni";
            }
            try {
                alumniService.saveProfile(user, companyName.trim(), jobRole.trim(), graduationYear, branch.trim(),
                        linkedinUrl != null ? linkedinUrl.trim() : null,
                        topicsCanHelp != null ? topicsCanHelp.trim() : null, visible);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Your alumni mentor profile has been saved successfully! You can now offer 1-on-1 mock interview slots to juniors.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Unable to save mentor profile: " + e.getMessage());
            }
        }
        return "redirect:/alumni";
    }
}
