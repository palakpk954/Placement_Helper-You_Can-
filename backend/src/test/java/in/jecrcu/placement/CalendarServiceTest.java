package in.jecrcu.placement;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.repository.DriveRepository;
import in.jecrcu.placement.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private DriveRepository driveRepository;

    @InjectMocks
    private CalendarService calendarService;

    private Company google;
    private Company amazon;
    private Drive drive2025August;
    private Drive drive2026July;

    @BeforeEach
    void setUp() {
        google = Company.builder().id(1L).name("Google").build();
        amazon = Company.builder().id(2L).name("Amazon").build();

        drive2025August = Drive.builder()
                .id(1L)
                .company(google)
                .driveYear(2025)
                .driveMonth("August")
                .roles("Software Engineer")
                .build();

        drive2026July = Drive.builder()
                .id(2L)
                .company(amazon)
                .driveYear(2026)
                .driveMonth("July")
                .roles("SDE-1")
                .build();
    }

    @Test
    void testGetSeasonalityMatrix() {
        when(driveRepository.findAll()).thenReturn(List.of(drive2025August, drive2026July));

        Map<String, Map<Integer, CalendarService.SeasonalityCell>> matrix =
                calendarService.getSeasonalityMatrix(List.of(2026, 2025, 2024));

        assertNotNull(matrix);
        assertTrue(matrix.containsKey("July"));
        assertTrue(matrix.containsKey("August"));

        // July 2026 should have Amazon (count 1)
        CalendarService.SeasonalityCell july2026 = matrix.get("July").get(2026);
        assertNotNull(july2026);
        assertEquals(1, july2026.getCount());
        assertEquals(List.of("Amazon"), july2026.getCompanyNames());

        // August 2025 should have Google (count 1)
        CalendarService.SeasonalityCell august2025 = matrix.get("August").get(2025);
        assertNotNull(august2025);
        assertEquals(1, august2025.getCount());
        assertEquals(List.of("Google"), august2025.getCompanyNames());

        // August 2024 should have 0
        CalendarService.SeasonalityCell august2024 = matrix.get("August").get(2024);
        assertNotNull(august2024);
        assertEquals(0, august2024.getCount());
    }

    @Test
    void testGetMonthlyDrivesForYear() {
        when(driveRepository.findByDriveYearOrderByDriveMonthAsc(2025))
                .thenReturn(List.of(drive2025August));

        Map<String, List<Drive>> monthlyDrives = calendarService.getMonthlyDrivesForYear(2025);

        assertNotNull(monthlyDrives);
        assertEquals(12, monthlyDrives.size());
        assertEquals(1, monthlyDrives.get("August").size());
        assertEquals(0, monthlyDrives.get("July").size());
    }
}
