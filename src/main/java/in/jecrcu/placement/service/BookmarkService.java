package in.jecrcu.placement.service;

import in.jecrcu.placement.entity.Bookmark;
import in.jecrcu.placement.entity.Company;
import in.jecrcu.placement.entity.Question;
import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.ItemType;
import in.jecrcu.placement.repository.BookmarkRepository;
import in.jecrcu.placement.repository.CompanyRepository;
import in.jecrcu.placement.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CompanyRepository companyRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public boolean toggleBookmark(User user, ItemType type, Long itemId) {
        Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndItemTypeAndItemId(user.getId(), type, itemId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        } else {
            Bookmark b = Bookmark.builder()
                    .user(user)
                    .itemType(type)
                    .itemId(itemId)
                    .build();
            bookmarkRepository.save(b);
            return true;
        }
    }

    public boolean isBookmarked(Long userId, ItemType type, Long itemId) {
        if (userId == null) return false;
        return bookmarkRepository.existsByUserIdAndItemTypeAndItemId(userId, type, itemId);
    }

    public List<Company> getBookmarkedCompanies(Long userId) {
        List<Bookmark> list = bookmarkRepository.findByUserIdAndItemType(userId, ItemType.COMPANY);
        List<Company> companies = new ArrayList<>();
        for (Bookmark b : list) {
            companyRepository.findById(b.getItemId()).ifPresent(companies::add);
        }
        return companies;
    }

    public List<Question> getBookmarkedQuestions(Long userId) {
        List<Bookmark> list = bookmarkRepository.findByUserIdAndItemType(userId, ItemType.QUESTION);
        List<Question> questions = new ArrayList<>();
        for (Bookmark b : list) {
            questionRepository.findById(b.getItemId()).ifPresent(questions::add);
        }
        return questions;
    }
}
