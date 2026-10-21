package in.jecrcu.placement.controller;

import in.jecrcu.placement.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/statistics")
    public String statisticsPage(
            @RequestParam(value = "year", defaultValue = "2025-26") String year,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        model.addAttribute("overallStats", statsService.getOverallStats());
        model.addAttribute("companiesPerYearChart", statsService.getCompaniesPerYearChart());
        model.addAttribute("monthlyDistributionChart", statsService.getMonthlyDistributionChart());
        model.addAttribute("topRecruiters", statsService.getTopRecruiters());
        model.addAttribute("packageTrends", statsService.getPackageTrends());
        model.addAttribute("branchStats", statsService.getBranchPlacementStats());
        model.addAttribute("topicStats", statsService.getTopicFrequencyChart());

        // Official JECRC University Scraped Records & Highlights
        model.addAttribute("officialHighlights", statsService.getOfficialHighlights());
        model.addAttribute("officialYears", statsService.getOfficialAcademicYears());
        model.addAttribute("selectedYear", year);
        model.addAttribute("searchQuery", search);
        model.addAttribute("officialRecords", statsService.getOfficialRecords(year, search));
        model.addAttribute("yearSummaries", statsService.getYearSummaryStats());

        return "statistics/index";
    }

    @GetMapping("/statistics/records-fragment")
    public String recordsFragment(
            @RequestParam(value = "year", defaultValue = "2025-26") String year,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        model.addAttribute("selectedYear", year);
        model.addAttribute("searchQuery", search);
        model.addAttribute("officialRecords", statsService.getOfficialRecords(year, search));
        return "statistics/fragments/records_table :: recordsTable";
    }
}
