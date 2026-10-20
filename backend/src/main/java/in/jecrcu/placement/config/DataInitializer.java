package in.jecrcu.placement.config;

import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final in.jecrcu.placement.repository.NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:./uploads/resumes}")
    private String uploadDir;

    @Override
    public void run(String... args) {
        // 1. Ensure file upload directory exists
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("Created upload directory: {} (success: {})", uploadDir, created);
            }
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
        } catch (Exception e) {
            log.error("Failed to initialize storage directories: {}", e.getMessage());
        }

        // 2. Ensure initial seed accounts have valid encoded passwords
        try {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                String expectedPassword = "password123";
                if ("admin@jecrcu.edu.in".equalsIgnoreCase(user.getEmail())) {
                    expectedPassword = "admin123";
                } else if ("rahul.sharma@jecrcu.edu.in".equalsIgnoreCase(user.getEmail()) ||
                           "priya.verma@jecrcu.edu.in".equalsIgnoreCase(user.getEmail())) {
                    expectedPassword = "student123";
                } else if ("alumni.amit@jecrcu.edu.in".equalsIgnoreCase(user.getEmail()) ||
                           "sneha.patel@jecrcu.edu.in".equalsIgnoreCase(user.getEmail())) {
                    expectedPassword = "mentor123";
                }

                // If password doesn't match expected BCrypt encoding, update it
                if (!passwordEncoder.matches(expectedPassword, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(expectedPassword));
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    log.info("Normalized seed password for user: {}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.warn("Could not check/update seed passwords: {}", e.getMessage());
        }

        // 3. Deduplicate any duplicate notifications by (userId, title, type)
        try {
            List<in.jecrcu.placement.entity.Notification> allNotifs = notificationRepository.findAll();
            java.util.Set<String> seen = new java.util.HashSet<>();
            List<in.jecrcu.placement.entity.Notification> duplicates = new java.util.ArrayList<>();
            // Sort by ID descending so we keep the newest
            allNotifs.sort((a, b) -> b.getId().compareTo(a.getId()));
            for (in.jecrcu.placement.entity.Notification n : allNotifs) {
                String key = (n.getUser() != null ? n.getUser().getId() : 0) + "|" + n.getTitle() + "|" + n.getNotificationType();
                if (!seen.add(key)) {
                    duplicates.add(n);
                }
            }
            if (!duplicates.isEmpty()) {
                notificationRepository.deleteAll(duplicates);
                log.info("Deduplicated {} redundant notifications on startup", duplicates.size());
            }
        } catch (Exception e) {
            log.warn("Could not deduplicate notifications: {}", e.getMessage());
        }
    }
}
