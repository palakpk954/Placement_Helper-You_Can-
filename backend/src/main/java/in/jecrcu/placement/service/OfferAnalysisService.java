package in.jecrcu.placement.service;

import in.jecrcu.placement.dto.BondRiskResult;
import in.jecrcu.placement.dto.OfferAnalysisRequest;
import in.jecrcu.placement.dto.OfferAnalysisResult;
import in.jecrcu.placement.dto.OfferComparisonResult;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferAnalysisService {

    private final CompanyRepository companyRepository;

    private static final Map<String, Double> CITY_COST_INDEX = new LinkedHashMap<>();
    static {
        CITY_COST_INDEX.put("JAIPUR", 1.00);
        CITY_COST_INDEX.put("BANGALORE", 1.65);
        CITY_COST_INDEX.put("MUMBAI", 1.80);
        CITY_COST_INDEX.put("GURGAON", 1.50);
        CITY_COST_INDEX.put("DELHI NCR", 1.45);
        CITY_COST_INDEX.put("PUNE", 1.35);
        CITY_COST_INDEX.put("HYDERABAD", 1.35);
        CITY_COST_INDEX.put("NOIDA", 1.30);
        CITY_COST_INDEX.put("CHENNAI", 1.25);
        CITY_COST_INDEX.put("REMOTE", 0.90);
    }

    /**
     * Performs end-to-end breakdown of CTC into monthly take-home, taxes, PF, city cost adjustment, and bond risk.
     */
    public OfferAnalysisResult analyzeOffer(OfferAnalysisRequest request) {
        double ctcLpa = request.getCtcAnnualLpa() != null && request.getCtcAnnualLpa() > 0
                ? request.getCtcAnnualLpa()
                : 10.0;
        double ctcAnnual = ctcLpa * 100_000.0;

        // Base salary breakdown (defaults to 60% of CTC if unspecified)
        double baseAnnual = request.getBasicSalaryLpa() != null && request.getBasicSalaryLpa() > 0
                ? request.getBasicSalaryLpa() * 100_000.0
                : ctcAnnual * 0.60;

        // Variable / performance bonus (defaults to 12% if unspecified)
        double variableAnnual = request.getVariableBonusLpa() != null && request.getVariableBonusLpa() >= 0
                ? request.getVariableBonusLpa() * 100_000.0
                : ctcAnnual * 0.12;

        // Joining / retention bonus (first year)
        double joiningBonus = request.getJoiningBonusLpa() != null && request.getJoiningBonusLpa() >= 0
                ? request.getJoiningBonusLpa() * 100_000.0
                : 0.0;

        // ESOPs / Stocks (annualized)
        double esopsAnnual = request.getEsopAnnualLpa() != null && request.getEsopAnnualLpa() >= 0
                ? request.getEsopAnnualLpa() * 100_000.0
                : 0.0;

        // Employer PF + Gratuity (typically part of CTC, not paid in monthly cash)
        double employerPfGratuity = request.getPfGratuityLpa() != null && request.getPfGratuityLpa() >= 0
                ? request.getPfGratuityLpa() * 100_000.0
                : ctcAnnual * 0.06;

        // Cash Gross = CTC minus non-cash ESOPs and Employer PF/Gratuity
        double grossCashAnnual = baseAnnual + variableAnnual + (joiningBonus > 0 ? joiningBonus : 0);
        if (grossCashAnnual > ctcAnnual) {
            grossCashAnnual = ctcAnnual;
        }

        // Employee PF deduction (12% of basic, standard Indian statutory deduction)
        double employeePfAnnual = Math.min(baseAnnual * 0.12, 1800.0 * 12.0 * 2.5); // Cap at realistic corporate standard

        // Professional Tax (standard ₹200/month = ₹2,400/year across Indian states like Karnataka, Maharashtra, Rajasthan)
        double professionalTaxAnnual = 2400.0;

        // Calculate Income Tax based on chosen regime (default: NEW)
        String regime = (request.getTaxRegime() != null && request.getTaxRegime().equalsIgnoreCase("OLD")) ? "OLD" : "NEW";
        double totalAnnualTax = calculateIncomeTax(grossCashAnnual, employeePfAnnual, regime);

        // Net Annual Take-Home (excluding variable if paid once a year, but prorated monthly)
        double netTakeHomeAnnual = Math.max(0.0, grossCashAnnual - totalAnnualTax - employeePfAnnual - professionalTaxAnnual);

        // Monthly Figures
        double monthlyGross = Math.round((grossCashAnnual / 12.0));
        double monthlyInHand = Math.round((netTakeHomeAnnual / 12.0));
        double monthlyTax = Math.round((totalAnnualTax / 12.0));
        double monthlyPf = Math.round((employeePfAnnual / 12.0));
        double monthlyPT = Math.round((professionalTaxAnnual / 12.0));

        int inHandPct = (int) Math.round((netTakeHomeAnnual / ctcAnnual) * 100.0);
        int deductionsPct = 100 - inHandPct;

        // City Cost of Living Adjustment
        String cityKey = request.getCity() != null ? request.getCity().trim().toUpperCase() : "JAIPUR";
        double cityIndex = CITY_COST_INDEX.getOrDefault(cityKey, 1.25);
        double purchasingPowerMonthly = Math.round(monthlyInHand / cityIndex);

        String costCategory = "Affordable";
        if (cityIndex >= 1.6) costCategory = "Very High Living Cost";
        else if (cityIndex >= 1.3) costCategory = "Moderate to High";

        // Bond Risk Evaluation
        BondRiskResult bondRisk = evaluateBondRisk(request);

        String companyName = request.getCompanyName() != null && !request.getCompanyName().isBlank()
                ? request.getCompanyName()
                : "Recruiter";

        String roleTitle = request.getRoleTitle() != null && !request.getRoleTitle().isBlank()
                ? request.getRoleTitle()
                : "Associate Software Engineer";

        return OfferAnalysisResult.builder()
                .companyName(companyName)
                .roleTitle(roleTitle)
                .city(request.getCity() != null ? request.getCity() : "Jaipur")
                .workMode(request.getWorkMode() != null ? request.getWorkMode() : "HYBRID")
                .taxRegime(regime)
                .ctcAnnual(ctcAnnual)
                .grossAnnual(grossCashAnnual)
                .netTakeHomeAnnual(netTakeHomeAnnual)
                .monthlyGross(monthlyGross)
                .monthlyInHand(monthlyInHand)
                .monthlyTaxDeduction(monthlyTax)
                .monthlyPfDeduction(monthlyPf)
                .monthlyProfessionalTax(monthlyPT)
                .inHandPercentage(inHandPct)
                .deductionsPercentage(deductionsPct)
                .baseSalary(baseAnnual)
                .variableBonus(variableAnnual)
                .joiningBonus(joiningBonus)
                .esops(esopsAnnual)
                .employerPfGratuity(employerPfGratuity)
                .totalAnnualTax(totalAnnualTax)
                .totalAnnualPf(employeePfAnnual)
                .totalAnnualProfessionalTax(professionalTaxAnnual)
                .cityCostIndex(cityIndex)
                .purchasingPowerAdjustedMonthly(purchasingPowerMonthly)
                .livingCostCategory(costCategory)
                .bondRisk(bondRisk)
                .build();
    }

    /**
     * Indian Income Tax Calculator (FY 2025-26).
     * New Tax Regime:
     * - Standard Deduction: ₹75,000
     * - Full tax rebate under Section 87A for taxable income up to ₹7,00,000
     * - Health & Education Cess: 4%
     */
    public double calculateIncomeTax(double grossIncome, double pfDeduction, String regime) {
        if ("OLD".equalsIgnoreCase(regime)) {
            // Old Regime calculation
            double standardDeduction = 50_000.0;
            double section80CDeduction = Math.min(150_000.0, pfDeduction + 50_000.0); // Assume basic 80C
            double taxable = Math.max(0.0, grossIncome - standardDeduction - section80CDeduction);

            if (taxable <= 500_000.0) {
                return 0.0; // 87A rebate under old regime
            }

            double tax = 0.0;
            if (taxable > 250_000.0) {
                tax += Math.min(taxable - 250_000.0, 250_000.0) * 0.05;
            }
            if (taxable > 500_000.0) {
                tax += Math.min(taxable - 500_000.0, 500_000.0) * 0.20;
            }
            if (taxable > 1_000_000.0) {
                tax += (taxable - 1_000_000.0) * 0.30;
            }
            return Math.round(tax * 1.04); // 4% cess
        }

        // NEW REGIME (FY 2025-26 Default)
        double standardDeduction = 75_000.0;
        double taxable = Math.max(0.0, grossIncome - standardDeduction);

        // Section 87A: Taxable income up to 7 Lakhs pays zero tax
        if (taxable <= 700_000.0) {
            return 0.0;
        }

        double tax = 0.0;
        // 0 to 3L: 0%
        // 3L to 7L: 5% (400,000 * 0.05 = 20,000)
        if (taxable > 300_000.0) {
            tax += Math.min(taxable - 300_000.0, 400_000.0) * 0.05;
        }
        // 7L to 10L: 10% (300,000 * 0.10 = 30,000)
        if (taxable > 700_000.0) {
            tax += Math.min(taxable - 700_000.0, 300_000.0) * 0.10;
        }
        // 10L to 12L: 15% (200,000 * 0.15 = 30,000)
        if (taxable > 1_000_000.0) {
            tax += Math.min(taxable - 1_000_000.0, 200_000.0) * 0.15;
        }
        // 12L to 15L: 20% (300,000 * 0.20 = 60,000)
        if (taxable > 1_200_000.0) {
            tax += Math.min(taxable - 1_200_000.0, 300_000.0) * 0.20;
        }
        // Above 15L: 30%
        if (taxable > 1_500_000.0) {
            tax += (taxable - 1_500_000.0) * 0.30;
        }

        return Math.round(tax * 1.04); // 4% Health & Education Cess
    }

    /**
     * Evaluates Service Agreement / Bond clauses and generates a Bond Risk Score (0 - 100).
     */
    public BondRiskResult evaluateBondRisk(OfferAnalysisRequest req) {
        int score = 0;
        List<String> redFlags = new ArrayList<>();
        List<String> safePoints = new ArrayList<>();
        List<String> legalTips = new ArrayList<>();

        int duration = req.getBondDurationMonths() != null ? req.getBondDurationMonths() : 0;
        double penalty = req.getBondPenaltyLakhs() != null ? req.getBondPenaltyLakhs() : 0.0;
        boolean docRetention = Boolean.TRUE.equals(req.getCertificateRetention());
        boolean trainingRecovery = Boolean.TRUE.equals(req.getTrainingCostRecovery());
        int noticePeriod = req.getNoticePeriodMonths() != null ? req.getNoticePeriodMonths() : 1;
        boolean nonCompete = Boolean.TRUE.equals(req.getNonCompeteClause());

        // 1. Certificate Retention (Immediate Critical Red Flag!)
        if (docRetention) {
            score += 50;
            redFlags.add("ILLEGAL: Employer requires withholding original educational certificates/marksheets.");
            legalTips.add("UGC and Indian Courts have explicitly ruled that employers cannot retain original academic marksheets as security. Provide only self-attested photocopies.");
        } else {
            safePoints.add("No original certificate withholding required.");
        }

        // 2. Bond Duration
        if (duration == 0) {
            safePoints.add("No lock-in service agreement or employment bond.");
        } else if (duration <= 12) {
            score += 15;
            safePoints.add("1-year service agreement is common in entry-level campus recruitment.");
        } else if (duration <= 24) {
            score += 35;
            redFlags.add(duration + "-month service agreement restricts career mobility for 2 years.");
        } else {
            score += 55;
            redFlags.add("Excessive bond duration: " + duration + " months (3+ years) severely locks in career options.");
        }

        // 3. Penalty Amount
        if (penalty > 0) {
            if (penalty <= 1.0) {
                score += 10;
                legalTips.add("Under Section 74 of the Indian Contract Act, companies can only claim actual proven training expenses, not arbitrary penalties.");
            } else if (penalty <= 2.0) {
                score += 25;
                redFlags.add("High exit penalty of ₹" + penalty + " Lakhs for pre-mature resignation.");
            } else {
                score += 40;
                redFlags.add("Severe financial penalty of ₹" + penalty + " Lakhs. High risk if you plan for higher studies or competitive exams.");
            }
        }

        // 4. Training Cost Recovery
        if (trainingRecovery) {
            score += 15;
            redFlags.add("Uncapped training expense recovery clause allows arbitrary deduction on exit.");
            legalTips.add("Ask HR for an itemized breakdown of formal external certifications funded before signing.");
        }

        // 5. Notice Period
        if (noticePeriod > 2) {
            score += 15;
            redFlags.add("Long " + noticePeriod + "-month notice period makes it difficult to switch to companies requiring 30-day joining.");
        } else if (noticePeriod == 1) {
            safePoints.add("Standard 30-day notice period ensures smooth future transitions.");
        }

        // 6. Non-Compete Clause
        if (nonCompete) {
            score += 15;
            redFlags.add("Non-compete clause restricts employment in the same industry post-exit.");
            legalTips.add("Section 27 of the Indian Contract Act makes post-employment non-compete agreements legally unenforceable in India.");
        }

        // Cap score at 100
        score = Math.min(100, score);

        String riskLevel;
        String badgeClass;
        String pillClass;
        String iconClass;
        String summary;
        String detailedAnalysis;

        if (score <= 20) {
            riskLevel = "LOW RISK";
            badgeClass = "badge-soft-success";
            pillClass = "bg-success-subtle text-success border-success";
            iconClass = "bi-shield-check";
            summary = "Clean, Candidate-Friendly Offer";
            detailedAnalysis = "This employment contract follows standard corporate practices with minimal or no restrictive covenants. Safe to sign.";
        } else if (score <= 45) {
            riskLevel = "MODERATE RISK";
            badgeClass = "badge-soft-warning";
            pillClass = "bg-warning-subtle text-warning border-warning";
            iconClass = "bi-shield-exclamation";
            summary = "Standard Corporate Service Agreement";
            detailedAnalysis = "Contains typical entry-level lock-in terms. Acceptable if you plan to stay with the company for at least " + duration + " months.";
        } else if (score <= 70) {
            riskLevel = "HIGH RISK";
            badgeClass = "badge-soft-danger";
            pillClass = "bg-danger-subtle text-danger border-danger";
            iconClass = "bi-exclamation-triangle-fill";
            summary = "Restrictive Bond Terms Flagged";
            detailedAnalysis = "Contains substantial penalties and long lock-in durations. Exercise caution and verify reimbursement clauses before committing.";
        } else {
            riskLevel = "SEVERE RISK";
            badgeClass = "badge-soft-danger";
            pillClass = "bg-danger-subtle text-danger border-danger";
            iconClass = "bi-shield-slash-fill";
            summary = "Potentially Predatory Agreement";
            detailedAnalysis = "Contains clauses (such as document retention or exorbitant penalties) that pose significant legal and career risks.";
        }

        return BondRiskResult.builder()
                .riskScore(score)
                .riskLevel(riskLevel)
                .badgeClass(badgeClass)
                .pillClass(pillClass)
                .iconClass(iconClass)
                .summaryHeadline(summary)
                .detailedAnalysis(detailedAnalysis)
                .redFlags(redFlags)
                .safePoints(safePoints)
                .legalTips(legalTips)
                .build();
    }

    /**
     * Compares multiple offers side-by-side and determines a balanced verdict.
     */
    public OfferComparisonResult compareOffers(List<OfferAnalysisRequest> requests) {
        List<OfferAnalysisResult> results = new ArrayList<>();
        for (OfferAnalysisRequest req : requests) {
            results.add(analyzeOffer(req));
        }

        if (results.isEmpty()) {
            return OfferComparisonResult.builder().offers(Collections.emptyList()).build();
        }

        // Find Best Cash In Hand
        OfferAnalysisResult bestCash = results.stream()
                .max(Comparator.comparingDouble(OfferAnalysisResult::getMonthlyInHand))
                .orElse(results.get(0));

        // Find Safest Agreement
        OfferAnalysisResult safest = results.stream()
                .min(Comparator.comparingInt(o -> o.getBondRisk().getRiskScore()))
                .orElse(results.get(0));

        // Find Best Purchasing Power (accounting for city cost of living)
        OfferAnalysisResult bestPower = results.stream()
                .max(Comparator.comparingDouble(OfferAnalysisResult::getPurchasingPowerAdjustedMonthly))
                .orElse(results.get(0));

        // Overall Winner
        OfferAnalysisResult winner = bestCash;
        String reason = "Offers the highest monthly in-hand take home (₹" + formatCurrency(bestCash.getMonthlyInHand()) + "/mo).";

        if (safest.getBondRisk().getRiskScore() < 25 && bestCash.getBondRisk().getRiskScore() > 60) {
            winner = safest;
            reason = "Selected as safest overall career choice due to clean terms and no predatory bond conditions, despite slightly lower CTC.";
        } else if (bestPower.getPurchasingPowerAdjustedMonthly() > bestCash.getPurchasingPowerAdjustedMonthly() * 1.15) {
            winner = bestPower;
            reason = "Provides significantly higher real purchasing power (₹" + formatCurrency(bestPower.getPurchasingPowerAdjustedMonthly()) + "/mo adjusted for " + bestPower.getCity() + "'s lower cost of living).";
        }

        return OfferComparisonResult.builder()
                .offers(results)
                .winnerCompany(winner.getCompanyName())
                .winnerReason(reason)
                .bestCashInHandCompany(bestCash.getCompanyName() + " (₹" + formatCurrency(bestCash.getMonthlyInHand()) + "/mo)")
                .safestAgreementCompany(safest.getCompanyName() + " (" + safest.getBondRisk().getRiskLevel() + ")")
                .bestWorkLifeBalanceCompany(bestPower.getCompanyName() + " (" + bestPower.getCity() + ")")
                .build();
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##,###");
        return df.format(amount);
    }
}
