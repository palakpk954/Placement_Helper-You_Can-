package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Roadmap;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    @EntityGraph(attributePaths = {"company", "items"})
    List<Roadmap> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"company", "items"})
    Optional<Roadmap> findById(Long id);

    @EntityGraph(attributePaths = {"company", "items"})
    Optional<Roadmap> findByUserIdAndCompanyId(Long userId, Long companyId);
}
