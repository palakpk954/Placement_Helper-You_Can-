package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.DriveRepository;
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

    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Drive> allDrives = driveRepository.findAll();
        long totalCompanies = companyRepository.count();
        long totalQuestions = questionRepository.count();
        long totalRounds = roundRepository.count();

        int totalOffers = allDrives.stream().mapToInt(d -> d.getStudentsSelected() != null ? d.getStudentsSelected() : 0).sum();
        double avgRounds = allDrives.isEmpty() ? 0 : (double) totalRounds / allDrives.size();

        stats.put("totalCompanies", totalCompanies);
        stats.put("totalDrives", allDrives.size());
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalOffers", totalOffers);
        stats.put("averageRounds", Math.round(avgRounds * 10.0) / 10.0);

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
}
