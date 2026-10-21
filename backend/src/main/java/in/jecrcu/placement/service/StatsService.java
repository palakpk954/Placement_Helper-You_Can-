package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.JecrcPlacementRecord;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.DriveRepository;
import in.jecrcu.placement.repository.JecrcPlacementRecordRepository;
import in.jecrcu.placement.repository.QuestionRepository;
import in.jecrcu.placement.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final DriveRepository driveRepository;
    private final CompanyRepository companyRepository;
    private final RoundRepository roundRepository;
    private final QuestionRepository questionRepository;
    private final JecrcPlacementRecordRepository jecrcPlacementRecordRepository;

    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Drive> allDrives = driveRepository.findAll();
        long totalCompanies = companyRepository.count();
        long totalQuestions = questionRepository.count();
        long totalRounds = roundRepository.count();

        int totalOffers = allDrives.stream().mapToInt(d -> d.getStudentsSelected() != null ? d.getStudentsSelected() : 0).sum();

        long drivesWithRounds = allDrives.stream().filter(d -> d.getRounds() != null && !d.getRounds().isEmpty()).count();
        double avgRounds = drivesWithRounds > 0 ? (double) totalRounds / drivesWithRounds : (allDrives.isEmpty() ? 0.0 : 3.5);

        // Package analytics across all recruitment drives
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        List<Double> packages = new ArrayList<>();
        for (Drive d : allDrives) {
            if (d.getCtcOrStipend() != null) {
                Matcher matcher = pattern.matcher(d.getCtcOrStipend());
                if (matcher.find()) {
                    packages.add(Double.parseDouble(matcher.group(1)));
                }
            }
        }

        double avgPackage = packages.isEmpty() ? 0.0 : packages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maxPackage = packages.isEmpty() ? 0.0 : packages.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        stats.put("totalCompanies", totalCompanies);
        stats.put("totalDrives", allDrives.size());
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalOffers", totalOffers);
        stats.put("averageRounds", Math.round(avgRounds * 10.0) / 10.0);
        stats.put("averagePackage", String.format(Locale.US, "%.1f LPA", avgPackage));
        stats.put("highestPackage", String.format(Locale.US, "%.1f LPA", maxPackage));

        return stats;
    }

    public Map<String, Object> getCompaniesPerYearChart() {
        List<Object[]> rows = driveRepository.countCompaniesPerYear();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Object[] row : rows) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    public Map<String, Object> getMonthlyDistributionChart() {
        List<Drive> allDrives = driveRepository.findAll();
        List<String> months = List.of("July", "August", "September", "October", "November", "December", "January", "February", "March", "April");
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String m : months) {
            counts.put(m, 0);
        }

        for (Drive d : allDrives) {
            for (String m : months) {
                if (m.equalsIgnoreCase(d.getDriveMonth())) {
                    counts.put(m, counts.get(m) + 1);
                    break;
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("labels", new ArrayList<>(counts.keySet()));
        data.put("values", new ArrayList<>(counts.values()));
        return data;
    }

    public List<Map<String, Object>> getTopRecruiters() {
        List<Object[]> rows = driveRepository.findTopRecruitersBySelections();
        List<Map<String, Object>> list = new ArrayList<>();
        int limit = 10;
        for (Object[] r : rows) {
            if (limit-- <= 0) break;
            Map<String, Object> map = new HashMap<>();
            map.put("companyName", r[0]);
            map.put("selections", r[1]);
            list.add(map);
        }
        return list;
    }

    public Map<String, Object> getPackageTrends() {
        List<Drive> all = driveRepository.findAll();
        Map<Integer, List<Double>> yearPackages = new TreeMap<>();
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");

        for (Drive d : all) {
            if (d.getCtcOrStipend() != null) {
                Matcher matcher = pattern.matcher(d.getCtcOrStipend());
                if (matcher.find()) {
                    double val = Double.parseDouble(matcher.group(1));
                    yearPackages.computeIfAbsent(d.getDriveYear(), k -> new ArrayList<>()).add(val);
                }
            }
        }

        List<String> years = new ArrayList<>();
        List<Double> averages = new ArrayList<>();
        List<Double> highest = new ArrayList<>();

        for (Map.Entry<Integer, List<Double>> entry : yearPackages.entrySet()) {
            years.add(entry.getKey().toString());
            List<Double> vals = entry.getValue();
            double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double max = vals.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            averages.add(Math.round(avg * 100.0) / 100.0);
            highest.add(max);
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("years", years);
        chartData.put("averages", averages);
        chartData.put("highest", highest);
        return chartData;
    }

    public Map<String, Object> getBranchPlacementStats() {
        List<Drive> all = driveRepository.findAll();
        Map<String, Integer> branchOffers = new LinkedHashMap<>();
        List<String> branches = List.of("CSE", "IT", "ECE", "EE", "ME", "Civil", "MCA", "BCA");
        for (String b : branches) branchOffers.put(b, 0);

        for (Drive d : all) {
            int offers = d.getStudentsSelected() != null ? d.getStudentsSelected() : 0;
            String eligible = d.getEligibleBranches();
            if (eligible != null) {
                for (String b : branches) {
                    if (eligible.toLowerCase().contains(b.toLowerCase())) {
                        branchOffers.put(b, branchOffers.get(b) + offers);
                    }
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("labels", new ArrayList<>(branchOffers.keySet()));
        data.put("values", new ArrayList<>(branchOffers.values()));
        return data;
    }

    public Map<String, Object> getTopicFrequencyChart() {
        List<String> tags = questionRepository.findAllTopicTags();
        Map<String, Integer> topicCounts = new HashMap<>();

        for (String tagString : tags) {
            if (tagString != null) {
                String[] split = tagString.split(",");
                for (String s : split) {
                    String clean = s.trim();
                    if (!clean.isEmpty()) {
                        topicCounts.put(clean, topicCounts.getOrDefault(clean, 0) + 1);
                    }
                }
            }
        }

        List<Map.Entry<String, Integer>> sorted = topicCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(8)
                .toList();

        List<String> labels = sorted.stream().map(Map.Entry::getKey).toList();
        List<Integer> values = sorted.stream().map(Map.Entry::getValue).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    public Map<String, Object> getOfficialHighlights() {
        Map<String, Object> highlights = new LinkedHashMap<>();
        highlights.put("fiveYearOffers", "12,000+");
        highlights.put("annualOffers", "2,000+");
        highlights.put("currentBatchOffers", "2,104+");
        highlights.put("currentHighestPackage", "₹33.00 LPA");
        highlights.put("allTimeHighestPackage", "₹52.00 LPA");
        highlights.put("top30PercentAvgPackage", "₹10.70 LPA");
        highlights.put("technicalAvgPackage", "₹6.45 LPA");
        highlights.put("managementAvgPackage", "₹7.00 LPA");
        highlights.put("coreAvgPackage", "₹5.00 LPA");
        highlights.put("skillAvgPackage", "₹4.00 LPA");
        highlights.put("fortune500Offers", "856");
        highlights.put("annualRecruiters", "200+");
        highlights.put("seasonDuration", "230 Days");
        highlights.put("mentorshipHours", "500+ Hours");
        return highlights;
    }

    public List<String> getOfficialAcademicYears() {
        List<String> years = jecrcPlacementRecordRepository.findDistinctAcademicYears();
        if (years.isEmpty()) {
            return List.of("2025-26", "2024-25", "2023-24", "2022-23", "2021-22", "2020-21", "2019-20");
        }
        return years;
    }

    public List<JecrcPlacementRecord> getOfficialRecords(String academicYear, String search) {
        if (academicYear == null || academicYear.isBlank()) {
            academicYear = "2025-26";
        }
        if (search != null && !search.trim().isEmpty()) {
            return jecrcPlacementRecordRepository.findByAcademicYearAndCompanyNameContainingIgnoreCaseOrderBySelectionsDesc(academicYear, search.trim());
        }
        return jecrcPlacementRecordRepository.findByAcademicYearOrderBySelectionsDesc(academicYear);
    }

    public Map<String, Map<String, Object>> getYearSummaryStats() {
        List<String> years = getOfficialAcademicYears();
        Map<String, Map<String, Object>> summary = new LinkedHashMap<>();

        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");

        for (String yr : years) {
            List<JecrcPlacementRecord> records = jecrcPlacementRecordRepository.findByAcademicYearOrderBySelectionsDesc(yr);
            Map<String, Object> yrData = new HashMap<>();
            int totalSelections = records.stream().mapToInt(r -> r.getSelections() != null ? r.getSelections() : 0).sum();
            double highestPkg = 0.0;
            String topCompany = !records.isEmpty() ? records.get(0).getCompanyName() : "-";
            int topCompanySel = !records.isEmpty() ? records.get(0).getSelections() : 0;

            for (JecrcPlacementRecord r : records) {
                if (r.getPackageLpa() != null) {
                    Matcher m = pattern.matcher(r.getPackageLpa());
                    while (m.find()) {
                        try {
                            double val = Double.parseDouble(m.group(1));
                            if (val > highestPkg) {
                                highestPkg = val;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            yrData.put("totalRecords", records.size());
            yrData.put("totalSelections", totalSelections);
            yrData.put("highestPackage", highestPkg > 0 ? String.format(Locale.US, "%.1f LPA", highestPkg) : "N/A");
            yrData.put("topRecruiter", topCompany);
            yrData.put("topRecruiterSelections", topCompanySel);
            summary.put(yr, yrData);
        }
        return summary;
    }
}
