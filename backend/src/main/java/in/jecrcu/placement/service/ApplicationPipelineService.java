package in.jecrcu.placement.service;

import in.jecrcu.placement.dto.ApplicationRequestDto;
import in.jecrcu.placement.dto.PipelineStatsDto;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.JobApplication;
import in.jecrcu.placement.entity.UpcomingDrive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ApplicationStage;
import in.jecrcu.placement.entity.enums.ApplicationType;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.JobApplicationRepository;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationPipelineService {

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UpcomingDriveRepository upcomingDriveRepository;

    /**
     * Retrieves Kanban board columns grouped by ApplicationStage.
     */
    @Transactional(readOnly = true)
    public Map<ApplicationStage, List<JobApplication>> getKanbanBoard(Long userId, String typeFilterStr, String query) {
        ApplicationType type = null;
        if (typeFilterStr != null && !typeFilterStr.isBlank() && !typeFilterStr.equalsIgnoreCase("ALL")) {
            try {
                type = ApplicationType.valueOf(typeFilterStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        List<JobApplication> applications = applicationRepository.searchApplications(userId, type, query);

        Map<ApplicationStage, List<JobApplication>> board = new LinkedHashMap<>();
        for (ApplicationStage stage : ApplicationStage.values()) {
            board.put(stage, new ArrayList<>());
        }

        for (JobApplication app : applications) {
            ApplicationStage stage = app.getStage() != null ? app.getStage() : ApplicationStage.APPLIED;
            board.computeIfAbsent(stage, k -> new ArrayList<>()).add(app);
        }

        return board;
    }

    /**
     * Computes funnel statistics for the student.
     */
    @Transactional(readOnly = true)
    public PipelineStatsDto getPipelineStats(Long userId) {
        return PipelineStatsDto.builder()
                .totalApplications(applicationRepository.countByUserId(userId))
                .appliedCount(applicationRepository.countByUserIdAndStage(userId, ApplicationStage.APPLIED))
                .oaCount(applicationRepository.countByUserIdAndStage(userId, ApplicationStage.OA_ROUND))
                .techCount(applicationRepository.countByUserIdAndStage(userId, ApplicationStage.TECH_INTERVIEW))
                .hrCount(applicationRepository.countByUserIdAndStage(userId, ApplicationStage.HR_ROUND))
                .offeredCount(applicationRepository.countByUserIdAndStage(userId, ApplicationStage.OFFERED))
                .rejectedCount(applicationRepository.countByUserIdAndStage(userId, ApplicationStage.REJECTED))
                .build();
    }

    /**
     * Returns scheduled upcoming interviews or assessment dates for the student.
     */
    @Transactional(readOnly = true)
    public List<JobApplication> getUpcomingInterviews(Long userId) {
        return applicationRepository.findUpcomingInterviews(userId);
    }

    /**
     * Updates an application's stage (e.g. from Drag & Drop or Step button).
     */
    @Transactional
    public JobApplication updateStage(Long userId, Long applicationId, ApplicationStage newStage) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to update this application");
        }

        app.setStage(newStage);
        return applicationRepository.save(app);
    }

    /**
     * Creates a new on-campus or off-campus application manually.
     */
    @Transactional
    public JobApplication createApplication(Long userId, ApplicationRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Company company = null;
        if (dto.getCompanyId() != null) {
            company = companyRepository.findById(dto.getCompanyId()).orElse(null);
        } else if (dto.getCompanyName() != null && !dto.getCompanyName().isBlank()) {
            company = companyRepository.findByNameIgnoreCase(dto.getCompanyName().trim()).orElse(null);
        }

        UpcomingDrive upcomingDrive = null;
        if (dto.getUpcomingDriveId() != null) {
            upcomingDrive = upcomingDriveRepository.findById(dto.getUpcomingDriveId()).orElse(null);
            if (upcomingDrive != null && company == null) {
                company = upcomingDrive.getCompany();
            }
        }

        ApplicationType appType = dto.getApplicationType() != null ? dto.getApplicationType() : ApplicationType.OFF_CAMPUS;
        if (upcomingDrive != null) {
            appType = ApplicationType.ON_CAMPUS;
        }

        ApplicationStage stage = dto.getStage() != null ? dto.getStage() : ApplicationStage.APPLIED;

        JobApplication app = JobApplication.builder()
                .user(user)
                .upcomingDrive(upcomingDrive)
                .company(company)
                .companyName(dto.getCompanyName() != null ? dto.getCompanyName().trim() : (company != null ? company.getName() : "Unknown Recruiter"))
                .roleTitle(dto.getRoleTitle() != null ? dto.getRoleTitle().trim() : "Software Engineer")
                .ctcOrStipend(dto.getCtcOrStipend() != null && !dto.getCtcOrStipend().isBlank() ? dto.getCtcOrStipend().trim() : null)
                .applicationType(appType)
                .stage(stage)
                .nextEventDate(dto.getNextEventDate())
                .nextEventTitle(dto.getNextEventTitle() != null ? dto.getNextEventTitle().trim() : null)
                .interviewLocation(dto.getInterviewLocation() != null ? dto.getInterviewLocation().trim() : null)
                .notes(dto.getNotes())
                .rating(dto.getRating() != null ? dto.getRating() : 0)
                .build();

        return applicationRepository.save(app);
    }

    /**
     * Updates notes, scheduled round dates, location, and rating.
     */
    @Transactional
    public JobApplication updateDetails(Long userId, ApplicationRequestDto dto) {
        JobApplication app = applicationRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to edit this application");
        }

        if (dto.getCompanyName() != null && !dto.getCompanyName().isBlank()) {
            app.setCompanyName(dto.getCompanyName().trim());
        }
        if (dto.getRoleTitle() != null && !dto.getRoleTitle().isBlank()) {
            app.setRoleTitle(dto.getRoleTitle().trim());
        }
        if (dto.getCtcOrStipend() != null) {
            app.setCtcOrStipend(dto.getCtcOrStipend().trim());
        }
        if (dto.getStage() != null) {
            app.setStage(dto.getStage());
        }
        if (dto.getApplicationType() != null) {
            app.setApplicationType(dto.getApplicationType());
        }
        app.setNextEventDate(dto.getNextEventDate());
        app.setNextEventTitle(dto.getNextEventTitle());
        app.setInterviewLocation(dto.getInterviewLocation());
        app.setNotes(dto.getNotes());
        if (dto.getRating() != null) {
            app.setRating(dto.getRating());
        }

        return applicationRepository.save(app);
    }

    /**
     * Deletes an application.
     */
    @Transactional
    public void deleteApplication(Long userId, Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this application");
        }

        applicationRepository.delete(app);
    }

    /**
     * Automatically syncs when a student registers for an upcoming drive.
     */
    @Transactional
    public void syncFromDriveRegistration(User user, UpcomingDrive drive, String notes) {
        Optional<JobApplication> existing = applicationRepository.findByUserIdAndUpcomingDriveId(user.getId(), drive.getId());
        if (existing.isPresent()) {
            JobApplication app = existing.get();
            if (app.getStage() == ApplicationStage.REJECTED) {
                app.setStage(ApplicationStage.APPLIED);
            }
            if (notes != null && !notes.isBlank()) {
                app.setNotes(notes);
            }
            applicationRepository.save(app);
            return;
        }

        JobApplication newApp = JobApplication.builder()
                .user(user)
                .upcomingDrive(drive)
                .company(drive.getCompany())
                .companyName(drive.getCompanyName())
                .roleTitle(drive.getRolesOffered())
                .ctcOrStipend(drive.getCtcOrStipend())
                .applicationType(ApplicationType.ON_CAMPUS)
                .stage(ApplicationStage.APPLIED)
                .notes(notes)
                .appliedAt(LocalDateTime.now())
                .build();

        applicationRepository.save(newApp);
        log.info("Synced upcoming drive registration to application pipeline for user {} and drive {}", user.getId(), drive.getId());
    }

    /**
     * Automatically syncs when a student withdraws from an upcoming drive.
     */
    @Transactional
    public void syncFromDriveWithdrawal(User user, UpcomingDrive drive) {
        applicationRepository.findByUserIdAndUpcomingDriveId(user.getId(), drive.getId())
                .ifPresent(app -> {
                    app.setStage(ApplicationStage.REJECTED);
                    app.setNotes((app.getNotes() != null ? app.getNotes() + "\n" : "") + "[Withdrawn on " + LocalDateTime.now() + "]");
                    applicationRepository.save(app);
                });
    }

    @Transactional(readOnly = true)
    public Optional<JobApplication> getApplication(Long userId, Long applicationId) {
        return applicationRepository.findById(applicationId)
                .filter(app -> app.getUser().getId().equals(userId));
    }
}
