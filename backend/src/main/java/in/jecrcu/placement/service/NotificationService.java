package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Notification;
import in.jecrcu.placement.entity.NotificationPreference;
import in.jecrcu.placement.entity.UpcomingDrive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.NotificationPreferenceRepository;
import in.jecrcu.placement.repository.NotificationRepository;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UpcomingDriveRepository upcomingDriveRepository;
    private final UserRepository userRepository;

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void sendNotification(User user, String title, String message, String link, String type) {
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .link(link)
                .notificationType(type)
                .read(false)
                .build();
        notificationRepository.save(n);
        log.info("Notification sent to {}: {}", user.getEmail(), title);
    }

    @Transactional
    public void notifyStudentsOfNewUpcomingDrive(UpcomingDrive drive) {
        List<User> students = userRepository.findAll();
        for (User student : students) {
            sendNotification(
                    student,
                    "New Recruitment Drive: " + drive.getCompanyName(),
                    drive.getCompanyName() + " announced campus recruitment for " + drive.getRolesOffered() + " (" + drive.getCtcOrStipend() + ")",
                    "/upcoming",
                    "NEW_UPCOMING_DRIVE"
            );
        }
    }

    @Transactional
    public void notifyStudentsOfNewOpportunity(in.jecrcu.placement.entity.Opportunity opp) {
        List<User> students = userRepository.findAll();
        String deadlineStr = opp.getDeadline() != null ? opp.getDeadline().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")) : "Soon";
        for (User student : students) {
            sendNotification(
                    student,
                    "New " + (opp.getType() != null ? opp.getType().getDisplayName() : "Opportunity") + ": " + opp.getTitle(),
                    opp.getOrganization() + " announced: " + opp.getTitle() + " (" + opp.getCompensationOrPrize() + "). Apply by " + deadlineStr + "!",
                    "/opportunities",
                    "OPPORTUNITY_ALERT"
            );
        }
    }

    @Transactional
    public void notifySubmissionOutcome(User author, String companyName, boolean approved, String reason) {
        if (approved) {
            sendNotification(
                    author,
                    "Experience Approved! (+50 pts)",
                    "Your placement experience for " + companyName + " has been approved and published to the company archive.",
                    "/dashboard",
                    "EXPERIENCE_APPROVED"
            );
        } else {
            sendNotification(
                    author,
                    "Experience Submission Update",
                    "Your experience for " + companyName + " was not approved. Coordinator note: " + (reason != null ? reason : "Needs revision"),
                    "/contribute",
                    "EXPERIENCE_REJECTED"
            );
        }
    }

    @Transactional
    public void notifyMockBooking(User student, User mentor, String companyName, boolean booked) {
        if (booked) {
            sendNotification(
                    mentor,
                    "New Mock Interview Booked",
                    student.getFullName() + " booked a mock interview session for " + companyName,
                    "/mocks",
                    "MOCK_BOOKED"
            );
            sendNotification(
                    student,
                    "Mock Interview Confirmed",
                    "Your mock session with " + mentor.getFullName() + " for " + companyName + " has been confirmed.",
                    "/mocks",
                    "MOCK_BOOKED"
            );
        } else {
            sendNotification(
                    mentor,
                    "Mock Interview Cancelled",
                    "The mock interview session for " + companyName + " was cancelled.",
                    "/mocks",
                    "MOCK_CANCELLED"
            );
        }
    }

    public Optional<NotificationPreference> getPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId);
    }

    @Transactional
    public void updatePreferences(Long userId, boolean drives, boolean deadlines, boolean exp, boolean bookings) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.builder()
                        .user(userRepository.findById(userId).orElseThrow())
                        .build());
        pref.setEmailDrives(drives);
        pref.setEmailDeadlines(deadlines);
        pref.setEmailExperiences(exp);
        pref.setEmailBookings(bookings);
        preferenceRepository.save(pref);
    }

    // Runs once every hour to detect deadlines within 48 hours
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime window = now.plusHours(48);

        List<UpcomingDrive> closingSoon = upcomingDriveRepository.findDrivesWithDeadlineApproaching(now, window);
        if (!closingSoon.isEmpty()) {
            List<User> students = userRepository.findAll();
            for (UpcomingDrive drive : closingSoon) {
                String title = "Deadline Reminder: " + drive.getCompanyName();
                for (User student : students) {
                    // Check if reminder was already sent to avoid duplicate hourly alerts
                    if (!notificationRepository.existsByUserIdAndNotificationTypeAndTitle(student.getId(), "DEADLINE_REMINDER", title)) {
                        sendNotification(
                                student,
                                title,
                                "Registration for " + drive.getCompanyName() + " (" + drive.getRolesOffered() + ") closes within 48 hours!",
                                "/upcoming",
                                "DEADLINE_REMINDER"
                        );
                    }
                }
            }
        }
    }

    @Transactional
    public void markSingleAsRead(Long notificationId, Long userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public Optional<Notification> markSingleAsReadAndGet(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId).map(n -> {
            n.setRead(true);
            return notificationRepository.save(n);
        });
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId).ifPresent(notificationRepository::delete);
    }
}
