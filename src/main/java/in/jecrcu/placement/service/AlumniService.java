package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.AlumniProfile;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.AlumniProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlumniService {

    private final AlumniProfileRepository alumniProfileRepository;

    public List<AlumniProfile> getAllVisibleAlumni() {
        return alumniProfileRepository.findByVisibleTrueOrderByGraduationYearDesc();
    }

    public List<AlumniProfile> searchAlumni(String company, String branch, Integer year) {
        String compFilter = (company != null && !company.isBlank() && !company.equalsIgnoreCase("ALL")) ? company.trim() : null;
        String branchFilter = (branch != null && !branch.isBlank() && !branch.equalsIgnoreCase("ALL")) ? branch.trim() : null;
        return alumniProfileRepository.searchAlumni(compFilter, branchFilter, year);
    }

    public Optional<AlumniProfile> getProfileByUserId(Long userId) {
        return alumniProfileRepository.findByUserId(userId);
    }

    @Transactional
    public AlumniProfile saveProfile(User user, String companyName, String jobRole,
                                     Integer gradYear, String branch, String linkedinUrl,
                                     String topicsCanHelp, boolean visible) {
        Optional<AlumniProfile> existing = alumniProfileRepository.findByUserId(user.getId());
        AlumniProfile profile;
        if (existing.isPresent()) {
            profile = existing.get();
            profile.setCompanyName(companyName);
            profile.setJobRole(jobRole);
            profile.setGraduationYear(gradYear);
            profile.setBranch(branch);
            profile.setLinkedinUrl(linkedinUrl);
            profile.setTopicsCanHelp(topicsCanHelp);
            profile.setVisible(visible);
        } else {
            profile = AlumniProfile.builder()
                    .user(user)
                    .companyName(companyName)
                    .jobRole(jobRole)
                    .graduationYear(gradYear)
                    .branch(branch)
                    .linkedinUrl(linkedinUrl)
                    .topicsCanHelp(topicsCanHelp)
                    .visible(visible)
                    .build();
        }
        return alumniProfileRepository.save(profile);
    }
}
