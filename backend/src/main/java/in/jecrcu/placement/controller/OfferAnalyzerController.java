package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.dto.OfferAnalysisRequest;
import in.jecrcu.placement.dto.OfferAnalysisResult;
import in.jecrcu.placement.dto.OfferComparisonResult;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.service.CompanyService;
import in.jecrcu.placement.service.OfferAnalysisService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/offers")
@RequiredArgsConstructor
@Slf4j
public class OfferAnalyzerController {

    private final OfferAnalysisService offerAnalysisService;
    private final CompanyService companyService;
    private final UserService userService;

    @GetMapping
    public String offerAnalyzerPage(
            @RequestParam(value = "ctc", required = false, defaultValue = "10.0") Double ctc,
            @RequestParam(value = "company", required = false, defaultValue = "Deloitte") String company,
            @RequestParam(value = "role", required = false, defaultValue = "Associate Analyst / SDE-1") String role,
            @RequestParam(value = "city", required = false, defaultValue = "Bangalore") String city,
            @RequestParam(value = "bondMonths", required = false, defaultValue = "0") Integer bondMonths,
            @RequestParam(value = "bondPenalty", required = false, defaultValue = "0.0") Double bondPenalty,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        OfferAnalysisRequest req = OfferAnalysisRequest.builder()
                .companyName(company)
                .roleTitle(role)
                .city(city)
                .workMode("HYBRID")
                .ctcAnnualLpa(ctc)
                .basicSalaryLpa(ctc * 0.55)
                .variableBonusLpa(ctc * 0.12)
                .joiningBonusLpa(0.0)
                .esopAnnualLpa(0.0)
                .taxRegime("NEW")
                .bondDurationMonths(bondMonths)
                .bondPenaltyLakhs(bondPenalty)
                .certificateRetention(false)
                .trainingCostRecovery(false)
                .noticePeriodMonths(2)
                .nonCompeteClause(false)
                .build();

        OfferAnalysisResult result = offerAnalysisService.analyzeOffer(req);
        List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("req", req);
        model.addAttribute("result", result);
        model.addAttribute("companies", companies);
        model.addAttribute("pageTitle", "Offer In-Hand Breakdown & Bond Risk Analyzer");

        return "offers/analyzer";
    }

    @PostMapping("/calculate")
    public String calculateOffer(
            @ModelAttribute OfferAnalysisRequest request,
            Model model
    ) {
        if (request == null) {
            request = OfferAnalysisRequest.builder().build();
        }
        if (request.getCtcAnnualLpa() == null || request.getCtcAnnualLpa() <= 0) {
            request.setCtcAnnualLpa(6.0);
        }
        if (request.getTaxRegime() == null || request.getTaxRegime().isBlank()) {
            request.setTaxRegime("NEW");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            request.setCity("JAIPUR");
        }
        OfferAnalysisResult result = offerAnalysisService.analyzeOffer(request);
        model.addAttribute("req", request);
        model.addAttribute("result", result);

        return "offers/analyzer :: analysisResultsContainer";
    }

    @GetMapping("/compare")
    public String compareOffersPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        // Preset Offer 1: Deloitte USI (Bangalore)
        OfferAnalysisRequest offer1 = OfferAnalysisRequest.builder()
                .companyName("Deloitte USI")
                .roleTitle("Associate Analyst")
                .city("Bangalore")
                .workMode("HYBRID")
                .ctcAnnualLpa(9.5)
                .basicSalaryLpa(5.5)
                .variableBonusLpa(1.2)
                .joiningBonusLpa(0.5)
                .esopAnnualLpa(0.0)
                .taxRegime("NEW")
                .bondDurationMonths(0)
                .bondPenaltyLakhs(0.0)
                .certificateRetention(false)
                .trainingCostRecovery(false)
                .noticePeriodMonths(2)
                .nonCompeteClause(false)
                .build();

