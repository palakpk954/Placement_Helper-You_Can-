package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByVerificationToken(String token);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRole(Role role);
    List<User> findTop10ByOrderByPointsDesc();
}
