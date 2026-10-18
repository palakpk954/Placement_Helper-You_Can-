package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.Round;
import in.jecrcu.placement.repository.DriveRepository;
import in.jecrcu.placement.repository.QuestionRepository;
import in.jecrcu.placement.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final RoundRepository roundRepository;
    private final QuestionRepository questionRepository;

    public List<Drive> getAllDrives() {
        return driveRepository.findAllOrdered();
    }

    public Optional<Drive> findById(Long id) {
        return driveRepository.findById(id);
    }

    @Transactional
    public Drive saveDrive(Drive drive) {
        return driveRepository.save(drive);
    }

    @Transactional
    public void deleteDrive(Long id) {
        driveRepository.deleteById(id);
    }

    public List<Round> getRoundsForDrive(Long driveId) {
        return roundRepository.findByDriveIdOrderByRoundNumberAsc(driveId);
    }

    @Transactional
    public Round saveRound(Round round) {
        return roundRepository.save(round);
    }

    @Transactional
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    public Optional<Question> findQuestionById(Long id) {
        return questionRepository.findById(id);
    }
}
