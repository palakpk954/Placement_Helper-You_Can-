package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Notification;
import in.jecrcu.placement.entity.NotificationPreference;
import in.jecrcu.placement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String notificationsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        List<Notification> list = notificationService.getUserNotifications(userDetails.getId());
        NotificationPreference preferences = notificationService.getPreferences(userDetails.getId()).orElse(null);

        long unreadCount = notificationService.getUnreadCount(userDetails.getId());

        model.addAttribute("notifications", list);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("preferences", preferences);

        return "notifications/index";
    }

    @GetMapping("/{id}/open")
    public String openNotification(
            @org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) {
            java.util.Optional<Notification> opt = notificationService.markSingleAsReadAndGet(id, userDetails.getId());
            if (opt.isPresent() && opt.get().getLink() != null && !opt.get().getLink().isBlank()) {
                return "redirect:" + opt.get().getLink();
            }
        }
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/read")
    public String markSingleRead(
            @org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) {
            notificationService.markSingleAsRead(id, userDetails.getId());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/delete")
    public String deleteSingle(
            @org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails != null) {
            notificationService.deleteNotification(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Notification dismissed.");
        }
        return "redirect:/notifications";
    }

    @PostMapping({"/mark-read", "/mark-all-read"})
    public String markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            notificationService.markAllAsRead(userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        }
        return "redirect:/notifications";
    }

    @PostMapping("/preferences")
    public String updatePreferences(
            @RequestParam(value = "emailDrives", defaultValue = "false") boolean drives,
            @RequestParam(value = "emailDeadlines", defaultValue = "false") boolean deadlines,
            @RequestParam(value = "emailExperiences", defaultValue = "false") boolean exp,
            @RequestParam(value = "emailBookings", defaultValue = "false") boolean bookings,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails != null) {
            notificationService.updatePreferences(userDetails.getId(), drives, deadlines, exp, bookings);
            redirectAttributes.addFlashAttribute("successMessage", "Notification preferences updated.");
        }
        return "redirect:/notifications";
    }
}
