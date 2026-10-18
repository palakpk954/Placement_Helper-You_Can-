package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.AlumniProfile;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.AlumniService;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/alumni")
@RequiredArgsConstructor
public class AlumniController {

    private final AlumniService alumniService;
    private final CompanyService companyService;
    private final UserService userService;

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
            alumniService.saveProfile(user, companyName, jobRole, graduationYear, branch, linkedinUrl, topicsCanHelp, visible);
            redirectAttributes.addFlashAttribute("successMessage", "Your alumni profile has been updated.");
        }
        return "redirect:/alumni";
    }
}
