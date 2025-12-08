package com.lp.criticabackend.service;

import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Conversation;
import com.lp.criticabackend.model.Message;
import com.lp.criticabackend.model.MessageType;
import com.lp.criticabackend.model.User;
import com.lp.criticabackend.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessagingService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final FollowerService followerService;
    private final UserDetailsService userDetailsService;

    private static final AppLogger logger = AppLogger.getLogger(MessagingService.class);

    @Autowired
    public MessagingService(ConversationRepository conversationRepo, MessageRepository messageRepo, UserRepository userRepo, FollowerService followService, UserDetailsService userDetailsService) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.followerService = followService;
        this.userDetailsService = userDetailsService;
    }

    private boolean areMutualFollowers(User a, User b){
        List<User> mutualsOfA = followerService.getMutuals(a.getIdusers());
        return mutualsOfA
                .stream()
                .anyMatch(u -> u.getIdusers().equals(b.getIdusers()));
    }

    @Transactional
    public Conversation getOrCreateConversation(User a, User b) {
        if(a.getIdusers().equals(b.getIdusers())){
            String errorMessage = "Cannot create a conversation between the same person";
            logger.error(errorMessage, new IllegalArgumentException(errorMessage));
        }
        Conversation existing = conversationRepo.findBetweenUsers(a, b)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();

                    Conversation conversation = new Conversation();
                    conversation.setUserOne(a);
                    conversation.setUserTwo(b);
                    conversation.setCreatedAt(now);
                    conversation.setLastUpdated(now);
                    return conversation;
                });

        return conversationRepo.save(existing);
    }

    public Message sendMessage(String sender, String receiver, MessageType type, String content, String metadataJson) {
        User a = userDetailsService.getUserByEmail(sender);
        User b = userDetailsService.getUserByEmail(receiver);

        if(!areMutualFollowers(a, b)){
            logger.warn("Users are not mutual followers");
        }

        Conversation conversation = getOrCreateConversation(a, b);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(a);
        if(type == MessageType.TEXT){
            message.setContent(content);
        } else {
            message.setMetadata(metadataJson);
        }
        message.setType(type);
        message.setCreatedAt(LocalDateTime.now());
        message.setSeen(false);
        message.setDeleted(false);

        conversation.setLastUpdated(LocalDateTime.now());
        conversationRepo.save(conversation);

        return messageRepo.save(message);
    }

    public List<Message> fetchMessages(String sender, String receiver, int page, int size) {
        User a = userDetailsService.getUserByEmail(sender);
        User b = userDetailsService.getUserByEmail(receiver);
        Conversation convo = getOrCreateConversation(a, b);

        Pageable pages = PageRequest.of(page, size);
        return messageRepo.findByConversationAndDeletedFalseOrderByCreatedAtDesc(convo, pages);
    }

    @Transactional
    public void markAsRead(Long messageId, User reader) {
        Message m = messageRepo.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        // don't mark if sender is the same as reader
        if(!m.getSender().getIdusers().equals(reader.getIdusers())) {
            m.setSeen(true);
            messageRepo.save(m);
        }
    }

    @Transactional
    public void deleteMessage(Long messageId, User requester) {
        Message m = messageRepo.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        // allow delete only by sender (soft delete)
        if(!m.getSender().getIdusers().equals(requester.getIdusers())) {
            throw new RuntimeException("Only sender can delete message");
        }
        m.setDeleted(true);
        messageRepo.save(m);
    }

    public List<Conversation> listUserConversations(User u){
        return conversationRepo.findByParticipant(u.getIdusers());
    }
}
