package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Drive;
import in.jecrcu.placement.entity.UpcomingDrive;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.DriveRepository;
import in.jecrcu.placement.repository.UpcomingDriveRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final UpcomingDriveRepository upcomingDriveRepository;
    private final DriveRepository driveRepository;

    @Getter
    public static class EligibilityResult<T> {
        private final T target;
        private final boolean eligible;
        private final List<String> exclusionReasons;

        public EligibilityResult(T target, boolean eligible, List<String> exclusionReasons) {
            this.target = target;
            this.eligible = eligible;
            this.exclusionReasons = exclusionReasons;
        }

        public T getDrive() {
            return target;
        }

        public T getUpcomingDrive() {
            return target;
        }

        public List<String> getFailureReasons() {
            return exclusionReasons;
        }
    }

    public boolean isEligibleForUpcoming(User user, UpcomingDrive drive) {
        return checkUpcomingEligibility(user.getBranch(), user.getCgpa(), user.getActiveBacklogs(), drive).isEligible();
    }

    public EligibilityResult<UpcomingDrive> checkUpcomingEligibility(
            String branch, BigDecimal cgpa, Integer backlogs, UpcomingDrive drive) {

        List<String> reasons = new ArrayList<>();

        if (cgpa != null && drive.getMinCgpa() != null) {
            if (cgpa.compareTo(drive.getMinCgpa()) < 0) {
                reasons.add("Your CGPA (" + cgpa + ") is below cutoff (" + drive.getMinCgpa() + ")");
            }
        }

        if (branch != null && drive.getEligibleBranches() != null) {
            String branchCode = branch.split(" ")[0].replaceAll("[^a-zA-Z]", "").toLowerCase();
            String eligible = drive.getEligibleBranches().toLowerCase();
            if (!eligible.contains("all") && !eligible.contains(branchCode) && !eligible.contains(branch.toLowerCase().trim())) {
                reasons.add("Branch '" + branch + "' is not eligible. Required: " + drive.getEligibleBranches());
            }
        }

        int userBacklogs = backlogs != null ? backlogs : 0;
        int maxAllowed = drive.getMaxBacklogs() != null ? drive.getMaxBacklogs() : 0;
        if (userBacklogs > maxAllowed) {
            reasons.add("Active backlogs (" + userBacklogs + ") exceed maximum permitted (" + maxAllowed + ")");
        }

        return new EligibilityResult<>(drive, reasons.isEmpty(), reasons);
    }

    public EligibilityResult<Drive> checkPastDriveEligibility(
            String branch, BigDecimal cgpa, Integer backlogs, Drive drive) {

        List<String> reasons = new ArrayList<>();

        if (cgpa != null && drive.getMinCgpa() != null) {
            if (cgpa.compareTo(drive.getMinCgpa()) < 0) {
                reasons.add("CGPA (" + cgpa + ") is below cutoff (" + drive.getMinCgpa() + ")");
            }
        }

        if (branch != null && drive.getEligibleBranches() != null) {
            String branchCode = branch.split(" ")[0].replaceAll("[^a-zA-Z]", "").toLowerCase();
            String eligible = drive.getEligibleBranches().toLowerCase();
            if (!eligible.contains("all") && !eligible.contains(branchCode) && !eligible.contains(branch.toLowerCase().trim())) {
                reasons.add("Branch '" + branch + "' not in eligible branches (" + drive.getEligibleBranches() + ")");
            }
        }

        int userBacklogs = backlogs != null ? backlogs : 0;
        int maxAllowed = drive.getMaxBacklogs() != null ? drive.getMaxBacklogs() : 0;
        if (userBacklogs > maxAllowed) {
            reasons.add("Active backlogs (" + userBacklogs + ") exceed max allowed (" + maxAllowed + ")");
        }

        return new EligibilityResult<>(drive, reasons.isEmpty(), reasons);
    }

    public List<EligibilityResult<UpcomingDrive>> evaluateAllUpcoming(String branch, BigDecimal cgpa, Integer backlogs) {
        List<UpcomingDrive> allUpcoming = upcomingDriveRepository.findByActiveTrueOrderByDriveDateAsc();
        List<EligibilityResult<UpcomingDrive>> list = new ArrayList<>();
        for (UpcomingDrive d : allUpcoming) {
            list.add(checkUpcomingEligibility(branch, cgpa, backlogs, d));
        }
        return list;
    }

    public List<EligibilityResult<Drive>> evaluateAllPast(String branch, BigDecimal cgpa, Integer backlogs) {
        List<Drive> allDrives = driveRepository.findAllOrdered();
        List<EligibilityResult<Drive>> list = new ArrayList<>();
        for (Drive d : allDrives) {
            list.add(checkPastDriveEligibility(branch, cgpa, backlogs, d));
        }
        return list;
    }
}
