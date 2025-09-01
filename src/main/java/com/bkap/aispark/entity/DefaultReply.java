package com.bkap.aispark.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "default_replies")
public class DefaultReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reply_text", nullable = false)
    private String replyText;

    @Column(name = "created_by", nullable = true)
    private Long createdBy; // có thể null

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
