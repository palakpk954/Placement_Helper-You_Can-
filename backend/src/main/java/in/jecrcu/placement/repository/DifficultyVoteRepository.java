package in.jecrcu.placement.repository;

import in.jecrcu.placement.entity.DifficultyVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DifficultyVoteRepository extends JpaRepository<DifficultyVote, Long> {

    Optional<DifficultyVote> findByCompanyIdAndUserId(Long companyId, Long userId);

    @Query("SELECT AVG(v.rating) FROM DifficultyVote v WHERE v.company.id = :companyId")
    Double findAverageRatingByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(v) FROM DifficultyVote v WHERE v.company.id = :companyId")
    Long countVotesByCompanyId(@Param("companyId") Long companyId);
}
