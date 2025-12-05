package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.Conversation;
import com.lp.criticabackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationAndDeletedFalseOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);
}
