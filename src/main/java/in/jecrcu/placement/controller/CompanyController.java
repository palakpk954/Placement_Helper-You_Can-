package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.entity.enums.ItemType;
import in.jecrcu.placement.repository.AlumniProfileRepository;
import in.jecrcu.placement.repository.CommentRepository;
import in.jecrcu.placement.service.*;
import in.jecrcu.placement.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final ExperienceService experienceService;
    private final BookmarkService bookmarkService;
    private final AlumniProfileRepository alumniProfileRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ReportService reportService;
    private final RateLimitService rateLimitService;
    private final AiService aiService;


    @GetMapping
    public String directory(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "jobType", required = false) String jobType,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "sort", required = false, defaultValue = "recent") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        populateFilterModel(search, year, month, industry, role, jobType, branch, sort, userDetails, model);
        return "companies/directory";
    }

    @GetMapping("/fragment")
    public String directoryFragment(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "jobType", required = false) String jobType,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "sort", required = false, defaultValue = "recent") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        populateFilterModel(search, year, month, industry, role, jobType, branch, sort, userDetails, model);
        return "fragments/company-cards :: companyGrid";
    }

    private void populateFilterModel(
            String search, Integer year, String month, String industry, String role,
            String jobType, String branch, String sort, CustomUserDetails userDetails, Model model
    ) {
        List<Company> companies = companyService.filterCompanies(search, year, month, industry, role, jobType, branch, sort);

        model.addAttribute("companies", companies);
        model.addAttribute("selectedSearch", search);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedIndustry", industry);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedJobType", jobType);
        model.addAttribute("selectedBranch", branch);
        model.addAttribute("selectedSort", sort);

        model.addAttribute("allIndustries", companyService.getDistinctIndustries());
        model.addAttribute("allYears", companyService.getDistinctYears());
        model.addAttribute("allMonths", companyService.getDistinctMonths());

        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getId());
        }
    }

    @GetMapping("/{id}")
    public String companyDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + id));

        model.addAttribute("company", company);

        // Drives (already sorted in entity by year desc)
        List<Drive> drives = company.getDrives();
        model.addAttribute("drives", drives);

        // Approved experiences
        List<Experience> experiences = experienceService.getApprovedExperiencesForCompany(id);
        model.addAttribute("experiences", experiences);

        // Alumni placed here
        List<AlumniProfile> alumni = alumniProfileRepository.findByCompanyNameIgnoreCaseAndVisibleTrue(company.getName());
        model.addAttribute("alumni", alumni);

        // Q&A Comments
        List<Comment> comments = commentRepository.findByCompanyIdAndParentIsNullAndStatusOrderByCreatedAtDesc(id, "VISIBLE");
        model.addAttribute("comments", comments);

        // User's own difficulty vote
        if (userDetails != null) {
            companyService.getUserVote(id, userDetails.getId()).ifPresent(v -> model.addAttribute("userVote", v.getRating()));
            boolean isBookmarked = bookmarkService.isBookmarked(userDetails.getId(), ItemType.COMPANY, id);
            model.addAttribute("isBookmarked", isBookmarked);
        }

        // AI-1: What to Expect Experience Summary (Phase 4)
        if (aiService != null && aiService.isEnabled()) {
            String aiSummary = aiService.summarizeCompanyExperience(company.getName(), experiences);
            model.addAttribute("aiSummary", aiSummary);
        }

        return "companies/detail";
    }


    @PostMapping("/{id}/vote")
    public String voteDifficulty(
            @PathVariable("id") Long id,
            @RequestParam("rating") int rating,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails != null) {
            User user = userService.findById(userDetails.getId()).orElse(null);
            if (user != null) {
                companyService.voteDifficulty(id, user, rating);
                redirectAttributes.addFlashAttribute("successMessage", "Your difficulty rating (" + rating + " stars) was recorded!");
            }
        }
        return "redirect:/companies/" + id;
    }

    @PostMapping("/{id}/comments")
    public String postComment(
            @PathVariable("id") Long id,
            @RequestParam("content") String content,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails != null && !content.isBlank()) {
            if (!rateLimitService.isCommentAllowed(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "You are commenting too fast. Please wait a few moments before posting again.");
                return "redirect:/companies/" + id;
            }

            User user = userService.findById(userDetails.getId()).orElse(null);
            Company company = companyService.findById(id).orElse(null);
            if (user != null && company != null) {
                Comment parent = parentId != null ? commentRepository.findById(parentId).orElse(null) : null;
                Comment comment = Comment.builder()
                        .company(company)
                        .author(user)
                        .parent(parent)
                        .content(content.trim())
                        .status("VISIBLE")
                        .build();
                commentRepository.save(comment);
                redirectAttributes.addFlashAttribute("successMessage", "Your comment has been posted.");
            }
        }
        return "redirect:/companies/" + id;
    }

    @PostMapping("/{id}/report")
    public String reportEntry(
            @PathVariable("id") Long companyId,
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("reason") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails != null && !reason.isBlank()) {
            User reporter = userService.findById(userDetails.getId()).orElse(null);
            if (reporter != null) {
                reportService.createReport(reporter, targetType, targetId, reason.trim());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thank you! Your report has been submitted to the placement coordinators for review.");
            }
        }
        return "redirect:/companies/" + companyId;
    }

    @PostMapping("/experiences/{expId}/upvote")
    @ResponseBody
    public String upvoteExperience(
            @PathVariable("expId") Long expId
    ) {
        int upvotes = experienceService.upvoteExperience(expId);
        return "<i class=\"bi bi-hand-thumbs-up-fill text-primary me-1\"></i> " + upvotes;
    }
}
