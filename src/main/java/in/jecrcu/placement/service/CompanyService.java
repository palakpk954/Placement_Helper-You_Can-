package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.DifficultyVote;
import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.JobType;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.DifficultyVoteRepository;
import in.jecrcu.placement.repository.DriveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DriveRepository driveRepository;
    private final DifficultyVoteRepository difficultyVoteRepository;

    public List<Company> getAllCompanies() {
        List<Company> list = companyRepository.findAll();
        list.forEach(this::enrichCompanyStats);
        return list;
    }

    public List<Company> filterCompanies(
            String keyword,
            Integer year,
            String month,
            String industry,
            String role,
            String jobType,
            String branch,
            String sort
    ) {
        List<Company> all = getAllCompanies();

        return all.stream()
                .filter(c -> {
                    // Keyword match (name or description)
                    if (keyword != null && !keyword.isBlank()) {
                        String k = keyword.toLowerCase().trim();
                        boolean matchesName = c.getName().toLowerCase().contains(k);
                        boolean matchesDesc = c.getDescription() != null && c.getDescription().toLowerCase().contains(k);
                        if (!matchesName && !matchesDesc) return false;
                    }

                    // Industry filter
                    if (industry != null && !industry.isBlank() && !industry.equalsIgnoreCase("ALL")) {
                        if (!c.getIndustry().equalsIgnoreCase(industry)) return false;
                    }

                    // Drive-level filters
                    List<Drive> drives = c.getDrives();
                    if (drives == null || drives.isEmpty()) {
                        return year == null && month == null && role == null && jobType == null && branch == null;
                    }

                    return drives.stream().anyMatch(d -> {
                        if (year != null && !d.getDriveYear().equals(year)) return false;
                        if (month != null && !month.isBlank() && !month.equalsIgnoreCase("ALL") &&
                                !d.getDriveMonth().equalsIgnoreCase(month)) return false;
                        if (role != null && !role.isBlank() && !role.equalsIgnoreCase("ALL") &&
                                !d.getRoles().toLowerCase().contains(role.toLowerCase())) return false;
                        if (jobType != null && !jobType.isBlank() && !jobType.equalsIgnoreCase("ALL")) {
                            if (!d.getJobType().name().equalsIgnoreCase(jobType)) return false;
                        }
                        if (branch != null && !branch.isBlank() && !branch.equalsIgnoreCase("ALL")) {
                            if (!d.getEligibleBranches().toLowerCase().contains(branch.toLowerCase())) return false;
                        }
                        return true;
                    });
                })
                .sorted((c1, c2) -> {
                    if ("frequent".equalsIgnoreCase(sort)) {
                        return Long.compare(c2.getTotalVisits() != null ? c2.getTotalVisits() : 0,
                                            c1.getTotalVisits() != null ? c1.getTotalVisits() : 0);
                    } else if ("difficulty".equalsIgnoreCase(sort)) {
                        double d1 = c1.getAverageDifficulty() != null ? c1.getAverageDifficulty() : 0.0;
                        double d2 = c2.getAverageDifficulty() != null ? c2.getAverageDifficulty() : 0.0;
                        return Double.compare(d2, d1);
                    } else if ("ctc".equalsIgnoreCase(sort)) {
                        return Double.compare(extractMaxCtc(c2), extractMaxCtc(c1));
                    } else {
                        // Default: most recent visit year/id
                        int y1 = getMostRecentYear(c1);
                        int y2 = getMostRecentYear(c2);
                        if (y1 != y2) return Integer.compare(y2, y1);
                        return c1.getName().compareToIgnoreCase(c2.getName());
                    }
                })
                .collect(Collectors.toList());
    }

    private void enrichCompanyStats(Company company) {
        Double avgDiff = difficultyVoteRepository.findAverageRatingByCompanyId(company.getId());
        company.setAverageDifficulty(avgDiff != null ? Math.round(avgDiff * 10.0) / 10.0 : 3.5);

        List<Drive> drives = company.getDrives();
        company.setTotalVisits((long) (drives != null ? drives.size() : 0));

        if (drives != null && !drives.isEmpty()) {
            Drive latest = drives.get(0);
            company.setMostRecentVisit(latest.getDriveMonth() + " " + latest.getDriveYear());
        } else {
            company.setMostRecentVisit("N/A");
        }
    }

    private int getMostRecentYear(Company c) {
        if (c.getDrives() != null && !c.getDrives().isEmpty()) {
            return c.getDrives().get(0).getDriveYear();
        }
        return 0;
    }

    public double extractMaxCtc(Company c) {
        if (c.getDrives() == null) return 0.0;
        double max = 0.0;
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        for (Drive d : c.getDrives()) {
            if (d.getCtcOrStipend() != null) {
                Matcher matcher = pattern.matcher(d.getCtcOrStipend());
                while (matcher.find()) {
                    try {
                        double val = Double.parseDouble(matcher.group(1));
                        if (val > max) max = val;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return max;
    }

    public Optional<Company> findById(Long id) {
        Optional<Company> opt = companyRepository.findById(id);
        opt.ifPresent(this::enrichCompanyStats);
        return opt;
    }

    public Optional<Company> findBySlug(String slug) {
        Optional<Company> opt = companyRepository.findBySlug(slug);
        opt.ifPresent(this::enrichCompanyStats);
        return opt;
    }

    @Transactional
    public void voteDifficulty(Long companyId, User user, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Optional<DifficultyVote> existing = difficultyVoteRepository.findByCompanyIdAndUserId(companyId, user.getId());
        if (existing.isPresent()) {
            DifficultyVote vote = existing.get();
            vote.setRating(rating);
            difficultyVoteRepository.save(vote);
        } else {
            DifficultyVote vote = DifficultyVote.builder()
                    .company(company)
                    .user(user)
                    .rating(rating)
                    .build();
            difficultyVoteRepository.save(vote);
        }
    }

    public Optional<DifficultyVote> getUserVote(Long companyId, Long userId) {
        return difficultyVoteRepository.findByCompanyIdAndUserId(companyId, userId);
    }

    public List<String> getDistinctIndustries() {
        return companyRepository.findDistinctIndustries();
    }

    public List<Integer> getDistinctYears() {
        return companyRepository.findDistinctYears();
    }

    public List<String> getDistinctMonths() {
        return companyRepository.findDistinctMonths();
    }

    @Transactional
    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
