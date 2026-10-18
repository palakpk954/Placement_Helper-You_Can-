package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Resume;
import in.jecrcu.placement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findTopByUserOrderByUploadedAtDesc(User user);
    Optional<Resume> findTopByUserIdOrderByUploadedAtDesc(Long userId);
    List<Resume> findByShareConsentTrueOrderByUploadedAtDesc();
}
