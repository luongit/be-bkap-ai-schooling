package com.bkap.aispark.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.UserImageLibraryCapacity;
import com.bkap.aispark.repository.UserImageLibraryCapacityRepository;

@Service
public class ImageLibraryService {

    private final UserImageLibraryCapacityRepository repo;

    // Dung lượng mặc định khi user mới sử dụng (10 ảnh free)
    private static final int DEFAULT_CAPACITY = 10;

    // Mỗi lần user mua thêm thì tăng thêm 5 slot
    private static final int SLOT_SIZE = 5;

    public ImageLibraryService(UserImageLibraryCapacityRepository repo) {
        this.repo = repo;
    }

    /**
     * Lấy thông tin dung lượng thư viện ảnh của user.
     * Nếu chưa có trong DB thì tạo mới với:
     * - capacity = 10 (mặc định)
     * - used = 0 (chưa dùng slot nào)
     */
    public UserImageLibraryCapacity getOrCreate(Long userId) {
        return repo.findById(userId).orElseGet(() -> {
            UserImageLibraryCapacity lib =
                    new UserImageLibraryCapacity(userId, DEFAULT_CAPACITY, 0);
            return repo.save(lib);
        });
    }

    /**
     * Kiểm tra xem user còn slot để lưu ảnh không.
     * Trả về true nếu used < capacity.
     */
    public boolean canStore(Long userId) {
        UserImageLibraryCapacity lib = getOrCreate(userId);
        return lib.getUsed() < lib.getCapacity();
    }

    /**
     * Tăng số lượng ảnh đã sử dụng lên 1.
     * Dùng khi user upload ảnh thành công.
     */
    public void incrementUsed(Long userId) {
        UserImageLibraryCapacity lib = getOrCreate(userId);
        lib.setUsed(lib.getUsed() + 1);
        lib.setUpdatedAt(LocalDateTime.now()); // cập nhật thời gian chỉnh sửa
        repo.save(lib);
    }

    /**
     * Giảm số lượng ảnh đã sử dụng đi 1.
     * Không cho phép giảm dưới 0.
     * Dùng khi user xoá ảnh.
     */
    public void decrementUsed(Long userId) {
        UserImageLibraryCapacity lib = getOrCreate(userId);
        lib.setUsed(Math.max(0, lib.getUsed() - 1));
        lib.setUpdatedAt(LocalDateTime.now());
        repo.save(lib);
    }

    /**
     * Tăng dung lượng thư viện lên SLOT_SIZE (mỗi lần +5).
     * Dùng khi user mua thêm dung lượng.
     */
    public void increaseCapacity(Long userId) {
        UserImageLibraryCapacity lib = getOrCreate(userId);
        lib.setCapacity(lib.getCapacity() + SLOT_SIZE);
        lib.setUpdatedAt(LocalDateTime.now());
        repo.save(lib);
    }
}
