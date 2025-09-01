package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.DefaultReply;

@Repository
public interface DefaultReplyRepository extends JpaRepository<DefaultReply, Long> {
}   