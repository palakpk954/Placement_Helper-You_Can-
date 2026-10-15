package in.jecrcu.placement.controller;

import in.jecrcu.placement.entity.Badge;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.repository.BadgeRepository;
import in.jecrcu.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LeaderboardController {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;

    @GetMapping("/leaderboard")
    public String leaderboardPage(Model model) {
        List<User> topContributors = userRepository.findTop10ByOrderByPointsDesc();
        List<Badge> badges = badgeRepository.findAll();

        model.addAttribute("topContributors", topContributors);
        model.addAttribute("badges", badges);

        return "leaderboard/index";
    }
}
