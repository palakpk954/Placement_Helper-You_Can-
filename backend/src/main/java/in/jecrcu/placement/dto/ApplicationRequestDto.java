package in.jecrcu.placement.dto;

import in.jecrcu.placement.entity.enums.ApplicationStage;
import in.jecrcu.placement.entity.enums.ApplicationType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {
    private Long id;
    private Long companyId;
    private Long upcomingDriveId;
    private String companyName;
    private String roleTitle;
    private String ctcOrStipend;
    private ApplicationType applicationType;
    private ApplicationStage stage;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime nextEventDate;

    private String nextEventTitle;
    private String interviewLocation;
    private String notes;
    private Integer rating;
}
