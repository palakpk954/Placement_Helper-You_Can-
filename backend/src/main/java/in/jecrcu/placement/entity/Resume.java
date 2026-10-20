package in.jecrcu.placement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "share_consent")
    @Builder.Default
    private Boolean shareConsent = false;

    @Column(name = "checklist_result_json", columnDefinition = "TEXT")
    private String checklistResultJson;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    @Transient
    public String getFileName() {
        return originalFilename != null ? originalFilename : "resume.pdf";
    }

    @Transient
    public Integer getScore() {
        return extractInt("score", 70);
    }

    @Transient
    public Integer getPageCount() {
        return extractInt("pages", 1);
    }

    @Transient
    public boolean isHasContactInfo() {
        return extractBool("email") || extractBool("phone");
    }

    @Transient
    public boolean isHasProjectsSection() {
        return extractBool("projects");
    }

    @Transient
    public boolean isHasSkillsSection() {
        return extractBool("skills");
    }

    @Transient
    public boolean isHasEducationSection() {
        return extractBool("education");
    }

    @Transient
    public String getFeedback() {
        if (checklistResultJson != null && checklistResultJson.contains("\"feedback\":")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"feedback\":\\s*\"([^\"]+)\"").matcher(checklistResultJson);
            if (m.find()) return m.group(1);
        }

        java.util.List<String> tips = new java.util.ArrayList<>();
        if (!isHasContactInfo()) tips.add("Add your professional email address and phone number at the top.");
        if (getPageCount() > 1) tips.add("Format your resume into a single page (currently " + getPageCount() + " pages).");
        if (!isHasProjectsSection()) tips.add("Include a Projects section highlighting at least two technical projects with measurable outcomes.");
        if (!isHasSkillsSection()) tips.add("Add a Technical Skills section grouped by Languages, Frameworks, and Tools.");
        if (!isHasEducationSection()) tips.add("Include your degree, institution name, and current CGPA.");

        if (tips.isEmpty()) {
            return "Excellent resume structure! All core sections, contact information, and formatting checks passed. Consider adding quantitative metrics to project impact (e.g. 'improved query performance by 40%').";
        } else {
            return String.join(" ", tips);
        }
    }

    private int extractInt(String key, int defaultValue) {
        if (checklistResultJson == null) return defaultValue;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\":\\s*(\\d+)").matcher(checklistResultJson);
        return m.find() ? Integer.parseInt(m.group(1)) : defaultValue;
    }

    private boolean extractBool(String key) {
        if (checklistResultJson == null) return false;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\":\\s*(true|false)").matcher(checklistResultJson);
        return m.find() && Boolean.parseBoolean(m.group(1));
    }
}
