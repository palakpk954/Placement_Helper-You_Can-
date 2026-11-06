package in.jecrcu.placement.config;

import in.jecrcu.placement.entity.StudentPlacementRecord;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Role;
import in.jecrcu.placement.repository.AlumniProfileRepository;
import in.jecrcu.placement.repository.ExperienceRepository;
import in.jecrcu.placement.repository.StudentPlacementRecordRepository;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final StudentPlacementRecordRepository studentPlacementRecordRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String cleanId = identifier != null ? identifier.trim() : "";
        User user = null;

        if (cleanId.contains("@")) {
            user = userRepository.findByEmailIgnoreCase(cleanId).orElse(null);
        } else {
            // Identifier is a Roll Number (e.g. 23BCON1402)
            Optional<StudentPlacementRecord> spr = studentPlacementRecordRepository.findByRollNumber(cleanId.toUpperCase());
            if (spr.isPresent() && spr.get().getEmail() != null) {
                user = userRepository.findByEmailIgnoreCase(spr.get().getEmail()).orElse(null);
            }
            if (user == null) {
                user = userRepository.findFirstByEmailContainingIgnoreCase(cleanId).orElse(null);
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found with identifier: " + identifier);
        }

        boolean hasApprovedExp = experienceRepository.countApprovedByAuthorId(user.getId()) > 0;
        boolean isMentor = user.getRole() == Role.ROLE_ALUMNI;
        return new CustomUserDetails(user, hasApprovedExp, isMentor);
    }
}
