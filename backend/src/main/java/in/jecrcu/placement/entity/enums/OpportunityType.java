package in.jecrcu.placement.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpportunityType {
    HACKATHON("Hackathon", "bi-trophy-fill", "badge-warning", "bg-warning-subtle text-warning border-warning"),
    INTERNSHIP("Internship", "bi-briefcase-fill", "badge-primary", "bg-primary-subtle text-primary border-primary"),
    HIRING_CHALLENGE("Hiring Challenge", "bi-code-slash", "badge-info", "bg-info-subtle text-info border-info"),
    SCHOLARSHIP("Scholarship & Mentorship", "bi-award-fill", "badge-success", "bg-success-subtle text-success border-success");

    private final String displayName;
    private final String iconClass;
    private final String badgeClass;
    private final String pillClass;
}
