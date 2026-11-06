package in.jecrcu.placement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineStatsDto {
    private long totalApplications;
    private long appliedCount;
    private long oaCount;
    private long techCount;
    private long hrCount;
    private long offeredCount;
    private long rejectedCount;

    // Computed funnel helpers
    public long getActiveInPipeline() {
        return appliedCount + oaCount + techCount + hrCount;
    }

    public long getInterviewsCount() {
        return techCount + hrCount;
    }

    public int getOfferRate() {
        if (totalApplications == 0) return 0;
        return (int) Math.round(((double) offeredCount / totalApplications) * 100);
    }

    public int getInterviewRate() {
        if (totalApplications == 0) return 0;
        long reachedInterviewOrOffer = techCount + hrCount + offeredCount;
        return (int) Math.round(((double) reachedInterviewOrOffer / totalApplications) * 100);
    }
}
