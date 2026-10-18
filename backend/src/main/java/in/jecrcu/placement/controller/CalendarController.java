package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.service.CalendarService;
import in.jecrcu.placement.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final CompanyService companyService;

    @GetMapping("/calendar")
    public String calendarPage(
            @RequestParam(value = "year", required = false) Integer year,
            Model model
    ) {
        List<Integer> years = companyService.getDistinctYears();
        if (years.isEmpty()) {
            years = List.of(2026, 2025, 2024);
        }

        int activeYear = (year != null) ? year : years.get(0);

        Map<String, List<Drive>> monthlyDrives = calendarService.getMonthlyDrivesForYear(activeYear);
        var seasonalityMatrix = calendarService.getSeasonalityMatrix(years);

        model.addAttribute("selectedYear", activeYear);
        model.addAttribute("availableYears", years);
        model.addAttribute("monthlyDrives", monthlyDrives);
        model.addAttribute("seasonalityMatrix", seasonalityMatrix);

        return "calendar/index";
    }
}
