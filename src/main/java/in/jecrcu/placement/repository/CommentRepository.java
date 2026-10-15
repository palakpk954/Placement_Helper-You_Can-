package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCompanyIdAndParentIsNullAndStatusOrderByCreatedAtDesc(Long companyId, String status);
    List<Comment> findByStatusOrderByCreatedAtDesc(String status);
}
