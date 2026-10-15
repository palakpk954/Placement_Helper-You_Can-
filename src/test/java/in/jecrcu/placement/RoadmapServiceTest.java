package in.jecrcu.placement;

import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.RoadmapItemRepository;
import in.jecrcu.placement.repository.RoadmapRepository;
import in.jecrcu.placement.service.RoadmapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private RoadmapItemRepository roadmapItemRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private RoadmapService roadmapService;

    private User user;
    private Company company;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("student@jecrcu.edu.in").build();
        company = Company.builder().id(10L).name("Capgemini").build();
    }

    @Test
    void testGenerateRoadmap_Success() {
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(roadmapRepository.findByUserIdAndCompanyId(1L, 10L)).thenReturn(Optional.empty());
        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(roadmapItemRepository.save(any(RoadmapItem.class))).thenAnswer(invocation -> {
            RoadmapItem item = invocation.getArgument(0);
            item.setId(200L + item.getWeekNumber());
            return item;
        });

        Roadmap roadmap = roadmapService.generateRoadmap(user, 10L, 8);

        assertNotNull(roadmap);
        assertEquals(8, roadmap.getWeeks());
        assertEquals(8, roadmap.getTotalWeeks());
        assertEquals("Capgemini", roadmap.getCompany().getName());
        assertEquals(8, roadmap.getItems().size());
        assertEquals(0, roadmap.getCompletedCount());
        assertEquals(0, roadmap.getProgressPercentage());
        assertFalse(roadmap.isAllCompleted());

        RoadmapItem item1 = roadmap.getItems().get(0);
        assertNotNull(item1.getDescription());
        assertTrue(item1.getDescription().contains("practice problems"));
    }

    @Test
    void testToggleItemCompletion() {
        RoadmapItem item = RoadmapItem.builder()
                .id(50L)
                .completed(false)
                .weekNumber(1)
                .topic("Arrays")
                .targetQuestions(5)
                .build();

        when(roadmapItemRepository.findById(50L)).thenReturn(Optional.of(item));
        when(roadmapItemRepository.save(any(RoadmapItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean status1 = roadmapService.toggleItemCompletion(50L);
        assertTrue(status1);
        assertTrue(item.getCompleted());

        boolean status2 = roadmapService.toggleItemCompletion(50L);
        assertFalse(status2);
        assertFalse(item.getCompleted());
    }

    @Test
    void testRoadmapProgressCalculations() {
        Roadmap roadmap = Roadmap.builder()
                .id(1L)
                .weeks(4)
                .items(new ArrayList<>())
                .build();

        RoadmapItem i1 = RoadmapItem.builder().id(1L).weekNumber(1).completed(true).build();
        RoadmapItem i2 = RoadmapItem.builder().id(2L).weekNumber(2).completed(true).build();
        RoadmapItem i3 = RoadmapItem.builder().id(3L).weekNumber(3).completed(false).build();
        RoadmapItem i4 = RoadmapItem.builder().id(4L).weekNumber(4).completed(false).build();

        roadmap.setItems(List.of(i1, i2, i3, i4));

        assertEquals(2, roadmap.getCompletedCount());
        assertEquals(50, roadmap.getProgressPercentage());
        assertFalse(roadmap.isAllCompleted());

        i3.setCompleted(true);
        i4.setCompleted(true);
        assertEquals(4, roadmap.getCompletedCount());
        assertEquals(100, roadmap.getProgressPercentage());
        assertTrue(roadmap.isAllCompleted());
    }
}
