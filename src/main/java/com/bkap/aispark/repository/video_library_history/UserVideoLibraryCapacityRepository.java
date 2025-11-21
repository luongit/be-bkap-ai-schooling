package com.bkap.aispark.repository.video_library_history;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.video_library_history.UserVideoLibraryCapacity;

public interface UserVideoLibraryCapacityRepository 
        extends JpaRepository<UserVideoLibraryCapacity, Long> {
}
