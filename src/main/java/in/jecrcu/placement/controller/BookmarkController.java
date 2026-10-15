package in.jecrcu.placement.controller;

import in.jecrcu.placement.config.CustomUserDetails;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ItemType;
import in.jecrcu.placement.service.BookmarkService;
import in.jecrcu.placement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    @PostMapping("/bookmarks/toggle")
    @ResponseBody
    public String toggleBookmark(
            @RequestParam("itemType") String itemTypeStr,
            @RequestParam("itemId") Long itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return "<span class=\"text-danger\">Login required</span>";
        }

        User user = userService.findById(userDetails.getId()).orElse(null);
        if (user == null) {
            return "<span class=\"text-danger\">User not found</span>";
        }

        ItemType type = ItemType.COMPANY;
        try {
            type = ItemType.valueOf(itemTypeStr.toUpperCase());
        } catch (Exception ignored) {}

        boolean isBookmarked = bookmarkService.toggleBookmark(user, type, itemId);
        if (isBookmarked) {
            return "<i class=\"bi bi-bookmark-fill text-warning me-1\"></i> Bookmarked";
        } else {
            return "<i class=\"bi bi-bookmark me-1\"></i> Bookmark";
        }
    }
}
