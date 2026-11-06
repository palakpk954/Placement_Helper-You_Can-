package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.dto.ApplicationRequestDto;
import in.jecrcu.placement.dto.PipelineStatsDto;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.JobApplication;
import in.jecrcu.placement.entity.UpcomingDrive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ApplicationStage;
import in.jecrcu.placement.entity.enums.ApplicationType;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import in.jecrcu.placement.service.ApplicationPipelineService;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pipeline")
@RequiredArgsConstructor
@Slf4j
public class PipelineController {

    private final ApplicationPipelineService pipelineService;
    private final UserService userService;
    private final CompanyService companyService;
    private final UpcomingDriveRepository upcomingDriveRepository;

    @GetMapping
    public String viewPipeline(
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
            @RequestParam(value = "q", required = false) String query,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userDetails.getId()).orElse(null);
        Map<ApplicationStage, List<JobApplication>> board = pipelineService.getKanbanBoard(userDetails.getId(), type, query);
        PipelineStatsDto stats = pipelineService.getPipelineStats(userDetails.getId());
        List<JobApplication> upcomingInterviews = pipelineService.getUpcomingInterviews(userDetails.getId());
        List<Company> companies = companyService.getAllCompanies();
        List<UpcomingDrive> upcomingDrives = upcomingDriveRepository.findAll();

        model.addAttribute("currentUser", user);
        model.addAttribute("board", board);
        model.addAttribute("stages", ApplicationStage.values());
        model.addAttribute("stats", stats);
        model.addAttribute("upcomingInterviews", upcomingInterviews);
        model.addAttribute("companies", companies);
        model.addAttribute("upcomingDrives", upcomingDrives);
        model.addAttribute("selectedType", type);
        model.addAttribute("searchQuery", query);
        model.addAttribute("pageTitle", "Recruitment Pipeline & Kanban Tracker");

        return "pipeline/index";
    }

    @GetMapping("/board")
    public String getBoardFragment(
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
            @RequestParam(value = "q", required = false) String query,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "error";
        }

        Map<ApplicationStage, List<JobApplication>> board = pipelineService.getKanbanBoard(userDetails.getId(), type, query);
        model.addAttribute("board", board);
        model.addAttribute("stages", ApplicationStage.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("searchQuery", query);

        return "pipeline/index :: boardContainer";
    }

    @PostMapping("/{id}/stage")
    public String updateStage(
            @PathVariable("id") Long id,
            @RequestParam("stage") String stageStr,
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
            @RequestParam(value = "q", required = false) String query,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            ApplicationStage newStage = ApplicationStage.valueOf(stageStr.toUpperCase());
            pipelineService.updateStage(userDetails.getId(), id, newStage);

            if ("true".equalsIgnoreCase(hxRequest)) {
                Map<ApplicationStage, List<JobApplication>> board = pipelineService.getKanbanBoard(userDetails.getId(), type, query);
                PipelineStatsDto stats = pipelineService.getPipelineStats(userDetails.getId());
                model.addAttribute("board", board);
                model.addAttribute("stages", ApplicationStage.values());
                model.addAttribute("stats", stats);
                model.addAttribute("selectedType", type);
                model.addAttribute("searchQuery", query);
                return "pipeline/index :: boardContainer";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Stage updated successfully!");
        } catch (Exception e) {
            log.error("Failed to update application stage: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update stage: " + e.getMessage());
        }

        return "redirect:/pipeline";
    }

    @PostMapping("/add")
    public String addApplication(
            @ModelAttribute ApplicationRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            pipelineService.createApplication(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Application for " + dto.getCompanyName() + " added to your recruitment pipeline!");
        } catch (Exception e) {
            log.error("Error creating application: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving application: " + e.getMessage());
        }

        return "redirect:/pipeline";
    }

    @PostMapping("/{id}/update")
    public String updateApplication(
            @PathVariable("id") Long id,
            @ModelAttribute ApplicationRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            dto.setId(id);
            pipelineService.updateDetails(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Application details updated.");
        } catch (Exception e) {
            log.error("Error updating application: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update details: " + e.getMessage());
        }

        return "redirect:/pipeline";
    }

    @PostMapping("/{id}/delete")
    public String deleteApplication(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            pipelineService.deleteApplication(userDetails.getId(), id);
            redirectAttributes.addFlashAttribute("infoMessage", "Application removed from your pipeline.");
        } catch (Exception e) {
            log.error("Error deleting application: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Could not remove application: " + e.getMessage());
        }

        return "redirect:/pipeline";
    }

    @GetMapping("/{id}/modal")
    public String getEditModal(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "error";
        }

        JobApplication app = pipelineService.getApplication(userDetails.getId(), id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        model.addAttribute("app", app);
        model.addAttribute("stages", ApplicationStage.values());
        model.addAttribute("appTypes", ApplicationType.values());

        return "pipeline/edit_modal :: editModalContent";
    }
}
