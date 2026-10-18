package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.RoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {
    List<RoadmapItem> findByRoadmapIdOrderByWeekNumberAsc(Long roadmapId);
}
