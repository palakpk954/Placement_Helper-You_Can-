package in.jecrcu.placement;

import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.entity.enums.Difficulty;
import in.jecrcu.placement.entity.enums.Outcome;
import in.jecrcu.placement.entity.enums.RoundType;
import in.jecrcu.placement.repository.*;
import in.jecrcu.placement.service.ai.DefaultAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UpcomingDriveRepository upcomingDriveRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AlumniProfileRepository alumniProfileRepository;

    @InjectMocks
    private DefaultAiService aiService;

    private Company google;
    private Drive drive;

    @BeforeEach
    void setUp() {
        google = Company.builder()
                .id(1L)
                .name("Google")
                .slug("google")
                .industry("Product & Tech")
                .tier("Super Dream")
                .drives(new ArrayList<>())
                .build();

        Round r1 = Round.builder()
                .id(1L)
                .name("Coding Assessment")
                .roundType(RoundType.CODING)
                .roundNumber(1)
                .difficulty(Difficulty.HARD)
                .build();

        drive = Drive.builder()
                .id(101L)
                .company(google)
                .driveYear(2025)
                .driveMonth("August")
                .roles("Software Engineer")
                .ctcOrStipend("35.00 LPA")
                .minCgpa(BigDecimal.valueOf(8.00))
                .eligibleBranches("CSE, IT")
                .maxBacklogs(0)
                .studentsSelected(4)
                .workMode(in.jecrcu.placement.entity.enums.WorkMode.HYBRID)
                .rounds(List.of(r1))
                .build();

        google.getDrives().add(drive);
    }

    @Test
    void testExperienceSummarizationOfflineWithRealExperiences() {
        Experience exp1 = Experience.builder()
                .id(1L)
                .title("Cracking Google")
                .content("Extensive algorithmic rounds on trees and graphs.")
                .tips("Focus on DP and articulate your thoughts aloud.")
                .mistakesToAvoid("Don't jump straight into code without clarifying constraints.")
                .outcome(Outcome.SELECTED)
                .difficultyRating(5)
                .build();

        Experience exp2 = Experience.builder()
                .id(2L)
                .title("My Google Interview Experience")
                .content("Two technical interviews focused on binary search.")
                .tips("Time complexity must be optimal.")
                .outcome(Outcome.SELECTED)
                .difficultyRating(4)
                .build();

        String summary = aiService.summarizeCompanyExperience("Google", List.of(exp1, exp2));

        assertNotNull(summary);
        assertTrue(summary.contains("AI Synthesis"));
        assertTrue(summary.contains("Google"));
        assertTrue(summary.contains("4.5 / 5.0") || summary.contains("4.5"));
        assertTrue(summary.contains("Focus on DP"));
        assertTrue(summary.contains("Don't jump straight into code"));
    }

    @Test
    void testExperienceSummarizationEmptyList() {
        String summary = aiService.summarizeCompanyExperience("Google", Collections.emptyList());
        assertNotNull(summary);
        assertTrue(summary.contains("No verified student interview experiences"));
    }

    @Test
    void testQuestionClassificationDSA() {
        Map<String, Object> result = aiService.classifyQuestion("Given a binary tree, find maximum path sum between two leaf nodes using recursion");
        assertNotNull(result);
        assertEquals("DSA", result.get("topic"));
        assertTrue(result.get("tags").toString().contains("Binary Trees"));
    }

    @Test
    void testQuestionClassificationDBMS() {
        Map<String, Object> result = aiService.classifyQuestion("Explain ACID properties and transaction isolation levels in SQL database");
        assertNotNull(result);
        assertEquals("DBMS", result.get("topic"));
        assertTrue(result.get("tags").toString().contains("ACID Transactions") || result.get("tags").toString().contains("SQL"));
    }

    @Test
    void testQuestionClassificationOS() {
        Map<String, Object> result = aiService.classifyQuestion("Explain deadlock detection, recovery, and prevention methods in modern operating systems");
        assertNotNull(result);
        assertEquals("Operating Systems", result.get("topic"));
        assertTrue(result.get("tags").toString().contains("Deadlocks"));
    }

    @Test
    void testQuestionClassificationHR() {
        Map<String, Object> result = aiService.classifyQuestion("Tell me about a time you faced a difficult conflict with a team member and how you resolved it");
        assertNotNull(result);
        assertEquals("HR & Behavioral", result.get("topic"));
        assertEquals("EASY", result.get("difficulty"));
    }

    @Test
    void testAnswerPrepQuestionGroundedCompany() {
        when(companyRepository.findAll()).thenReturn(List.of(google));
        when(upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc()).thenReturn(Collections.emptyList());
        when(alumniProfileRepository.findByCompanyNameIgnoreCaseAndVisibleTrue("Google")).thenReturn(Collections.emptyList());

        String answer = aiService.answerPrepQuestion("What is the package and eligibility for Google?");

        assertNotNull(answer);
        assertTrue(answer.contains("Google Placement Intelligence"));
        assertTrue(answer.contains("35.00 LPA"));
        assertTrue(answer.contains("Min CGPA"));
        assertTrue(answer.contains("Coding Assessment"));
    }

    @Test
    void testAnswerPrepQuestionUnknownQueryReturnsStrictFallback() {
        when(companyRepository.findAll()).thenReturn(List.of(google));

        String answer = aiService.answerPrepQuestion("What is the weather in Paris?");

        assertNotNull(answer);
        assertTrue(answer.contains("I don't have verified data on that in our campus recruitment records"));
    }

    @Test
    void testAnswerPrepQuestionGratitudeWithTypo() {
        when(companyRepository.findAll()).thenReturn(List.of(google));

        // Testing the exact user phrase with typo "muhc"
        String answer1 = aiService.answerPrepQuestion("ok thank you so muhc");
        assertNotNull(answer1);
        assertTrue(answer1.contains("You're very welcome"));

        String answer2 = aiService.answerPrepQuestion("thanks a lot!");
        assertNotNull(answer2);
        assertTrue(answer2.contains("You're very welcome"));
    }

    @Test
    void testAnswerPrepQuestionAcknowledgment() {
        when(companyRepository.findAll()).thenReturn(List.of(google));

        String answer1 = aiService.answerPrepQuestion("ok");
        assertNotNull(answer1);
        assertTrue(answer1.contains("Glad to help"));

        String answer2 = aiService.answerPrepQuestion("got it");
        assertNotNull(answer2);
        assertTrue(answer2.contains("Glad to help"));
    }

    @Test
    void testAnswerPrepQuestionGreeting() {
        when(companyRepository.findAll()).thenReturn(List.of(google));

        String answer1 = aiService.answerPrepQuestion("hi there");
        assertNotNull(answer1);
        assertTrue(answer1.contains("You_can Campus Placement Assistant"));

        String answer2 = aiService.answerPrepQuestion("who are you");
        assertNotNull(answer2);
        assertTrue(answer2.contains("You_can Campus Placement Assistant"));
    }

    @Test
    void testAnswerPrepQuestionFarewell() {
        when(companyRepository.findAll()).thenReturn(List.of(google));

        String answer = aiService.answerPrepQuestion("goodbye");
        assertNotNull(answer);
        assertTrue(answer.contains("Goodbye and have a wonderful day"));
    }
}
