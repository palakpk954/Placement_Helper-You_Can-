package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Report;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ReportStatus;
import in.jecrcu.placement.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Report> getOpenReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN);
    }

    @Transactional
    public Report createReport(User reporter, String targetType, Long targetId, String reason) {
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .status(ReportStatus.OPEN)
                .build();
        return reportRepository.save(report);
    }

    @Transactional
    public void resolveReport(Long reportId, String adminNotes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus(ReportStatus.RESOLVED);
        report.setAdminNotes(adminNotes);
        reportRepository.save(report);
    }

    @Transactional
    public void dismissReport(Long reportId, String adminNotes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus(ReportStatus.DISMISSED);
        report.setAdminNotes(adminNotes);
        reportRepository.save(report);
    }
}
