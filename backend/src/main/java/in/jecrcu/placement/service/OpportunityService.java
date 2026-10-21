package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Opportunity;
import in.jecrcu.placement.entity.enums.OpportunityType;
import in.jecrcu.placement.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    public List<Opportunity> getOpportunities(OpportunityType type, String query) {
        boolean hasQuery = (query != null && !query.trim().isEmpty());
        String cleanQuery = hasQuery ? query.trim() : null;

        if (type != null && hasQuery) {
            return opportunityRepository.searchActiveByType(type, cleanQuery);
        } else if (type != null) {
            return opportunityRepository.findByActiveTrueAndTypeOrderByFeaturedDescDeadlineAsc(type);
        } else if (hasQuery) {
            return opportunityRepository.searchActive(cleanQuery);
        } else {
            return opportunityRepository.findByActiveTrueOrderByFeaturedDescDeadlineAsc();
        }
    }

    public List<Opportunity> getTopTrending() {
        return opportunityRepository.findTop4ByActiveTrueOrderByFeaturedDescDeadlineAsc();
    }

    public List<Opportunity> getAllForAdmin() {
        return opportunityRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Opportunity> findById(Long id) {
        return opportunityRepository.findById(id);
    }

    @Transactional
    public Opportunity saveOpportunity(Opportunity opportunity) {
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public void deleteOpportunity(Long id) {
        opportunityRepository.deleteById(id);
    }

    @Transactional
    public boolean toggleActive(Long id) {
        Opportunity opp = opportunityRepository.findById(id).orElse(null);
        if (opp != null) {
            opp.setActive(!Boolean.TRUE.equals(opp.getActive()));
            opportunityRepository.save(opp);
            return opp.getActive();
        }
        return false;
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalActive", opportunityRepository.countByActiveTrue());
        stats.put("totalHackathons", opportunityRepository.countByTypeAndActiveTrue(OpportunityType.HACKATHON));
        stats.put("totalInternships", opportunityRepository.countByTypeAndActiveTrue(OpportunityType.INTERNSHIP));
        stats.put("totalChallenges", opportunityRepository.countByTypeAndActiveTrue(OpportunityType.HIRING_CHALLENGE));
        stats.put("totalScholarships", opportunityRepository.countByTypeAndActiveTrue(OpportunityType.SCHOLARSHIP));
        return stats;
    }
}
