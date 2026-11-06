package in.jecrcu.placement.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferAnalysisRequest {
    private String offerTitle;
    private String companyName;
    private Long companyId;
    private String roleTitle;
    private String city; // Bangalore, Jaipur, Gurgaon, Pune, Hyderabad, Mumbai, Noida, Remote
    private String workMode; // REMOTE, HYBRID, ONSITE

    // Compensation inputs (in INR per annum or LPA)
    private Double ctcAnnualLpa; // e.g. 12.0
    private Double basicSalaryLpa; // e.g. 6.0
    private Double variableBonusLpa; // e.g. 1.2
    private Double joiningBonusLpa; // e.g. 1.0 (first year)
    private Double esopAnnualLpa; // e.g. 1.5
    private Double pfGratuityLpa; // e.g. 0.8

    // Tax Settings
    @Builder.Default
    private String taxRegime = "NEW"; // NEW or OLD

    // Bond & Agreement Settings
    @Builder.Default
    private Integer bondDurationMonths = 0; // 0, 12, 18, 24, 36
    @Builder.Default
    private Double bondPenaltyLakhs = 0.0; // 0, 1.0, 2.0, 3.0
    @Builder.Default
    private Boolean certificateRetention = false; // Withholding original marksheets
    @Builder.Default
    private Boolean trainingCostRecovery = false; // Training cost clawback clause
    @Builder.Default
    private Integer noticePeriodMonths = 1; // 1, 2, 3
    @Builder.Default
    private Boolean nonCompeteClause = false;
    private String bondNotes;
}
