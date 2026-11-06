package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.PlacementDebarmentRecord;
import in.jecrcu.placement.entity.StudentPlacementRecord;
import in.jecrcu.placement.repository.PlacementDebarmentRecordRepository;
import in.jecrcu.placement.repository.StudentPlacementRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentPlacementService {

    private final StudentPlacementRecordRepository studentRepo;
    private final PlacementDebarmentRecordRepository debarmentRepo;

    public Map<String, Object> getFunnelMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long total = studentRepo.count();
        long enrolled = studentRepo.countByPlacementStatus("ENROLLED");
        long placed = studentRepo.countByPlacementStatus("PLACED");
        long blocked = studentRepo.countByPlacementStatus("BLOCKED");

        double rate = total > 0 ? ((double) placed / total) * 100.0 : 0.0;
        BigDecimal avgPkg = studentRepo.findAveragePackage();
        BigDecimal maxPkg = studentRepo.findHighestPackage();

        List<StudentPlacementRecord> all = studentRepo.findAll();
        long sumApps = all.stream().mapToLong(r -> r.getTotalApplications() != null ? r.getTotalApplications() : 0).sum();
        long sumPpt = all.stream().mapToLong(r -> r.getPptCount() != null ? r.getPptCount() : 0).sum();
        long sumScreening = all.stream().mapToLong(r -> r.getScreeningCount() != null ? r.getScreeningCount() : 0).sum();
        long sumGd = all.stream().mapToLong(r -> r.getGdCount() != null ? r.getGdCount() : 0).sum();
        long sumPi = all.stream().mapToLong(r -> r.getPiCount() != null ? r.getPiCount() : 0).sum();
        long sumOffers = all.stream().mapToLong(r -> r.getOffersCount() != null ? r.getOffersCount() : 0).sum();

        long debarredCount = debarmentRepo.count();

        // At-risk counts
        long lowAppCount = studentRepo.findLowApplicationRisk().size();
        long screeningBottleneckCount = studentRepo.findScreeningBottleneckRisk().size();
        long interviewLagCount = studentRepo.findInterviewConversionRisk().size();

        metrics.put("totalStudents", total);
        metrics.put("enrolledCount", enrolled);
        metrics.put("placedCount", placed);
        metrics.put("blockedCount", blocked);
        metrics.put("placementRate", Math.round(rate * 10.0) / 10.0);
        metrics.put("averagePackage", avgPkg != null ? avgPkg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        metrics.put("highestPackage", maxPkg != null ? maxPkg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        metrics.put("totalApplications", sumApps);
        metrics.put("totalPpt", sumPpt);
        metrics.put("totalScreening", sumScreening);
        metrics.put("totalGd", sumGd);
        metrics.put("totalPi", sumPi);
        metrics.put("totalOffers", sumOffers > 0 ? sumOffers : placed);
        metrics.put("debarredCount", debarredCount);

        metrics.put("lowAppCount", lowAppCount);
        metrics.put("screeningBottleneckCount", screeningBottleneckCount);
        metrics.put("interviewLagCount", interviewLagCount);

        return metrics;
    }

    public List<StudentPlacementRecord> getLowApplicationRisk() {
        return studentRepo.findLowApplicationRisk();
    }

    public List<StudentPlacementRecord> getScreeningBottleneckRisk() {
        return studentRepo.findScreeningBottleneckRisk();
    }

    public List<StudentPlacementRecord> getInterviewConversionRisk() {
        return studentRepo.findInterviewConversionRisk();
    }

    public Optional<StudentPlacementRecord> findByEmailOrRoll(String emailOrRoll) {
        if (emailOrRoll == null) return Optional.empty();
        String q = emailOrRoll.trim();
        Optional<StudentPlacementRecord> byEmail = studentRepo.findByEmailIgnoreCase(q);
        if (byEmail.isPresent()) return byEmail;
        return studentRepo.findByRollNumber(q.toUpperCase());
    }

    public Map<String, Object> getStudentRiskSummary(StudentPlacementRecord rec) {
        Map<String, Object> map = new HashMap<>();
        if (rec == null) return map;

        map.put("record", rec);
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if ("BLOCKED".equalsIgnoreCase(rec.getPlacementStatus())) {
            warnings.add("Account is currently DEBARRED / BLOCKED due to missed mandatory recruitment events.");
            recommendations.add("Visit the Placement Cell coordinator to file an attendance clearance request.");
        } else if ("PLACED".equalsIgnoreCase(rec.getPlacementStatus())) {
            map.put("isPlaced", true);
            recommendations.add("Congratulations on your placement at " + rec.getPlacedCompany() + "! Share your interview experience to mentor juniors.");
        } else {
            // Evaluator for active enrolled students
            int elig = rec.getEligibleProfiles() != null ? rec.getEligibleProfiles() : 0;
            int apps = rec.getTotalApplications() != null ? rec.getTotalApplications() : 0;
            int screen = rec.getScreeningCount() != null ? rec.getScreeningCount() : 0;
            int pi = rec.getPiCount() != null ? rec.getPiCount() : 0;
            int offers = rec.getOffersCount() != null ? rec.getOffersCount() : 0;

            if (elig >= 25 && apps <= 2) {
                warnings.add("High Inaction Alert: You are currently eligible for " + elig + " company drives, but have submitted only " + apps + " applications.");
                recommendations.add("Review upcoming recruitment drives and register before deadlines close.");
            }
            if (apps >= 8 && screen <= 1) {
                warnings.add("Screening Bottleneck: You have applied to " + apps + " companies with low online test clearance rate.");
                recommendations.add("Practice DSA & Core Aptitude questions from past JECRC interview drives in Practice Mode.");
            }
            if (pi >= 2 && offers == 0) {
                warnings.add("Interview Conversion Lag: You have reached " + pi + " Personal Interview (PI) rounds without converting an offer.");
                recommendations.add("Book a 1-on-1 Mock Interview session with placed alumni to polish behavioral & technical responses.");
            }
        }

        map.put("warnings", warnings);
        map.put("recommendations", recommendations);
        map.put("hasWarnings", !warnings.isEmpty());
        return map;
    }

    public List<Map<String, Object>> getTopRecruiters() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Object[]> raw = studentRepo.findTopRecruiters();

        for (Object[] r : raw) {
            Map<String, Object> map = new HashMap<>();
            map.put("companyName", r[0]);
            map.put("hireCount", r[1]);
            Double avg = (Double) r[2];
            BigDecimal max = (BigDecimal) r[3];
            map.put("avgCtc", avg != null ? String.format(Locale.US, "%.2f LPA", avg) : "N/A");
            map.put("maxCtc", max != null ? String.format(Locale.US, "%.2f LPA", max.doubleValue()) : "N/A");
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getCourseStatistics() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Object[]> raw = studentRepo.findCourseStatistics();

        for (Object[] r : raw) {
            Map<String, Object> map = new HashMap<>();
            String course = (String) r[0];
            Long total = (Long) r[1];
            Long placed = ((Number) r[2]).longValue();
            Long blocked = ((Number) r[3]).longValue();

            double rate = total > 0 ? ((double) placed / total) * 100.0 : 0.0;

            map.put("course", course);
            map.put("total", total);
            map.put("placed", placed);
            map.put("blocked", blocked);
            map.put("placementRate", Math.round(rate * 10.0) / 10.0);
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getMentorStatistics() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Object[]> raw = studentRepo.findMentorStatistics();

        for (Object[] r : raw) {
            Map<String, Object> map = new HashMap<>();
            String mentor = (String) r[0];
            Long total = (Long) r[1];
            Long placed = ((Number) r[2]).longValue();
            Long blocked = ((Number) r[3]).longValue();

            double rate = total > 0 ? ((double) placed / total) * 100.0 : 0.0;

            map.put("mentor", mentor);
            map.put("total", total);
            map.put("placed", placed);
            map.put("blocked", blocked);
            map.put("placementRate", Math.round(rate * 10.0) / 10.0);
            list.add(map);
        }
        return list;
    }

    public List<PlacementDebarmentRecord> getDebarredRecords() {
        return debarmentRepo.findAllByOrderByAbsentCountDesc();
    }

    public List<String> getDistinctCourses() {
        return studentRepo.findDistinctCourses();
    }

    public List<String> getDistinctMentors() {
        return studentRepo.findDistinctMentors();
    }

    public Page<StudentPlacementRecord> filterStudents(String search, String status, String course, String mentor, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        String cleanCourse = (course != null && !course.trim().isEmpty()) ? course.trim() : null;
        String cleanMentor = (mentor != null && !mentor.trim().isEmpty()) ? mentor.trim() : null;

        return studentRepo.filterStudents(cleanSearch, cleanStatus, cleanCourse, cleanMentor, pageable);
    }

    @Transactional
    public boolean unblockStudent(Long id) {
        Optional<StudentPlacementRecord> opt = studentRepo.findById(id);
        if (opt.isPresent()) {
            StudentPlacementRecord rec = opt.get();
            rec.setPlacementStatus("ENROLLED");
            studentRepo.save(rec);

            List<PlacementDebarmentRecord> debList = debarmentRepo.findByRollNumber(rec.getRollNumber());
            for (PlacementDebarmentRecord d : debList) {
                d.setPenaltyStatus("UNBLOCKED_BY_ADMIN");
                debarmentRepo.save(d);
            }
            log.info("Student unblocked successfully: {}", rec.getRollNumber());
            return true;
        }
        return false;
    }
}
