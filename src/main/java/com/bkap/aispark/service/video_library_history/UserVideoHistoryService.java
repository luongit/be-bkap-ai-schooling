package com.bkap.aispark.service.video_library_history;
import com.bkap.aispark.entity.video_library_history.UserVideoHistory;
import com.bkap.aispark.repository.video_library_history.UserVideoHistoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional; // ← ĐẢM BẢO CÓ DÒNG NÀY Ở TRÊN CÙNG
@Service
public class UserVideoHistoryService {

    private final UserVideoHistoryRepository repo;

    public UserVideoHistoryService(UserVideoHistoryRepository repo) {
        this.repo = repo;
    }

    public List<UserVideoHistory> getHistory(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public UserVideoHistory save(UserVideoHistory video) {
        return repo.save(video);
    }
    @Transactional
    public boolean delete(Long userId, Long videoId) {
        if (repo.existsByUserIdAndId(userId, videoId)) {
            repo.deleteByUserIdAndId(userId, videoId);
            return true;
        }
        return false;
    }
}