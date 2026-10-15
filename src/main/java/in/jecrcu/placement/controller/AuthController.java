package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid university email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been successfully logged out.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "branch", required = false, defaultValue = "CSE") String branch,
            @RequestParam(value = "graduationYear", required = false, defaultValue = "2026") Integer graduationYear,
            @RequestParam(value = "cgpa", required = false, defaultValue = "7.50") BigDecimal cgpa,
            @RequestParam(value = "activeBacklogs", required = false, defaultValue = "0") Integer activeBacklogs,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.registerUser(email, password, fullName, branch, graduationYear, cgpa, activeBacklogs);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account registered successfully! You can now log in with your university credentials.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            redirectAttributes.addFlashAttribute("successMessage", "Email verified successfully! You can now sign in.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired verification token.");
        }
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage",
                "If an account with that email exists, password reset instructions have been dispatched.");
        return "redirect:/login";
    }
}
