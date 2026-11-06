package in.jecrcu.placement.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStage {
    APPLIED("Applied", "badge-soft-primary", "bg-primary-subtle text-primary border-primary", "bi-send-fill", 1),
    OA_ROUND("Online Assessment", "badge-soft-warning", "bg-warning-subtle text-warning border-warning", "bi-laptop", 2),
    TECH_INTERVIEW("Technical Interview", "badge-soft-info", "bg-info-subtle text-info border-info", "bi-code-slash", 3),
    HR_ROUND("HR & Managerial", "badge-soft-purple", "bg-purple-subtle text-purple border-purple", "bi-people-fill", 4),
    OFFERED("Offer Received", "badge-soft-success", "bg-success-subtle text-success border-success", "bi-trophy-fill", 5),
    REJECTED("Not Selected", "badge-soft-danger", "bg-danger-subtle text-danger border-danger", "bi-x-circle-fill", 6);

    private final String displayName;
    private final String badgeClass;
    private final String pillClass;
    private final String iconClass;
    private final int order;

    public boolean isTerminal() {
        return this == OFFERED || this == REJECTED;
    }
}
