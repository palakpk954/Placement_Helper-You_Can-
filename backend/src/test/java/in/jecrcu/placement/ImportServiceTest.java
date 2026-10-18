package in.jecrcu.placement;

import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.DriveRepository;
import in.jecrcu.placement.service.ImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DriveRepository driveRepository;

    @InjectMocks
    private ImportService importService;

    @Test
    void testParseAndValidateValidCsv() throws Exception {
        String csvContent = "Company Name,Industry,Drive Year,Drive Month,Roles,CTC,Min CGPA,Eligible Branches,Selections,Job Type,Work Mode\n" +
                "Atlassian,Product & Tech,2025,October,Software Engineer,40.0 LPA,8.0,CSE IT,4,PLACEMENT,HYBRID\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "drives.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        ImportService.ImportPreviewResult result = importService.parseAndValidate(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getValidRows().size());
        assertEquals(0, result.getInvalidRows().size());
        assertEquals("Atlassian", result.getValidRows().get(0).getCompanyName());
    }

    @Test
    void testParseAndValidateInvalidRowFailsValidation() throws Exception {
        // Missing company name and invalid year
        String csvContent = "Company Name,Industry,Drive Year,Drive Month,Roles,CTC,Min CGPA,Eligible Branches,Selections,Job Type,Work Mode\n" +
                ",IT Services,1890,October,SDE,10 LPA,12.5,CSE,0,PLACEMENT,HYBRID\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        ImportService.ImportPreviewResult result = importService.parseAndValidate(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getValidRows().size());
        assertEquals(1, result.getInvalidRows().size());
        assertTrue(result.getInvalidRows().get(0).getErrors().size() >= 2);
    }
}
