package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.NotificationPreference;
import in.jecrcu.placement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUser(User user);
    Optional<NotificationPreference> findByUserId(Long userId);
}
