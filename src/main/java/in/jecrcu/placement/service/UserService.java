package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.NotificationPreference;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Role;
import in.jecrcu.placement.repository.NotificationPreferenceRepository;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.allowed-domains:jecrcu.edu.in,university.edu}")
    private String allowedDomains;

    @Value("${app.security.require-email-verification:false}")
    private boolean requireEmailVerification;

    public boolean isDomainAllowed(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();
        List<String> validDomains = Arrays.stream(allowedDomains.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        return validDomains.contains(domain);
    }

    @Transactional
    public User registerUser(String email, String rawPassword, String fullName, String branch,
                             Integer graduationYear, BigDecimal cgpa, Integer backlogs) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account already exists with this email address.");
        }
        if (!isDomainAllowed(email)) {
            throw new IllegalArgumentException("Registration is restricted to university email IDs (" + allowedDomains + ")");
        }

        String verificationToken = UUID.randomUUID().toString();
        User user = User.builder()
                .email(email.toLowerCase().trim())
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName.trim())
                .branch(branch)
                .graduationYear(graduationYear)
                .cgpa(cgpa)
                .activeBacklogs(backlogs != null ? backlogs : 0)
                .role(Role.ROLE_STUDENT)
                .emailVerified(!requireEmailVerification)
                .verificationToken(verificationToken)
                .tokenExpiry(LocalDateTime.now().plusHours(24))
                .points(10)
                .build();

        User saved = userRepository.save(user);

        // Create default notification preferences
        NotificationPreference pref = NotificationPreference.builder()
                .user(saved)
                .emailDrives(true)
                .emailDeadlines(true)
                .emailExperiences(true)
                .emailBookings(true)
                .build();
        preferenceRepository.save(pref);

        log.info("New student registered: {} (emailVerified: {})", saved.getEmail(), saved.getEmailVerified());
        return saved;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getTokenExpiry() != null && user.getTokenExpiry().isAfter(LocalDateTime.now())) {
                user.setEmailVerified(true);
                user.setVerificationToken(null);
                user.setTokenExpiry(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void updateProfile(Long userId, String branch, Integer graduationYear, BigDecimal cgpa, Integer backlogs) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBranch(branch);
        user.setGraduationYear(graduationYear);
        user.setCgpa(cgpa);
        user.setActiveBacklogs(backlogs != null ? backlogs : 0);
        userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional
    public void addPoints(Long userId, int pointsToAdd) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setPoints((u.getPoints() != null ? u.getPoints() : 0) + pointsToAdd);
            userRepository.save(u);
        });
    }
}
