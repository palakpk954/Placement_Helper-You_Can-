package in.jecrcu.placement.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.enums.JobType;
import in.jecrcu.placement.entity.enums.WorkMode;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.DriveRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final CompanyRepository companyRepository;
    private final DriveRepository driveRepository;

    @Getter
    @Setter
    public static class ImportRowDto {
        private int rowNumber;
        private String companyName;
        private String industry;
        private Integer year;
        private String month;
        private String roles;
        private String ctc;
        private BigDecimal minCgpa;
        private String branches;
        private Integer selections;
        private String jobType;
        private String workMode;
        private List<String> errors = new ArrayList<>();

        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    @Getter
    public static class ImportPreviewResult {
        private final List<ImportRowDto> validRows = new ArrayList<>();
        private final List<ImportRowDto> invalidRows = new ArrayList<>();
        private int totalRows;

        public boolean hasErrors() {
            return !invalidRows.isEmpty();
        }
    }

    public ImportPreviewResult parseAndValidate(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<ImportRowDto> parsedRows;

        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            parsedRows = parseExcel(file.getInputStream());
        } else {
            parsedRows = parseCsv(file.getInputStream());
        }

        ImportPreviewResult result = new ImportPreviewResult();
        result.totalRows = parsedRows.size();

        for (ImportRowDto row : parsedRows) {
            validateRow(row);
            if (row.isValid()) {
                result.getValidRows().add(row);
            } else {
                result.getInvalidRows().add(row);
            }
        }

        return result;
    }

    private void validateRow(ImportRowDto row) {
        if (row.getCompanyName() == null || row.getCompanyName().isBlank()) {
            row.getErrors().add("Company Name is required.");
        }
        if (row.getYear() == null || row.getYear() < 2000 || row.getYear() > 2035) {
            row.getErrors().add("Valid Year between 2000 and 2035 is required.");
        }
        if (row.getMonth() == null || row.getMonth().isBlank()) {
            row.getErrors().add("Month is required (e.g. August, September).");
        }
        if (row.getRoles() == null || row.getRoles().isBlank()) {
            row.getErrors().add("Roles offered is required.");
        }
        if (row.getCtc() == null || row.getCtc().isBlank()) {
            row.getErrors().add("CTC or Stipend is required.");
        }
        if (row.getMinCgpa() != null && (row.getMinCgpa().compareTo(BigDecimal.ZERO) < 0 || row.getMinCgpa().compareTo(BigDecimal.TEN) > 0)) {
            row.getErrors().add("Min CGPA must be between 0.0 and 10.0.");
        }
    }

    private List<ImportRowDto> parseCsv(InputStream in) throws Exception {
        List<ImportRowDto> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))) {
            List<String[]> allLines = reader.readAll();
            if (allLines.isEmpty()) return rows;

            // Skip header line
            for (int i = 1; i < allLines.size(); i++) {
                String[] line = allLines.get(i);
                if (line.length == 0 || (line.length == 1 && line[0].isBlank())) continue;

                ImportRowDto row = new ImportRowDto();
                row.setRowNumber(i + 1);
                row.setCompanyName(getVal(line, 0));
                row.setIndustry(getVal(line, 1, "IT Services"));
                row.setYear(parseYear(getVal(line, 2)));
                row.setMonth(getVal(line, 3));
                row.setRoles(getVal(line, 4));
                row.setCtc(getVal(line, 5));
                row.setMinCgpa(parseCgpa(getVal(line, 6)));
                row.setBranches(getVal(line, 7, "CSE, IT, ECE"));
                row.setSelections(parseInt(getVal(line, 8), 0));
                row.setJobType(getVal(line, 9, "PLACEMENT"));
                row.setWorkMode(getVal(line, 10, "HYBRID"));
                rows.add(row);
            }
        }
        return rows;
    }

    private List<ImportRowDto> parseExcel(InputStream in) throws Exception {
        List<ImportRowDto> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String compName = formatter.formatCellValue(row.getCell(0)).trim();
                if (compName.isBlank()) continue;

                ImportRowDto dto = new ImportRowDto();
                dto.setRowNumber(r + 1);
                dto.setCompanyName(compName);
                dto.setIndustry(defaultIfBlank(formatter.formatCellValue(row.getCell(1)), "IT Services"));
                dto.setYear(parseYear(formatter.formatCellValue(row.getCell(2))));
                dto.setMonth(formatter.formatCellValue(row.getCell(3)));
                dto.setRoles(formatter.formatCellValue(row.getCell(4)));
                dto.setCtc(formatter.formatCellValue(row.getCell(5)));
                dto.setMinCgpa(parseCgpa(formatter.formatCellValue(row.getCell(6))));
                dto.setBranches(defaultIfBlank(formatter.formatCellValue(row.getCell(7)), "CSE, IT, ECE"));
                dto.setSelections(parseInt(formatter.formatCellValue(row.getCell(8)), 0));
                dto.setJobType(defaultIfBlank(formatter.formatCellValue(row.getCell(9)), "PLACEMENT"));
                dto.setWorkMode(defaultIfBlank(formatter.formatCellValue(row.getCell(10)), "HYBRID"));

                rows.add(dto);
            }
        }
        return rows;
    }

    @Transactional
    public int executeImport(List<ImportRowDto> validRows) {
        int count = 0;
        for (ImportRowDto row : validRows) {
            String name = row.getCompanyName().trim();
            String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (slug.isBlank()) slug = "comp-" + System.currentTimeMillis();

            String finalSlug = slug;
            Company company = companyRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Company c = Company.builder()
                                .name(name)
                                .slug(finalSlug)
                                .industry(row.getIndustry() != null ? row.getIndustry() : "IT Services")
                                .tier("Recruiter")
                                .build();
                        return companyRepository.save(c);
                    });

            JobType jType = JobType.PLACEMENT;
            try {
                if (row.getJobType() != null) jType = JobType.valueOf(row.getJobType().toUpperCase());
            } catch (Exception ignored) {}

            WorkMode wMode = WorkMode.HYBRID;
            try {
                if (row.getWorkMode() != null) wMode = WorkMode.valueOf(row.getWorkMode().toUpperCase());
            } catch (Exception ignored) {}

            Drive drive = Drive.builder()
                    .company(company)
                    .driveYear(row.getYear())
                    .driveMonth(row.getMonth())
                    .roles(row.getRoles())
                    .ctcOrStipend(row.getCtc())
                    .minCgpa(row.getMinCgpa() != null ? row.getMinCgpa() : BigDecimal.valueOf(6.00))
                    .eligibleBranches(row.getBranches())
                    .studentsSelected(row.getSelections())
                    .jobType(jType)
                    .workMode(wMode)
                    .conversionOffered(true)
                    .build();

            driveRepository.save(drive);
            count++;
        }
        log.info("Bulk import committed: {} drives imported successfully.", count);
        return count;
    }

    private String getVal(String[] arr, int index) {
        return (index < arr.length && arr[index] != null) ? arr[index].trim() : "";
    }

    private String getVal(String[] arr, int index, String fallback) {
        String val = getVal(arr, index);
        return val.isBlank() ? fallback : val;
    }

    private String defaultIfBlank(String val, String fallback) {
        return (val == null || val.isBlank()) ? fallback : val.trim();
    }

    private Integer parseYear(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseCgpa(String str) {
        try {
            return new BigDecimal(str.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return BigDecimal.valueOf(6.00);
        }
    }

    private Integer parseInt(String str, int fallback) {
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return fallback;
        }
    }
}
