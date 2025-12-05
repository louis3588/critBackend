package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.Conversation;
import com.lp.criticabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // Find the conversation for two users (unordered)
    @Query("SELECT c FROM Conversation c WHERE (c.userOne = :a AND c.userTwo = :b) OR (c.userOne = :b AND c.userTwo = :a)")
    Optional<Conversation> findBetweenUsers(User a, User b);
}
