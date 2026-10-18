package in.jecrcu.placement;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.DifficultyVoteRepository;
import in.jecrcu.placement.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DifficultyVoteRepository difficultyVoteRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company google;
    private Company tcs;

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

        Drive gDrive = Drive.builder()
                .id(1L)
                .company(google)
                .driveYear(2025)
                .driveMonth("August")
                .roles("Software Engineer")
                .ctcOrStipend("35.00 LPA")
                .eligibleBranches("CSE, IT")
                .build();
        google.getDrives().add(gDrive);

        tcs = Company.builder()
                .id(2L)
                .name("TCS")
                .slug("tcs")
                .industry("IT Services")
                .tier("Mass Recruiters")
                .drives(new ArrayList<>())
                .build();

        Drive tDrive = Drive.builder()
                .id(2L)
                .company(tcs)
                .driveYear(2025)
                .driveMonth("July")
                .roles("TCS Digital")
                .ctcOrStipend("7.50 LPA")
                .eligibleBranches("CSE, IT, ECE, EE, ME, Civil")
                .build();
        tcs.getDrives().add(tDrive);
    }

    @Test
    void testFilterByKeyword() {
        when(companyRepository.findAll()).thenReturn(List.of(google, tcs));
        when(difficultyVoteRepository.findAverageRatingByCompanyId(anyLong())).thenReturn(4.5);

        List<Company> results = companyService.filterCompanies("Google", null, null, null, null, null, null, "recent");
        assertEquals(1, results.size());
        assertEquals("Google", results.get(0).getName());
    }

    @Test
    void testFilterByIndustry() {
        when(companyRepository.findAll()).thenReturn(List.of(google, tcs));
        when(difficultyVoteRepository.findAverageRatingByCompanyId(anyLong())).thenReturn(4.0);

        List<Company> results = companyService.filterCompanies(null, null, null, "IT Services", null, null, null, "recent");
        assertEquals(1, results.size());
        assertEquals("TCS", results.get(0).getName());
    }

    @Test
    void testExtractMaxCtc() {
        double maxGoogle = companyService.extractMaxCtc(google);
        assertEquals(35.0, maxGoogle);

        double maxTcs = companyService.extractMaxCtc(tcs);
        assertEquals(7.5, maxTcs);
    }
}