        // Preset Offer 2: Celebal Technologies (Jaipur)
        OfferAnalysisRequest offer2 = OfferAnalysisRequest.builder()
                .companyName("Celebal Technologies")
                .roleTitle("Cloud Associate")
                .city("Jaipur")
                .workMode("HYBRID")
                .ctcAnnualLpa(7.5)
                .basicSalaryLpa(4.5)
                .variableBonusLpa(0.8)
                .joiningBonusLpa(0.0)
                .esopAnnualLpa(0.0)
                .taxRegime("NEW")
                .bondDurationMonths(12)
                .bondPenaltyLakhs(1.0)
                .certificateRetention(false)
                .trainingCostRecovery(true)
                .noticePeriodMonths(1)
                .nonCompeteClause(false)
                .build();

        List<OfferAnalysisRequest> defaultList = List.of(offer1, offer2);
        OfferComparisonResult comparisonResult = offerAnalysisService.compareOffers(defaultList);

        model.addAttribute("offer1", offer1);
        model.addAttribute("offer2", offer2);
        model.addAttribute("comparison", comparisonResult);
        model.addAttribute("pageTitle", "Compare Job Offers Side-by-Side");

        return "offers/compare";
    }

    @PostMapping("/compare")
    public String evaluateComparison(
            @RequestParam(value = "company1", required = false, defaultValue = "Offer 1") String c1,
            @RequestParam(value = "ctc1", required = false, defaultValue = "8.0") Double ctc1,
            @RequestParam(value = "city1", required = false, defaultValue = "BANGALORE") String city1,
            @RequestParam(value = "bondMonths1", required = false, defaultValue = "0") Integer bond1,
            @RequestParam(value = "penalty1", required = false, defaultValue = "0.0") Double pen1,

            @RequestParam(value = "company2", required = false, defaultValue = "Offer 2") String c2,
            @RequestParam(value = "ctc2", required = false, defaultValue = "6.0") Double ctc2,
            @RequestParam(value = "city2", required = false, defaultValue = "JAIPUR") String city2,
            @RequestParam(value = "bondMonths2", required = false, defaultValue = "0") Integer bond2,
            @RequestParam(value = "penalty2", required = false, defaultValue = "0.0") Double pen2,

            Model model
    ) {
        if (ctc1 == null || ctc1 <= 0) ctc1 = 6.0;
        if (ctc2 == null || ctc2 <= 0) ctc2 = 6.0;
        if (bond1 == null || bond1 < 0) bond1 = 0;
        if (bond2 == null || bond2 < 0) bond2 = 0;
        if (pen1 == null || pen1 < 0) pen1 = 0.0;
        if (pen2 == null || pen2 < 0) pen2 = 0.0;
        if (c1 == null || c1.isBlank()) c1 = "Offer 1";
        if (c2 == null || c2.isBlank()) c2 = "Offer 2";
        if (city1 == null || city1.isBlank()) city1 = "BANGALORE";
        if (city2 == null || city2.isBlank()) city2 = "JAIPUR";

        OfferAnalysisRequest req1 = OfferAnalysisRequest.builder()
                .companyName(c1)
                .city(city1)
                .ctcAnnualLpa(ctc1)
                .basicSalaryLpa(ctc1 * 0.55)
                .variableBonusLpa(ctc1 * 0.12)
                .taxRegime("NEW")
                .bondDurationMonths(bond1)
                .bondPenaltyLakhs(pen1)
                .build();

        OfferAnalysisRequest req2 = OfferAnalysisRequest.builder()
                .companyName(c2)
                .city(city2)
                .ctcAnnualLpa(ctc2)
                .basicSalaryLpa(ctc2 * 0.55)
                .variableBonusLpa(ctc2 * 0.12)
                .taxRegime("NEW")
                .bondDurationMonths(bond2)
                .bondPenaltyLakhs(pen2)
                .build();

        OfferComparisonResult comparisonResult = offerAnalysisService.compareOffers(List.of(req1, req2));
        model.addAttribute("comparison", comparisonResult);

        return "offers/compare :: comparisonResultsContainer";
    }
}
