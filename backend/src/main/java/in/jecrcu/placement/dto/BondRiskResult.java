package in.jecrcu.placement.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BondRiskResult {
    private int riskScore; // 0 to 100
    private String riskLevel; // LOW, MODERATE, HIGH, SEVERE
    private String badgeClass;
    private String pillClass;
    private String iconClass;
    private String summaryHeadline;
    private String detailedAnalysis;

    @Builder.Default
    private List<String> redFlags = new ArrayList<>();

    @Builder.Default
    private List<String> safePoints = new ArrayList<>();

    @Builder.Default
    private List<String> legalTips = new ArrayList<>();
}
