package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Resume;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.ResumeRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Value("${app.upload.dir:./uploads/resumes}")
    private String uploadDir;

    @Getter
    public static class ResumeChecklistReport {
        private boolean hasContactEmail;
        private boolean hasPhoneNumber;
        private boolean hasLinkedInOrGitHub;
        private boolean isSinglePage;
        private int pageCount;
        private boolean hasProjects;
        private boolean hasTechnicalSkills;
        private boolean hasEducation;
        private boolean hasExperienceOrInternship;
        private int totalWordCount;
        private int scorePercentage;
        private List<String> suggestions = new ArrayList<>();
    }

    public Optional<Resume> findById(Long id) {
        return resumeRepository.findById(id);
    }

    public Optional<Resume> getLatestResumeForUser(Long userId) {
        return resumeRepository.findTopByUserIdOrderByUploadedAtDesc(userId);
    }

    public List<Resume> getSharedResumes() {
        return resumeRepository.findByShareConsentTrueOrderByUploadedAtDesc();
    }

    @Transactional
    public Resume uploadAndAnalyze(User user, MultipartFile file, boolean shareConsent) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty.");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Resume must be smaller than 2 MB.");
        }
        String rawName = file.getOriginalFilename();
        String originalFilename = (rawName != null && !rawName.isBlank()) ? rawName : "resume.pdf";
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF resumes are supported.");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedName = "user_" + user.getId() + "_" + System.currentTimeMillis() + ".pdf";
        Path targetFile = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), targetFile);

        // Run PDFBox Analysis
        ResumeChecklistReport report = analyzePdf(targetFile.toFile());

        String feedback = report.getSuggestions().isEmpty()
                ? "Excellent resume structure! All core sections, contact information, and formatting checks passed."
                : String.join(" ", report.getSuggestions());
        String escapedFeedback = feedback.replace("\"", "\\\"");

        String jsonResult = String.format(
                "{\"score\":%d,\"pages\":%d,\"email\":%b,\"phone\":%b,\"linkedin\":%b,\"projects\":%b,\"skills\":%b,\"education\":%b,\"words\":%d,\"feedback\":\"%s\"}",
                report.getScorePercentage(), report.getPageCount(), report.isHasContactEmail(),
                report.isHasPhoneNumber(), report.isHasLinkedInOrGitHub(), report.isHasProjects(),
                report.isHasTechnicalSkills(), report.isHasEducation(), report.getTotalWordCount(),
                escapedFeedback
        );

        Resume resume = Resume.builder()
                .user(user)
                .filePath(targetFile.toString())
                .originalFilename(originalFilename)
                .fileSize(file.getSize())
                .shareConsent(shareConsent)
                .checklistResultJson(jsonResult)
                .uploadedAt(LocalDateTime.now())
                .build();

        return resumeRepository.save(resume);
    }

    public ResumeChecklistReport analyzePdf(File pdfFile) {
        ResumeChecklistReport report = new ResumeChecklistReport();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            report.pageCount = document.getNumberOfPages();
            report.isSinglePage = report.pageCount == 1;

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String lower = text.toLowerCase();

            // Word count
            String[] words = text.split("\\s+");
            report.totalWordCount = words.length;

            // Contact checks
            report.hasContactEmail = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(text).find();
            report.hasPhoneNumber = Pattern.compile("(\\+?\\d{1,3}[- ]?)?\\d{10}").matcher(text).find();
            report.hasLinkedInOrGitHub = lower.contains("linkedin.com") || lower.contains("github.com") || lower.contains("github") || lower.contains("linkedin");

            // Key section headings
            report.hasEducation = lower.contains("education") || lower.contains("academic") || lower.contains("b.tech");
            report.hasTechnicalSkills = lower.contains("skill") || lower.contains("technologies") || lower.contains("technical skills") || lower.contains("languages");
            report.hasProjects = lower.contains("project") || lower.contains("projects") || lower.contains("key projects");
            report.hasExperienceOrInternship = lower.contains("experience") || lower.contains("internship") || lower.contains("work experience");

            // Compute score
            int points = 0;
            if (report.hasContactEmail) points += 15; else report.suggestions.add("Add a professional email address.");
            if (report.hasPhoneNumber) points += 10; else report.suggestions.add("Add your phone contact number.");
            if (report.hasLinkedInOrGitHub) points += 15; else report.suggestions.add("Include clickable links to your LinkedIn profile and GitHub portfolio.");
            if (report.isSinglePage) points += 20; else report.suggestions.add("Format your resume into a single page (currently " + report.pageCount + " pages).");
            if (report.hasEducation) points += 10; else report.suggestions.add("Add an Education section highlighting your degree, university, and CGPA.");
            if (report.hasTechnicalSkills) points += 15; else report.suggestions.add("Add a Technical Skills section grouped by Languages, Frameworks, and Tools.");
            if (report.hasProjects) points += 15; else report.suggestions.add("Include at least two full-stack or technical projects with live links.");

            report.scorePercentage = points;
        } catch (Exception e) {
            log.error("Failed to parse PDF resume: {}", e.getMessage());
            report.suggestions.add("Could not extract complete text from PDF. Ensure your resume is text-based and not a scanned image.");
            report.scorePercentage = 50;
        }
        return report;
    }

    public String extractResumeText(Resume resume) {
        if (resume == null || resume.getFilePath() == null) return "";
        File file = new File(resume.getFilePath());
        if (!file.exists()) return "";
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.warn("Could not extract text from resume {}: {}", resume.getId(), e.getMessage());
            return "";
        }
    }
}
