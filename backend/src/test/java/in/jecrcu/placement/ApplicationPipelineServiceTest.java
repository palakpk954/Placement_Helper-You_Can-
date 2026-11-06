package in.jecrcu.placement;

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
import in.jecrcu.placement.service.ApplicationPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationPipelineServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UpcomingDriveRepository upcomingDriveRepository;

    @InjectMocks
    private ApplicationPipelineService pipelineService;

    private User student;
    private JobApplication app1;
    private JobApplication app2;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(2L)
                .email("rahul.sharma@jecrcu.edu.in")
                .fullName("Rahul Sharma")
                .build();

        app1 = JobApplication.builder()
                .id(101L)
                .user(student)
                .companyName("Microsoft")
                .roleTitle("SDE-1")
                .applicationType(ApplicationType.ON_CAMPUS)
                .stage(ApplicationStage.OA_ROUND)
                .appliedAt(LocalDateTime.now())
                .build();

        app2 = JobApplication.builder()
                .id(102L)
                .user(student)
                .companyName("Atlassian")
                .roleTitle("Intern")
                .applicationType(ApplicationType.OFF_CAMPUS)
                .stage(ApplicationStage.OFFERED)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetKanbanBoard_GroupsApplicationsByStage() {
        when(applicationRepository.searchApplications(2L, null, null))
                .thenReturn(List.of(app1, app2));

        Map<ApplicationStage, List<JobApplication>> board = pipelineService.getKanbanBoard(2L, "ALL", null);

        assertNotNull(board);
        assertEquals(ApplicationStage.values().length, board.size());
        assertEquals(1, board.get(ApplicationStage.OA_ROUND).size());
        assertEquals("Microsoft", board.get(ApplicationStage.OA_ROUND).get(0).getCompanyName());
        assertEquals(1, board.get(ApplicationStage.OFFERED).size());
        assertEquals("Atlassian", board.get(ApplicationStage.OFFERED).get(0).getCompanyName());
        assertTrue(board.get(ApplicationStage.APPLIED).isEmpty());
    }

    @Test
    void testGetPipelineStats() {
        when(applicationRepository.countByUserId(2L)).thenReturn(5L);
        when(applicationRepository.countByUserIdAndStage(2L, ApplicationStage.APPLIED)).thenReturn(2L);
        when(applicationRepository.countByUserIdAndStage(2L, ApplicationStage.OA_ROUND)).thenReturn(1L);
        when(applicationRepository.countByUserIdAndStage(2L, ApplicationStage.TECH_INTERVIEW)).thenReturn(1L);
        when(applicationRepository.countByUserIdAndStage(2L, ApplicationStage.HR_ROUND)).thenReturn(0L);
        when(applicationRepository.countByUserIdAndStage(2L, ApplicationStage.OFFERED)).thenReturn(1L);
        when(applicationRepository.countByUserIdAndStage(2L, ApplicationStage.REJECTED)).thenReturn(0L);

        PipelineStatsDto stats = pipelineService.getPipelineStats(2L);

        assertNotNull(stats);
        assertEquals(5, stats.getTotalApplications());
        assertEquals(4, stats.getActiveInPipeline()); // applied(2) + oa(1) + tech(1) + hr(0)
        assertEquals(1, stats.getInterviewsCount()); // tech(1) + hr(0)
        assertEquals(1, stats.getOfferedCount());
        assertEquals(20, stats.getOfferRate()); // 1 / 5 = 20%
    }

    @Test
    void testUpdateStage_Success() {
        when(applicationRepository.findById(101L)).thenReturn(Optional.of(app1));
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(i -> i.getArgument(0));

        JobApplication updated = pipelineService.updateStage(2L, 101L, ApplicationStage.TECH_INTERVIEW);

        assertNotNull(updated);
        assertEquals(ApplicationStage.TECH_INTERVIEW, updated.getStage());
        verify(applicationRepository).save(app1);
    }

    @Test
    void testUpdateStage_UnauthorizedUser_ThrowsException() {
        when(applicationRepository.findById(101L)).thenReturn(Optional.of(app1));

        assertThrows(SecurityException.class, () ->
                pipelineService.updateStage(999L, 101L, ApplicationStage.OFFERED)
        );
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreateApplication_ManualEntry() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(companyRepository.findByNameIgnoreCase("Google")).thenReturn(Optional.of(Company.builder().id(1L).name("Google").build()));
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(i -> i.getArgument(0));

        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .companyName("Google")
                .roleTitle("Software Engineer")
                .ctcOrStipend("32 LPA")
                .applicationType(ApplicationType.OFF_CAMPUS)
                .stage(ApplicationStage.APPLIED)
                .build();

        JobApplication saved = pipelineService.createApplication(2L, dto);

        assertNotNull(saved);
        assertEquals("Google", saved.getCompanyName());
        assertEquals("Software Engineer", saved.getRoleTitle());
        assertEquals(ApplicationStage.APPLIED, saved.getStage());
        assertEquals(1L, saved.getCompany().getId());
    }

    @Test
    void testSyncFromDriveRegistration() {
        UpcomingDrive drive = UpcomingDrive.builder()
                .id(10L)
                .companyName("Amazon")
                .rolesOffered("SDE-1")
                .ctcOrStipend("45 LPA")
                .build();

        when(applicationRepository.findByUserIdAndUpcomingDriveId(2L, 10L)).thenReturn(Optional.empty());
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(i -> i.getArgument(0));

        pipelineService.syncFromDriveRegistration(student, drive, "Applied with updated resume");

        verify(applicationRepository).save(argThat(app ->
                "Amazon".equals(app.getCompanyName()) &&
                app.getStage() == ApplicationStage.APPLIED &&
                app.getApplicationType() == ApplicationType.ON_CAMPUS
        ));
    }
}
