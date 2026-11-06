package in.jecrcu.placement.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferComparisonResult {
    private List<OfferAnalysisResult> offers;
    private String winnerCompany;
    private String winnerReason;
    private String bestCashInHandCompany;
    private String bestWorkLifeBalanceCompany;
    private String safestAgreementCompany;
}
