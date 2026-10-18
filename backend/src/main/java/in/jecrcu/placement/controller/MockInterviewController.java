package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.MockBooking;
import in.jecrcu.placement.entity.MockSlot;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.MockInterviewService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/mocks")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService mockService;
    private final CompanyService companyService;
    private final UserService userService;

    @GetMapping
    public String mocksDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        List<MockSlot> availableSlots = mockService.getAvailableSlots();
        List<MockBooking> myBookings = mockService.getStudentBookings(userDetails.getId());
        List<MockSlot> myHostedSlots = mockService.getMentorSlots(userDetails.getId());

        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("myBookings", myBookings);
        model.addAttribute("myHostedSlots", myHostedSlots);
        model.addAttribute("companies", companyService.getAllCompanies());

        return "mocks/index";
    }

    @PostMapping("/create-slot")
    public String createSlot(
            @RequestParam("companyName") String companyName,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) return "redirect:/login";

        User mentor = userService.findById(userDetails.getId()).orElse(null);
        if (mentor != null) {
            mockService.createSlot(mentor, companyName, startTime, endTime);
            redirectAttributes.addFlashAttribute("successMessage", "New mock interview slot made available!");
        }
        return "redirect:/mocks";
    }

    @PostMapping("/book")
    public String bookSlot(
            @RequestParam("slotId") Long slotId,
            @RequestParam(value = "notes", required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) return "redirect:/login";

        try {
            User student = userService.findById(userDetails.getId()).orElseThrow();
            mockService.bookSlot(student, slotId, notes);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mock interview session booked successfully! Calendar invitation generated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/mocks";
    }

    @PostMapping("/cancel/{bookingId}")
    public String cancelBooking(
            @PathVariable("bookingId") Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findById(userDetails.getId()).orElseThrow();
        mockService.cancelBooking(bookingId, user);
        redirectAttributes.addFlashAttribute("successMessage", "Interview booking cancelled.");
        return "redirect:/mocks";
    }

    @PostMapping("/feedback/{bookingId}")
    public String submitFeedback(
            @PathVariable("bookingId") Long bookingId,
            @RequestParam("rating") int rating,
            @RequestParam("feedback") String feedback,
            RedirectAttributes redirectAttributes
    ) {
        mockService.submitFeedback(bookingId, rating, feedback);
        redirectAttributes.addFlashAttribute("successMessage", "Thank you for rating your mentor!");
        return "redirect:/mocks";
    }
}
