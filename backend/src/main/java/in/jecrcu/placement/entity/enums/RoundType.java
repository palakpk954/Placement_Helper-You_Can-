package in.jecrcu.placement.entity.enums;

public enum RoundType {
    ONLINE_ASSESSMENT("Online Assessment"),
    APTITUDE("Aptitude & Reasoning"),
    CODING("Coding Assessment"),
    TECHNICAL("Technical Interview"),
    HR("HR / Fitment Interview"),
    GROUP_DISCUSSION("Group Discussion"),
    CASE_STUDY("Case Study / System Design");

    private final String displayName;

    RoundType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
