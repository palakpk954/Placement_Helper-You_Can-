package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.service.ChecklistService;
import in.jecrcu.placement.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;
    private final CompanyService companyService;

    @GetMapping("/checklists")
    public String checklistsIndex(@RequestParam(value = "companyId", required = false) Long companyId) {
        if (companyId != null) {
            return "redirect:/checklists/" + companyId;
        }
        List<Company> list = companyService.getAllCompanies();
        if (!list.isEmpty()) {
            return "redirect:/checklists/" + list.get(0).getId();
        }
        return "redirect:/companies";
    }

    @GetMapping("/checklists/{id}")
    public String companyChecklist(@PathVariable("id") Long id, Model model) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        var sections = checklistService.generateChecklist(id);
        List<Company> allCompanies = companyService.getAllCompanies();

        model.addAttribute("company", company);
        model.addAttribute("sections", sections);
        model.addAttribute("allCompanies", allCompanies);

        return "checklists/index";
    }
}
