package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Roadmap;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.RoadmapService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final CompanyService companyService;
    private final UserService userService;

    @GetMapping
    public String roadmapIndex(
            @RequestParam(value = "roadmapId", required = false) Long roadmapId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) return "redirect:/login";

        List<Roadmap> userRoadmaps = roadmapService.getUserRoadmaps(userDetails.getId());
        List<Company> companies = companyService.getAllCompanies();

        Roadmap currentRoadmap = null;
        if (roadmapId != null) {
            currentRoadmap = roadmapService.getRoadmapById(roadmapId).orElse(null);
        } else if (!userRoadmaps.isEmpty()) {
            currentRoadmap = userRoadmaps.get(0);
        }

        model.addAttribute("userRoadmaps", userRoadmaps);
        model.addAttribute("currentRoadmap", currentRoadmap);
        model.addAttribute("companies", companies);

        return "roadmap/index";
    }

    @PostMapping("/generate")
    public String generateRoadmap(
            @RequestParam("companyId") Long companyId,
            @RequestParam(value = "weeks", defaultValue = "4") int weeks,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findById(userDetails.getId()).orElse(null);
        if (user != null) {
            Roadmap r = roadmapService.generateRoadmap(user, companyId, weeks);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Generated new " + weeks + "-week preparation roadmap for " + r.getCompany().getName() + "!");
            return "redirect:/roadmap?roadmapId=" + r.getId();
        }
        return "redirect:/roadmap";
    }

    @PostMapping("/toggle-item")
    public String toggleItemForm(
            @RequestParam("itemId") Long itemId,
            @RequestParam(value = "roadmapId", required = false) Long roadmapId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) return "redirect:/login";
        roadmapService.toggleItemCompletion(itemId);
        return "redirect:/roadmap" + (roadmapId != null ? "?roadmapId=" + roadmapId : "");
    }

    @PostMapping("/toggle-item/{itemId}")
    @ResponseBody
    public String toggleItem(@PathVariable("itemId") Long itemId) {
        boolean completed = roadmapService.toggleItemCompletion(itemId);
        return completed ? "COMPLETED" : "PENDING";
    }
}
