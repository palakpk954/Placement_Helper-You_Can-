package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.DriveRegistration;
import in.jecrcu.placement.entity.UpcomingDrive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.DriveRegistrationRepository;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import in.jecrcu.placement.service.AuditService;
import in.jecrcu.placement.service.EligibilityService;
import in.jecrcu.placement.service.NotificationService;
import in.jecrcu.placement.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UpcomingController {

    private final UpcomingDriveRepository upcomingDriveRepository;
    private final DriveRegistrationRepository driveRegistrationRepository;
    private final EligibilityService eligibilityService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @GetMapping("/upcoming")
    public String upcomingDrivesPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;

        var evaluations = eligibilityService.evaluateAllUpcoming(
                user != null ? user.getBranch() : "CSE",
                user != null ? user.getCgpa() : null,
                user != null ? user.getActiveBacklogs() : 0
        );

        Set<Long> registeredDriveIds = Collections.emptySet();
        if (user != null) {
            registeredDriveIds = driveRegistrationRepository.findByUserIdOrderByRegisteredAtDesc(user.getId())
                    .stream()
                    .filter(r -> "REGISTERED".equalsIgnoreCase(r.getStatus()))
                    .map(r -> r.getUpcomingDrive().getId())
                    .collect(Collectors.toSet());
        }

        model.addAttribute("evaluations", evaluations);
        model.addAttribute("currentUser", user);
        model.addAttribute("registeredDriveIds", registeredDriveIds);

        return "upcoming/index";
    }

    @GetMapping("/upcoming/{id}/register")
    public String registerPage(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        UpcomingDrive drive = upcomingDriveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + id));

        User user = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;

        var eval = eligibilityService.checkUpcomingEligibility(
                user != null ? user.getBranch() : "CSE",
                user != null ? user.getCgpa() : null,
                user != null ? user.getActiveBacklogs() : 0,
                drive
        );

        Optional<DriveRegistration> existingReg = (user != null)
                ? driveRegistrationRepository.findByUserIdAndUpcomingDriveId(user.getId(), drive.getId())
                : Optional.empty();

        boolean isRegistered = existingReg.isPresent() && "REGISTERED".equalsIgnoreCase(existingReg.get().getStatus());

        model.addAttribute("drive", drive);
        model.addAttribute("currentUser", user);
        model.addAttribute("isEligible", eval.isEligible());
        model.addAttribute("exclusionReasons", eval.getExclusionReasons());
        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("registration", existingReg.orElse(null));

        return "upcoming/register";
    }

    @PostMapping("/upcoming/{id}/register")
    public String handleRegistration(
            @PathVariable("id") Long id,
            @RequestParam(value = "notes", required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        UpcomingDrive drive = upcomingDriveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + id));
        User user = userService.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var eval = eligibilityService.checkUpcomingEligibility(
                user.getBranch(), user.getCgpa(), user.getActiveBacklogs(), drive
        );

        if (!eval.isEligible()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You do not meet the minimum eligibility criteria for " + drive.getCompanyName() + ".");
            return "redirect:/upcoming/" + id + "/register";
        }

        DriveRegistration reg = driveRegistrationRepository.findByUserIdAndUpcomingDriveId(user.getId(), drive.getId())
                .orElseGet(() -> DriveRegistration.builder()
                        .user(user)
                        .upcomingDrive(drive)
                        .build());

        reg.setStatus("REGISTERED");
        reg.setNotes(notes);
        driveRegistrationRepository.save(reg);

        notificationService.sendNotification(
                user,
                "Registered: " + drive.getCompanyName(),
                "You have successfully registered for the " + drive.getCompanyName() + " campus drive (" + drive.getRolesOffered() + "). Deadline: " + drive.getDeadline().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                "/upcoming/" + drive.getId() + "/register",
                "DRIVE_REGISTRATION"
        );

        auditService.logAction(
                user,
                "REGISTER_DRIVE",
                "UpcomingDrive",
                drive.getId(),
                null,
                "Student " + user.getFullName() + " registered for " + drive.getCompanyName()
        );

        redirectAttributes.addFlashAttribute("successMessage",
                "Registration Confirmed! Your application for " + drive.getCompanyName() + " has been successfully submitted to the Training & Placement Cell.");

        return "redirect:/upcoming/" + id + "/register";
    }

    @PostMapping("/upcoming/{id}/withdraw")
    public String handleWithdrawal(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        UpcomingDrive drive = upcomingDriveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + id));

        driveRegistrationRepository.findByUserIdAndUpcomingDriveId(userDetails.getId(), drive.getId())
                .ifPresent(reg -> {
                    reg.setStatus("WITHDRAWN");
                    driveRegistrationRepository.save(reg);
                });

        redirectAttributes.addFlashAttribute("infoMessage",
                "Your registration for " + drive.getCompanyName() + " has been withdrawn.");

        return "redirect:/upcoming/" + id + "/register";
    }

    @GetMapping("/upcoming/{id}/ics")
    public void downloadCalendarEvent(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {
        UpcomingDrive drive = upcomingDriveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found"));

        response.setContentType("text/calendar");
        response.setHeader("Content-Disposition", "attachment; filename=\"drive_" + id + ".ics\"");

        DateTimeFormatter icsDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String startStr = drive.getDeadline().format(icsDate);

        PrintWriter writer = response.getWriter();
        writer.println("BEGIN:VCALENDAR");
        writer.println("VERSION:2.0");
        writer.println("PRODID:-//JECRC University//You_can Placement Portal//EN");
        writer.println("BEGIN:VEVENT");
        writer.println("UID:drive-" + drive.getId() + "@jecrcu.edu.in");
        writer.println("DTSTAMP:" + startStr);
        writer.println("DTSTART:" + startStr);
        writer.println("SUMMARY:Placement Drive: " + drive.getCompanyName() + " (" + drive.getRolesOffered() + ")");
        writer.println("DESCRIPTION:Registration deadline for " + drive.getCompanyName() + ". Package: " + drive.getCtcOrStipend());
        writer.println("LOCATION:JECRC University Campus");
        writer.println("END:VEVENT");
        writer.println("END:VCALENDAR");
        writer.flush();
    }
}
