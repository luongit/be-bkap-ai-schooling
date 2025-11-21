package com.bkap.aispark.service.video_library_history;

import com.bkap.aispark.entity.video_library_history.UserVideoLibraryCapacity;
import com.bkap.aispark.repository.video_library_history.UserVideoLibraryCapacityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserVideoLibraryService {

    private final UserVideoLibraryCapacityRepository repo;

    public UserVideoLibraryService(UserVideoLibraryCapacityRepository repo) {
        this.repo = repo;
    }

    /**
     * Lấy thông tin dung lượng thư viện video của user
     * Nếu chưa có → tự động tạo mới với 5 slot
     */
    @Transactional(readOnly = true)
    public UserVideoLibraryCapacity getOrCreate(Long userId) {
        return repo.findById(userId).orElseGet(() -> {
            UserVideoLibraryCapacity newCapacity = new UserVideoLibraryCapacity();
            newCapacity.setUserId(userId);
            newCapacity.setCapacity(5);
            newCapacity.setUsed(0);
            return repo.save(newCapacity); // lưu luôn vào DB
        });
    }

    /**
     * Kiểm tra user còn slot còn trống không
     */
    @Transactional(readOnly = true)
    public boolean canStore(Long userId) {
        UserVideoLibraryCapacity info = getOrCreate(userId);
        return info.getUsed() < info.getCapacity();
    }

    /**
     * Tăng số video đã dùng +1 (khi tạo video thành công)
     */
    @Transactional
    public void incrementUsed(Long userId) {
        UserVideoLibraryCapacity info = getOrCreate(userId);
        if (info.getUsed() < info.getCapacity()) {
            info.setUsed(info.getUsed() + 1);
            repo.save(info);
        }
        // Nếu đã full thì không tăng nữa (tránh lỗi)
    }

    /**
     * Giảm số video đã dùng -1 (khi xóa video)
     */
    @Transactional
    public void decrementUsed(Long userId) {
        UserVideoLibraryCapacity info = getOrCreate(userId);
        if (info.getUsed() > 0) {
            info.setUsed(info.getUsed() - 1);
            repo.save(info);
        }
    }

    /**
     * Mua thêm 5 slot (khi user thanh toán)
     */
    @Transactional
    public void increaseCapacity(Long userId) {
        UserVideoLibraryCapacity info = getOrCreate(userId);
        info.setCapacity(info.getCapacity() + 5);
        repo.save(info);
    }

    /**
     * Reset lại dung lượng (dùng khi admin reset hoặc test)
     */
    @Transactional
    public void reset(Long userId) {
        UserVideoLibraryCapacity info = getOrCreate(userId);
        info.setUsed(0);
        info.setCapacity(5);
        repo.save(info);
    }
}