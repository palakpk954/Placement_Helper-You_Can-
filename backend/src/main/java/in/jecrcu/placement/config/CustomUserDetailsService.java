package in.jecrcu.placement.config;

import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.ExperienceRepository;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean hasApprovedExp = experienceRepository.countApprovedByAuthorId(user.getId()) > 0;
        return new CustomUserDetails(user, hasApprovedExp);
    }
}
