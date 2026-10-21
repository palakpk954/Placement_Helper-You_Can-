package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.*;
import in.jecrcu.placement.entity.enums.JobType;
import in.jecrcu.placement.entity.enums.WorkMode;
import in.jecrcu.placement.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

import in.jecrcu.placement.repository.UpcomingDriveRepository;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExperienceService experienceService;
    private final CompanyService companyService;
    private final DriveService driveService;
    private final ReportService reportService;
    private final AuditService auditService;
    private final ImportService importService;
    private final ExportService exportService;
    private final StatsService statsService;
    private final UserService userService;
    private final UpcomingDriveRepository upcomingDriveRepository;
    private final NotificationService notificationService;
    private final OpportunityService opportunityService;

    // In-memory cache for import preview before user commits
    private ImportService.ImportPreviewResult cachedImportPreview;

    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("pendingExperiences", experienceService.getPendingExperiences());
        model.addAttribute("openReports", reportService.getOpenReports());
        model.addAttribute("overallStats", statsService.getOverallStats());
        model.addAttribute("recentLogs", auditService.getAllLogs().stream().limit(8).toList());

        return "admin/index";
    }

    // ==========================================
    // 1. Submissions Approval Queue
    // ==========================================
    @GetMapping("/approvals")
    public String approvalsQueue(Model model) {
        List<Experience> pending = experienceService.getPendingExperiences();
        List<Long> duplicateFlaggedIds = pending.stream()
                .filter(e -> e.getDrive() != null && experienceService.isPossibleSpamOrDuplicate(e.getDrive().getId(), e.getContent()))
                .map(Experience::getId)
                .toList();
        model.addAttribute("pendingExperiences", pending);
        model.addAttribute("duplicateFlaggedIds", duplicateFlaggedIds);
        return "admin/approvals";
    }

    @PostMapping("/approvals/{id}/approve")
    public String approveExperience(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        Experience exp = experienceService.findById(id).orElse(null);
        experienceService.approveExperience(id);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "APPROVE_EXPERIENCE", "Experience", id, "PENDING", "APPROVED");

        if (exp != null && exp.getAuthor() != null) {
            String compName = exp.getDrive() != null && exp.getDrive().getCompany() != null ? exp.getDrive().getCompany().getName() : "Company";
            notificationService.notifySubmissionOutcome(exp.getAuthor(), compName, true, null);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Experience #" + id + " has been approved and published!");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/approvals/{id}/reject")
    public String rejectExperience(
            @PathVariable("id") Long id,
            @RequestParam("reason") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        Experience exp = experienceService.findById(id).orElse(null);
        experienceService.rejectExperience(id, reason);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "REJECT_EXPERIENCE", "Experience", id, "PENDING", "REJECTED (" + reason + ")");

        if (exp != null && exp.getAuthor() != null) {
            String compName = exp.getDrive() != null && exp.getDrive().getCompany() != null ? exp.getDrive().getCompany().getName() : "Company";
            notificationService.notifySubmissionOutcome(exp.getAuthor(), compName, false, reason);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Experience #" + id + " rejected.");
        return "redirect:/admin/approvals";
    }

    // ==========================================
    // 2. Company & Drive Management (CRUD)
    // ==========================================
    @GetMapping("/companies")
    public String manageCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/companies";
    }

    @PostMapping("/companies/save")
    public String saveCompany(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("name") String name,
            @RequestParam("industry") String industry,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "website", required = false) String website,
            @RequestParam(value = "tier", defaultValue = "Mass Recruiters") String tier,
            @RequestParam(value = "logoUrl", required = false) String logoUrl,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        Company company = (id != null) ? companyService.findById(id).orElse(new Company()) : new Company();
        company.setName(name.trim());
        company.setSlug(slug);
        company.setIndustry(industry.trim());
        company.setDescription(description);
        company.setWebsite(website);
        company.setTier(tier);
        company.setLogoUrl(logoUrl);

        companyService.saveCompany(company);

        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, (id != null ? "UPDATE_COMPANY" : "CREATE_COMPANY"), "Company", company.getId(), null, name);

        redirectAttributes.addFlashAttribute("successMessage", "Company '" + name + "' saved successfully.");
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/{id}/delete")
    public String deleteCompany(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        companyService.deleteCompany(id);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "DELETE_COMPANY", "Company", id, null, null);

        redirectAttributes.addFlashAttribute("successMessage", "Company deleted.");
        return "redirect:/admin/companies";
    }

    @GetMapping("/drives")
    public String manageDrives(Model model) {
        model.addAttribute("drives", driveService.getAllDrives());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/drives";
    }

    @PostMapping("/drives/save")
    public String saveDrive(
            @RequestParam("companyId") Long companyId,
            @RequestParam("driveYear") Integer driveYear,
            @RequestParam("driveMonth") String driveMonth,
            @RequestParam("roles") String roles,
            @RequestParam("ctcOrStipend") String ctcOrStipend,
            @RequestParam(value = "minCgpa", defaultValue = "6.0") BigDecimal minCgpa,
            @RequestParam("eligibleBranches") String eligibleBranches,
            @RequestParam(value = "studentsSelected", defaultValue = "0") Integer studentsSelected,
            @RequestParam(value = "jobType", defaultValue = "PLACEMENT") String jobTypeStr,
            @RequestParam(value = "workMode", defaultValue = "HYBRID") String workModeStr,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        Company company = companyService.findById(companyId).orElseThrow();
        JobType jobType = JobType.PLACEMENT;
        try { jobType = JobType.valueOf(jobTypeStr); } catch (Exception ignored) {}

        WorkMode workMode = WorkMode.HYBRID;
        try { workMode = WorkMode.valueOf(workModeStr); } catch (Exception ignored) {}

        Drive drive = Drive.builder()
                .company(company)
                .driveYear(driveYear)
                .driveMonth(driveMonth)
                .roles(roles)
                .ctcOrStipend(ctcOrStipend)
                .minCgpa(minCgpa)
                .eligibleBranches(eligibleBranches)
                .studentsSelected(studentsSelected)
                .jobType(jobType)
                .workMode(workMode)
                .conversionOffered(true)
                .build();

        driveService.saveDrive(drive);

        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "CREATE_DRIVE", "Drive", drive.getId(), null, company.getName() + " (" + driveYear + ")");

        redirectAttributes.addFlashAttribute("successMessage", "Drive for " + company.getName() + " added successfully.");
        return "redirect:/admin/drives";
    }

    // ==========================================
    // 2.1 Upcoming Drives Management (UPD-1)
    // ==========================================
    @GetMapping("/upcoming-drives")
    public String manageUpcomingDrives(Model model) {
        model.addAttribute("upcomingDrives", upcomingDriveRepository.findAll());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/upcoming-drives";
    }

    @PostMapping("/upcoming-drives/save")
    public String saveUpcomingDrive(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam("companyName") String companyName,
            @RequestParam("driveDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate driveDate,
            @RequestParam("rolesOffered") String rolesOffered,
            @RequestParam(value = "minCgpa", defaultValue = "6.00") BigDecimal minCgpa,
            @RequestParam("eligibleBranches") String eligibleBranches,
            @RequestParam(value = "maxBacklogs", defaultValue = "0") Integer maxBacklogs,
            @RequestParam("ctcOrStipend") String ctcOrStipend,
            @RequestParam(value = "registrationLink", required = false) String registrationLink,
            @RequestParam("deadline") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime deadline,
            @RequestParam(value = "active", defaultValue = "true") boolean active,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        Company company = (companyId != null) ? companyService.findById(companyId).orElse(null) : null;
        UpcomingDrive drive = (id != null) ? upcomingDriveRepository.findById(id).orElse(new UpcomingDrive()) : new UpcomingDrive();
        drive.setCompany(company);
        drive.setCompanyName(company != null ? company.getName() : companyName.trim());
        drive.setDriveDate(driveDate);
        drive.setRolesOffered(rolesOffered.trim());
        drive.setMinCgpa(minCgpa);
        drive.setEligibleBranches(eligibleBranches.trim());
        drive.setMaxBacklogs(maxBacklogs);
        drive.setCtcOrStipend(ctcOrStipend.trim());
        drive.setRegistrationLink(registrationLink);
        drive.setDeadline(deadline);
        drive.setActive(active);

        upcomingDriveRepository.save(drive);

        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, (id != null ? "UPDATE_UPCOMING_DRIVE" : "CREATE_UPCOMING_DRIVE"), "UpcomingDrive", drive.getId(), null, drive.getCompanyName());

        // Notify matching students (NOT-1)
        notificationService.notifyStudentsOfNewUpcomingDrive(drive);

        redirectAttributes.addFlashAttribute("successMessage", "Upcoming drive for " + drive.getCompanyName() + " posted and notification sent to students!");
        return "redirect:/admin/upcoming-drives";
    }

    @PostMapping("/upcoming-drives/{id}/delete")
    public String deleteUpcomingDrive(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        upcomingDriveRepository.deleteById(id);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "DELETE_UPCOMING_DRIVE", "UpcomingDrive", id, null, null);

        redirectAttributes.addFlashAttribute("successMessage", "Upcoming drive deleted.");
        return "redirect:/admin/upcoming-drives";
    }

    // ==========================================
    // 2.2 Off-Campus Internships & Hackathons Management
    // ==========================================
    @GetMapping("/opportunities")
    public String manageOpportunities(Model model) {
        model.addAttribute("opportunities", opportunityService.getAllForAdmin());
        model.addAttribute("types", in.jecrcu.placement.entity.enums.OpportunityType.values());
        model.addAttribute("stats", opportunityService.getStatistics());
        return "admin/opportunities";
    }

    @PostMapping("/opportunities/save")
    public String saveOpportunity(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("title") String title,
            @RequestParam("organization") String organization,
            @RequestParam("type") in.jecrcu.placement.entity.enums.OpportunityType type,
            @RequestParam(value = "category", defaultValue = "Technology") String category,
            @RequestParam(value = "mode", defaultValue = "Remote") String mode,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam("compensationOrPrize") String compensationOrPrize,
            @RequestParam(value = "eligibleBatches", defaultValue = "All Batches") String eligibleBatches,
            @RequestParam(value = "eligibleBranches", defaultValue = "All Branches") String eligibleBranches,
            @RequestParam("deadline") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime deadline,
            @RequestParam(value = "eventDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate eventDate,
            @RequestParam("registrationUrl") String registrationUrl,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "perks", required = false) String perks,
            @RequestParam(value = "featured", defaultValue = "false") boolean featured,
            @RequestParam(value = "active", defaultValue = "true") boolean active,
            @RequestParam(value = "notifyStudents", defaultValue = "false") boolean notifyStudents,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        Opportunity opp = (id != null) ? opportunityService.findById(id).orElse(new Opportunity()) : new Opportunity();
        opp.setTitle(title.trim());
        opp.setOrganization(organization.trim());
        opp.setType(type);
        opp.setCategory(category.trim());
        opp.setMode(mode.trim());
        opp.setLocation(location != null ? location.trim() : null);
        opp.setCompensationOrPrize(compensationOrPrize.trim());
        opp.setEligibleBatches(eligibleBatches.trim());
        opp.setEligibleBranches(eligibleBranches.trim());
        opp.setDeadline(deadline);
        opp.setEventDate(eventDate);
        opp.setRegistrationUrl(registrationUrl.trim());
        opp.setDescription(description != null ? description.trim() : null);
        opp.setPerks(perks != null ? perks.trim() : null);
        opp.setFeatured(featured);
        opp.setActive(active);

        opportunityService.saveOpportunity(opp);

        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, (id != null ? "UPDATE_OPPORTUNITY" : "CREATE_OPPORTUNITY"), "Opportunity", opp.getId(), null, opp.getTitle() + " (" + opp.getOrganization() + ")");

        if (notifyStudents || id == null) {
            notificationService.notifyStudentsOfNewOpportunity(opp);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Opportunity '" + opp.getTitle() + "' saved successfully!");
        return "redirect:/admin/opportunities";
    }

    @PostMapping("/opportunities/{id}/toggle")
    public String toggleOpportunity(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        boolean active = opportunityService.toggleActive(id);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "TOGGLE_OPPORTUNITY", "Opportunity", id, null, "Active: " + active);

        redirectAttributes.addFlashAttribute("successMessage", "Opportunity status updated to " + (active ? "Active" : "Archived"));
        return "redirect:/admin/opportunities";
    }

    @PostMapping("/opportunities/{id}/delete")
    public String deleteOpportunity(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        opportunityService.deleteOpportunity(id);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "DELETE_OPPORTUNITY", "Opportunity", id, null, null);

        redirectAttributes.addFlashAttribute("successMessage", "Opportunity deleted successfully.");
        return "redirect:/admin/opportunities";
    }

    // ==========================================
    // 3. User Reports Queue
    // ==========================================
    @GetMapping("/reports")
    public String manageReports(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "admin/reports";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(
            @PathVariable("id") Long id,
            @RequestParam(value = "adminNotes", required = false) String adminNotes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        reportService.resolveReport(id, adminNotes);
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "RESOLVE_REPORT", "Report", id, "OPEN", "RESOLVED");

        redirectAttributes.addFlashAttribute("successMessage", "Report marked as resolved.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{id}/dismiss")
    public String dismissReport(
            @PathVariable("id") Long id,
            @RequestParam(value = "adminNotes", required = false) String adminNotes,
            RedirectAttributes redirectAttributes
    ) {
        reportService.dismissReport(id, adminNotes);
        redirectAttributes.addFlashAttribute("successMessage", "Report dismissed.");
        return "redirect:/admin/reports";
    }

    // ==========================================
    // 4. Audit Log
    // ==========================================
    @GetMapping("/audit-log")
    public String auditLogPage(
            @RequestParam(value = "entityType", required = false) String entityType,
            Model model
    ) {
        List<AuditLog> logs = (entityType != null && !entityType.isBlank() && !entityType.equalsIgnoreCase("ALL"))
                ? auditService.getLogsByEntityType(entityType)
                : auditService.getAllLogs();

        model.addAttribute("logs", logs);
        model.addAttribute("selectedEntityType", entityType);
        return "admin/audit-log";
    }

    // ==========================================
    // 5. Bulk CSV / Excel Import
    // ==========================================
    @GetMapping("/import")
    public String importPage(Model model) {
        model.addAttribute("previewResult", cachedImportPreview);
        return "admin/import";
    }

    @PostMapping("/import/preview")
    public String previewImport(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        try {
            this.cachedImportPreview = importService.parseAndValidate(file);
            redirectAttributes.addFlashAttribute("successMessage",
                    "File parsed: " + cachedImportPreview.getTotalRows() + " rows found (" +
                    cachedImportPreview.getValidRows().size() + " valid, " +
                    cachedImportPreview.getInvalidRows().size() + " invalid). Review below before committing.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error parsing import file: " + e.getMessage());
        }
        return "redirect:/admin/import";
    }

    @PostMapping("/import/commit")
    public String commitImport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (cachedImportPreview == null || cachedImportPreview.getValidRows().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No validated records to import. Please upload a file first.");
            return "redirect:/admin/import";
        }

        int count = importService.executeImport(cachedImportPreview.getValidRows());
        User actor = (userDetails != null) ? userService.findById(userDetails.getId()).orElse(null) : null;
        auditService.logAction(actor, "BULK_IMPORT", "Drives", null, null, count + " records imported");

        this.cachedImportPreview = null;
        redirectAttributes.addFlashAttribute("successMessage", "Success! " + count + " recruitment drives imported into database.");
        return "redirect:/admin/import";
    }

    // ==========================================
    // 6. PDF & Excel Placement Reports Export
    // ==========================================
    @GetMapping("/export/pdf")
    public void exportPdfReport(
            @RequestParam(value = "year", required = false) Integer year,
            HttpServletResponse response
    ) throws Exception {
        byte[] pdfBytes = exportService.generatePdfReport(year);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"JECRC_Placement_Report_" + (year != null ? year : "All") + ".pdf\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/export/excel")
    public void exportExcelReport(
            @RequestParam(value = "year", required = false) Integer year,
            HttpServletResponse response
    ) throws Exception {
        byte[] excelBytes = exportService.generateExcelReport(year);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"JECRC_Placement_Report_" + (year != null ? year : "All") + ".xlsx\"");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }
}
