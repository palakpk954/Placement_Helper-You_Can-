package in.jecrcu.placement.controller;

import in.jecrcu.placement.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/statistics")
    public String statisticsPage(Model model) {
        model.addAttribute("overallStats", statsService.getOverallStats());
        model.addAttribute("companiesPerYearChart", statsService.getCompaniesPerYearChart());
        model.addAttribute("monthlyDistributionChart", statsService.getMonthlyDistributionChart());
        model.addAttribute("topRecruiters", statsService.getTopRecruiters());
        model.addAttribute("packageTrends", statsService.getPackageTrends());
        model.addAttribute("branchStats", statsService.getBranchPlacementStats());
        model.addAttribute("topicStats", statsService.getTopicFrequencyChart());

        return "statistics/index";
    }
}
