package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.AuditLog;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(User actor, String action, String entityType, Long entityId, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType);
    }
}
