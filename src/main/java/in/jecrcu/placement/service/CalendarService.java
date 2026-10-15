package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.repository.DriveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final DriveRepository driveRepository;

    private static final List<String> MONTH_ORDER = List.of(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    );

    public Map<String, List<Drive>> getMonthlyDrivesForYear(Integer year) {
        List<Drive> yearDrives = driveRepository.findByDriveYearOrderByDriveMonthAsc(year);
        Map<String, List<Drive>> monthlyMap = new LinkedHashMap<>();

        // Group into calendar months in standard order
        for (String month : MONTH_ORDER) {
            monthlyMap.put(month, new ArrayList<>());
        }

        for (Drive drive : yearDrives) {
            String month = drive.getDriveMonth();
            // Match month case-insensitively
            for (String standardMonth : MONTH_ORDER) {
                if (standardMonth.equalsIgnoreCase(month)) {
                    monthlyMap.get(standardMonth).add(drive);
                    break;
                }
            }
        }

        return monthlyMap;
    }

    public static class SeasonalityCell {
        private final int count;
        private final List<String> companyNames;

        public SeasonalityCell(int count, List<String> companyNames) {
            this.count = count;
            this.companyNames = companyNames;
        }

        public int getCount() {
            return count;
        }

        public List<String> getCompanyNames() {
            return companyNames;
        }
    }

    public Map<String, Map<Integer, SeasonalityCell>> getSeasonalityMatrix(List<Integer> years) {
        List<Drive> allDrives = driveRepository.findAll();
        Map<String, Map<Integer, SeasonalityCell>> matrix = new LinkedHashMap<>();

        // Academic placement cycle: July to June
        List<String> academicMonths = List.of(
                "July", "August", "September", "October", "November", "December",
                "January", "February", "March", "April", "May", "June"
        );

        for (String month : academicMonths) {
            Map<Integer, SeasonalityCell> yearMap = new LinkedHashMap<>();
            for (Integer y : years) {
                List<String> companies = new ArrayList<>();
                for (Drive d : allDrives) {
                    if (d.getDriveYear().equals(y) && d.getDriveMonth().equalsIgnoreCase(month)) {
                        companies.add(d.getCompany().getName());
                    }
                }
                yearMap.put(y, new SeasonalityCell(companies.size(), companies));
            }
            matrix.put(month, yearMap);
        }

        return matrix;
    }
}
