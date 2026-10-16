package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Resume;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.ResumeService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final UserService userService;

    @GetMapping
    public String resumePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        Optional<Resume> myResume = resumeService.getLatestResumeForUser(userDetails.getId());
        List<Resume> sampleResumes = resumeService.getSharedResumes();

        model.addAttribute("myResume", myResume.orElse(null));
        model.addAttribute("sampleResumes", sampleResumes);

        return "resume/index";
    }

    @PostMapping("/upload")
    public String uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "shareConsent", defaultValue = "false") boolean shareConsent,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) return "redirect:/login";

        try {
            User user = userService.findById(userDetails.getId()).orElseThrow();
            resumeService.uploadAndAnalyze(user, file, shareConsent);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Resume uploaded and analyzed successfully! Check your instant score and recommendations.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading resume: " + e.getMessage());
        }

        return "redirect:/resume";
    }
}
