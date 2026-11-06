package in.jecrcu.placement.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferAnalysisResult {
    private String companyName;
    private String roleTitle;
    private String city;
    private String workMode;
    private String taxRegime;

    // Headline numbers (Annual)
    private double ctcAnnual;
    private double grossAnnual;
    private double netTakeHomeAnnual;

    // Monthly figures (Crucial for students)
    private double monthlyGross;
    private double monthlyInHand;
    private double monthlyTaxDeduction;
    private double monthlyPfDeduction;
    private double monthlyProfessionalTax;

    // Percentages
    private int inHandPercentage; // e.g. 78% of CTC
    private int deductionsPercentage;

    // Detailed Breakdown Items (Annual in INR)
    private double baseSalary;
    private double variableBonus;
    private double joiningBonus;
    private double esops;
    private double employerPfGratuity;
    private double totalAnnualTax;
    private double totalAnnualPf;
    private double totalAnnualProfessionalTax;

    // City Cost of Living Adjustment
    private double cityCostIndex; // e.g. 1.6 for Bangalore vs 1.0 for Jaipur
    private double purchasingPowerAdjustedMonthly; // Real spending power normalized to Jaipur base
    private String livingCostCategory; // High, Moderate, Affordable

    // Bond Risk Assessment
    private BondRiskResult bondRisk;
}
