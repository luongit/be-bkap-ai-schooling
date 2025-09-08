package com.bkap.aispark.repository;

import com.bkap.aispark.entity.ConversationLog;
import com.bkap.aispark.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationLogRepository extends JpaRepository<ConversationLog, Long> {
	

	    List<ConversationLog> findByUserOrderByCreatedAtDesc(User user);
	    
	    List<ConversationLog> findByUserAndSessionIdOrderByCreatedAt(User user, UUID sessionId);
	
	    @Query("SELECT c.sessionId as sessionId, MIN(c.createdAt) as createdAt " +
	    	       "FROM ConversationLog c " +
	    	       "WHERE c.user.id = :userId " +
	    	       "GROUP BY c.sessionId " +
	    	       "ORDER BY MIN(c.createdAt) DESC")
	    	List<Object[]> findDistinctSessionsByUser(Long userId);
	    	Optional<ConversationLog> findTopByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, UUID sessionId);

}



