package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Bookmark;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);

    List<Bookmark> findByUserIdAndItemType(Long userId, ItemType itemType);

    Optional<Bookmark> findByUserIdAndItemTypeAndItemId(Long userId, ItemType itemType, Long itemId);

    boolean existsByUserIdAndItemTypeAndItemId(Long userId, ItemType itemType, Long itemId);

    void deleteByUserIdAndItemTypeAndItemId(Long userId, ItemType itemType, Long itemId);
}
