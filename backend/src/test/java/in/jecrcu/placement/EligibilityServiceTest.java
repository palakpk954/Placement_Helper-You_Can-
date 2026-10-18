package in.jecrcu.placement;

import in.jecrcu.placement.entity.UpcomingDrive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import in.jecrcu.placement.service.EligibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {

    @Mock
    private UpcomingDriveRepository upcomingDriveRepository;

    @InjectMocks
    private EligibilityService eligibilityService;

    private UpcomingDrive sampleUpcoming;

    @BeforeEach
    void setUp() {
        sampleUpcoming = UpcomingDrive.builder()
                .id(1L)
                .companyName("Microsoft")
                .rolesOffered("SDE-1")
                .minCgpa(BigDecimal.valueOf(8.00))
                .eligibleBranches("CSE, IT, ECE")
                .maxBacklogs(0)
                .ctcOrStipend("48.00 LPA")
                .driveDate(LocalDate.now().plusDays(10))
                .deadline(LocalDateTime.now().plusDays(5))
                .active(true)
                .build();
    }

    @Test
    void testEligibleStudentPassesCheck() {
        User eligibleStudent = User.builder()
                .branch("CSE")
                .cgpa(BigDecimal.valueOf(8.50))
                .activeBacklogs(0)
                .build();

        assertTrue(eligibilityService.isEligibleForUpcoming(eligibleStudent, sampleUpcoming));
    }

    @Test
    void testCgpaBelowCutoffFailsWithReason() {
        var result = eligibilityService.checkUpcomingEligibility(
                "CSE", BigDecimal.valueOf(7.20), 0, sampleUpcoming);

        assertFalse(result.isEligible());
        assertTrue(result.getExclusionReasons().stream().anyMatch(r -> r.contains("below cutoff")));
    }

    @Test
    void testIneligibleBranchFailsWithReason() {
        var result = eligibilityService.checkUpcomingEligibility(
                "Civil", BigDecimal.valueOf(9.00), 0, sampleUpcoming);

        assertFalse(result.isEligible());
        assertTrue(result.getExclusionReasons().stream().anyMatch(r -> r.contains("Branch 'Civil' is not eligible")));
    }

    @Test
    void testActiveBacklogsFailWithReason() {
        var result = eligibilityService.checkUpcomingEligibility(
                "CSE", BigDecimal.valueOf(8.50), 2, sampleUpcoming);

        assertFalse(result.isEligible());
        assertTrue(result.getExclusionReasons().stream().anyMatch(r -> r.contains("Active backlogs")));
    }
}
