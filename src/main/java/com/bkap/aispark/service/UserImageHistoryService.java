package com.bkap.aispark.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.aispark.entity.UserImageHistory;
import com.bkap.aispark.repository.UserImageHistoryRepository;

@Service
public class UserImageHistoryService {

	private final UserImageHistoryRepository imageRepo;

	private final R2StorageService r2;

	public UserImageHistoryService(UserImageHistoryRepository imageRepo, R2StorageService r2) { 
																								
		this.imageRepo = imageRepo;
		this.r2 = r2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(Long userId, String prompt, String style, String size, String imageUrl, String status,
			String errorMessage) {
		try {
			UserImageHistory history = new UserImageHistory();
			history.setUserId(userId);
			history.setPrompt(prompt);
			history.setStyle(style);
			history.setSize(size);
			history.setImageUrl(imageUrl); // 👈 QUAN TRỌNG!
			history.setStatus(status);
			history.setErrorMessage(errorMessage);

			imageRepo.save(history);

		} catch (Exception e) {
			System.err.println("❌ Failed to save image history: " + e.getMessage());
		}
	}

	public List<UserImageHistory> getHistory(Long userId) {
		return imageRepo.findByUserIdOrderByCreatedAtDesc(userId);
	}

	@Transactional
	public boolean deleteImage(Long userId, Long imageId) {

	    UserImageHistory img = imageRepo.findById(imageId).orElse(null);
	    if (img == null) return false;
	    if (!img.getUserId().equals(userId)) return false;

	    //  Xác định ảnh có phải ảnh thành công hay không
	    boolean shouldDecrement = img.getImageUrl() != null && !img.getImageUrl().isBlank();

	    // Xóa file từ R2 nếu có file thật
	    try {
	        if (img.getImageUrl() != null) {
	            r2.deleteFile(img.getImageUrl());
	        }
	    } catch (Exception ignored) {}

	    // Xóa DB record
	    imageRepo.delete(img);

	    //  CHỈ GIẢM SLOT NẾU ẢNH LÀ ẢNH THẬT
	    return shouldDecrement;
	}


}
