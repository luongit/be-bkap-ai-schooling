package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_logs")
public class ConversationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;   // tin nhắn user gửi

    @Column(columnDefinition = "TEXT")
    private String response;  // phản hồi AI

    @Column(name = "violation_flag")
    private Boolean violationFlag = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    
    // ===== Constructor =====
    public ConversationLog() {}

    
   

	public ConversationLog(Long id, User user, String message, String response, Boolean violationFlag,
			LocalDateTime createdAt, UUID sessionId) {
		super();
		this.id = id;
		this.user = user;
		this.message = message;
		this.response = response;
		this.violationFlag = violationFlag;
		this.createdAt = createdAt;
		this.sessionId = sessionId;
	}




	// ===== Getter/Setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Boolean getViolationFlag() {
        return violationFlag;
    }

    public void setViolationFlag(Boolean violationFlag) {
        this.violationFlag = violationFlag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }




	public UUID getSessionId() {
		return sessionId;
	}




	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}
    
}
