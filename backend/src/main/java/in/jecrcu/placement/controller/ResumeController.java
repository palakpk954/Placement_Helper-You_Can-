package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Resume;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.ResumeService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final UserService userService;
    private final in.jecrcu.placement.repository.UpcomingDriveRepository upcomingDriveRepository;

    @GetMapping
    public String resumePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        Optional<Resume> myResume = resumeService.getLatestResumeForUser(userDetails.getId());
        List<Resume> sampleResumes = resumeService.getSharedResumes();
        List<in.jecrcu.placement.entity.UpcomingDrive> upcomingDrives = upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc();

        String extractedText = myResume.map(resumeService::extractResumeText).orElse("");

        model.addAttribute("myResume", myResume.orElse(null));
        model.addAttribute("sampleResumes", sampleResumes);
        model.addAttribute("upcomingDrives", upcomingDrives);
        model.addAttribute("extractedText", extractedText);
        model.addAttribute("hasExtractedText", !extractedText.isBlank());

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

    @GetMapping("/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Resume> resumeOpt = resumeService.findById(id);
        if (resumeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resume resume = resumeOpt.get();

        boolean isOwner = resume.getUser() != null && resume.getUser().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !Boolean.TRUE.equals(resume.getShareConsent()) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Path path = Paths.get(resume.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resume.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
