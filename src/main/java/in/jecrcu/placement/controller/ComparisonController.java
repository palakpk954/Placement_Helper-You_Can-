package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ComparisonController {

    private final CompanyService companyService;
    private final ComparisonService comparisonService;

    @GetMapping("/compare")
    public String comparePage(
            @RequestParam(value = "c1", required = false) Long c1,
            @RequestParam(value = "c2", required = false) Long c2,
            Model model
    ) {
        List<Company> companies = companyService.getAllCompanies();
        model.addAttribute("companies", companies);

        // Defaults to first 2 companies if not specified
        if (c1 == null && companies.size() > 0) c1 = companies.get(0).getId();
        if (c2 == null && companies.size() > 1) c2 = companies.get(1).getId();

        model.addAttribute("c1Id", c1);
        model.addAttribute("c2Id", c2);

        if (c1 != null) {
            comparisonService.getProfile(c1).ifPresent(p -> model.addAttribute("profile1", p));
        }
        if (c2 != null) {
            comparisonService.getProfile(c2).ifPresent(p -> model.addAttribute("profile2", p));
        }

        return "compare/index";
    }
}
