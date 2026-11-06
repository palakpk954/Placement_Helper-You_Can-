package in.jecrcu.placement;

import in.jecrcu.placement.dto.BondRiskResult;
import in.jecrcu.placement.dto.OfferAnalysisRequest;
import in.jecrcu.placement.dto.OfferAnalysisResult;
import in.jecrcu.placement.dto.OfferComparisonResult;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.service.OfferAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OfferAnalysisServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private OfferAnalysisService offerAnalysisService;

    private OfferAnalysisRequest req10Lpa;

    @BeforeEach
    void setUp() {
        req10Lpa = OfferAnalysisRequest.builder()
                .companyName("Deloitte")
                .roleTitle("Analyst")
                .city("JAIPUR")
                .ctcAnnualLpa(10.0)
                .basicSalaryLpa(6.0)
                .variableBonusLpa(1.2)
                .taxRegime("NEW")
                .bondDurationMonths(0)
                .bondPenaltyLakhs(0.0)
                .certificateRetention(false)
                .build();
    }

    @Test
    void testAnalyzeOffer_ComputesMonthlyInHandAccurately() {
        OfferAnalysisResult result = offerAnalysisService.analyzeOffer(req10Lpa);

        assertNotNull(result);
        assertEquals(10_00_000.0, result.getCtcAnnual());
        assertTrue(result.getMonthlyInHand() > 0, "Monthly in hand should be positive");
        assertTrue(result.getMonthlyInHand() < result.getMonthlyGross(), "Net take-home must be less than gross");
        assertTrue(result.getInHandPercentage() >= 60 && result.getInHandPercentage() <= 95);
        assertEquals("LOW RISK", result.getBondRisk().getRiskLevel());
    }

    @Test
    void testTaxCalculation_ZeroTaxUnder7LakhsNewRegime() {
        // Gross income 7.0 LPA -> with 75k std deduction taxable = 6.25L -> Section 87A rebate applies
        double tax = offerAnalysisService.calculateIncomeTax(700_000.0, 50_000.0, "NEW");
        assertEquals(0.0, tax, "Tax under New Regime for income up to 7 LPA (after 75k std deduction) must be 0");
    }

    @Test
    void testTaxCalculation_Above15Lakhs() {
        double tax = offerAnalysisService.calculateIncomeTax(2_000_000.0, 50_000.0, "NEW");
        assertTrue(tax > 200_000.0, "Tax on 20 LPA should be substantial");
    }

    @Test
    void testEvaluateBondRisk_CleanOfferIsLowRisk() {
        BondRiskResult risk = offerAnalysisService.evaluateBondRisk(req10Lpa);

        assertNotNull(risk);
        assertTrue(risk.getRiskScore() <= 20);
        assertEquals("LOW RISK", risk.getRiskLevel());
        assertFalse(risk.getSafePoints().isEmpty());
        assertTrue(risk.getRedFlags().isEmpty());
    }

    @Test
    void testEvaluateBondRisk_FlagCertificateRetentionAsSevere() {
        OfferAnalysisRequest predatoryReq = OfferAnalysisRequest.builder()
                .bondDurationMonths(36)
                .bondPenaltyLakhs(3.0)
                .certificateRetention(true) // Illegal document retention!
                .trainingCostRecovery(true)
                .noticePeriodMonths(3)
                .build();

        BondRiskResult risk = offerAnalysisService.evaluateBondRisk(predatoryReq);

        assertNotNull(risk);
        assertTrue(risk.getRiskScore() >= 75);
        assertEquals("SEVERE RISK", risk.getRiskLevel());
        assertTrue(risk.getRedFlags().stream().anyMatch(f -> f.contains("ILLEGAL")));
        assertFalse(risk.getLegalTips().isEmpty());
    }

    @Test
    void testCompareOffers_SelectsWinnerAndPurchasingPower() {
        OfferAnalysisRequest offerA = OfferAnalysisRequest.builder()
                .companyName("Bangalore Tech")
                .city("BANGALORE")
                .ctcAnnualLpa(12.0)
                .basicSalaryLpa(7.0)
                .bondDurationMonths(0)
                .build();

        OfferAnalysisRequest offerB = OfferAnalysisRequest.builder()
                .companyName("Jaipur Firm")
                .city("JAIPUR")
                .ctcAnnualLpa(10.0)
                .basicSalaryLpa(6.0)
                .bondDurationMonths(0)
                .build();

        OfferComparisonResult comparison = offerAnalysisService.compareOffers(List.of(offerA, offerB));

        assertNotNull(comparison);
        assertEquals(2, comparison.getOffers().size());
        assertNotNull(comparison.getWinnerCompany());
        assertNotNull(comparison.getBestCashInHandCompany());
        assertNotNull(comparison.getSafestAgreementCompany());
    }
}
