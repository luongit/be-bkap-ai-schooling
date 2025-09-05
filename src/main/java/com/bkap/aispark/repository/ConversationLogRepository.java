package com.bkap.aispark.repository;

import com.bkap.aispark.entity.ConversationLog;
import com.bkap.aispark.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationLogRepository extends JpaRepository<ConversationLog, Long> {
	

	    List<ConversationLog> findByUserOrderByCreatedAtDesc(User user);
	

}



