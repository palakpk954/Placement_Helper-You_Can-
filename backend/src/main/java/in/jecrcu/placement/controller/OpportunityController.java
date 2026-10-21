package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.Opportunity;
import in.jecrcu.placement.entity.enums.OpportunityType;
import in.jecrcu.placement.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    public String opportunitiesPage(
            @RequestParam(value = "type", required = false) String typeStr,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        OpportunityType filterType = null;
        if (typeStr != null && !typeStr.trim().isEmpty() && !"ALL".equalsIgnoreCase(typeStr)) {
            try {
                filterType = OpportunityType.valueOf(typeStr.toUpperCase().trim());
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<Opportunity> list = opportunityService.getOpportunities(filterType, search);

        model.addAttribute("opportunities", list);
        model.addAttribute("types", OpportunityType.values());
        model.addAttribute("selectedType", filterType != null ? filterType.name() : "ALL");
        model.addAttribute("searchQuery", search != null ? search.trim() : "");
        model.addAttribute("stats", opportunityService.getStatistics());

        return "opportunities/index";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Opportunity> getOpportunityDetails(@PathVariable("id") Long id) {
        return opportunityService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
