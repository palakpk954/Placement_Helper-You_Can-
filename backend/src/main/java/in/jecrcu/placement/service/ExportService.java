package in.jecrcu.placement.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.repository.DriveRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final DriveRepository driveRepository;

    public byte[] generateExcelReport(Integer year) throws Exception {
        List<Drive> drives = year != null ? driveRepository.findByDriveYearOrderByDriveMonthAsc(year) : driveRepository.findAllOrdered();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Placement Report " + (year != null ? year : "All Years"));

            // Styling
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Header row
            String[] headers = {"Company", "Year", "Month", "Roles Offered", "CTC / Stipend", "Min CGPA", "Eligible Branches", "Students Placed", "Job Type", "Work Mode"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            int totalSelections = 0;
            for (Drive d : drives) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getCompany().getName());
                row.createCell(1).setCellValue(d.getDriveYear());
                row.createCell(2).setCellValue(d.getDriveMonth());
                row.createCell(3).setCellValue(d.getRoles());
                row.createCell(4).setCellValue(d.getCtcOrStipend());
                row.createCell(5).setCellValue(d.getMinCgpa() != null ? d.getMinCgpa().doubleValue() : 6.0);
                row.createCell(6).setCellValue(d.getEligibleBranches());
                int sel = d.getStudentsSelected() != null ? d.getStudentsSelected() : 0;
                totalSelections += sel;
                row.createCell(7).setCellValue(sel);
                row.createCell(8).setCellValue(d.getJobType().name());
                row.createCell(9).setCellValue(d.getWorkMode() != null ? d.getWorkMode().name() : "HYBRID");
            }

            // Summary row
            org.apache.poi.ss.usermodel.Row summaryRow = sheet.createRow(rowIdx + 1);
            org.apache.poi.ss.usermodel.Cell sumCellLabel = summaryRow.createCell(6);
            sumCellLabel.setCellValue("TOTAL OFFERS:");
            org.apache.poi.ss.usermodel.Cell sumCellVal = summaryRow.createCell(7);
            sumCellVal.setCellValue(totalSelections);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generatePdfReport(Integer year) throws Exception {
        List<Drive> drives = year != null ? driveRepository.findByDriveYearOrderByDriveMonthAsc(year) : driveRepository.findAllOrdered();

        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 34, 64));
        Paragraph title = new Paragraph("JECRC UNIVERSITY — CAMPUS PLACEMENT ANNUAL REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Paragraph sub = new Paragraph("Academic Recruitment Drive Summary " + (year != null ? ("(Year " + year + ")") : "(All Historical Records)"), subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(15);
        document.add(sub);

        // Table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3.0f, 1.2f, 1.5f, 3.5f, 2.5f, 1.2f, 2.5f, 1.2f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        String[] headers = {"Company", "Year", "Month", "Roles", "Package (CTC)", "Cutoff", "Branches", "Placed"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(24, 76, 120));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        int totalOffers = 0;

        for (Drive d : drives) {
            table.addCell(new PdfPCell(new Phrase(d.getCompany().getName(), rowFont)));
            table.addCell(new PdfPCell(new Phrase(d.getDriveYear().toString(), rowFont)));
            table.addCell(new PdfPCell(new Phrase(d.getDriveMonth(), rowFont)));
            table.addCell(new PdfPCell(new Phrase(d.getRoles(), rowFont)));
            table.addCell(new PdfPCell(new Phrase(d.getCtcOrStipend(), rowFont)));
            table.addCell(new PdfPCell(new Phrase(d.getMinCgpa() != null ? d.getMinCgpa().toString() : "6.0", rowFont)));
            table.addCell(new PdfPCell(new Phrase(d.getEligibleBranches(), rowFont)));
            int sel = d.getStudentsSelected() != null ? d.getStudentsSelected() : 0;
            totalOffers += sel;
            table.addCell(new PdfPCell(new Phrase(String.valueOf(sel), rowFont)));
        }

        document.add(table);

        Paragraph summary = new Paragraph("\nTotal Recruiters: " + drives.size() + " | Total Placements / Offers Generated: " + totalOffers, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 34, 64)));
        summary.setAlignment(Element.ALIGN_RIGHT);
        document.add(summary);

        document.close();
        return out.toByteArray();
    }
}
