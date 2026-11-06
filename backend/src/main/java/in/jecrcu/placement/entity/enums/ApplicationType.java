package in.jecrcu.placement.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
    ON_CAMPUS("On-Campus", "bi-mortarboard-fill", "badge-soft-primary", "bg-primary-subtle text-primary border-primary"),
    OFF_CAMPUS("Off-Campus", "bi-globe2", "badge-soft-success", "bg-success-subtle text-success border-success"),
    OPPORTUNITY("Hackathon / Contest", "bi-trophy-fill", "badge-soft-warning", "bg-warning-subtle text-warning border-warning");

    private final String displayName;
    private final String iconClass;
    private final String badgeClass;
    private final String pillClass;
}
