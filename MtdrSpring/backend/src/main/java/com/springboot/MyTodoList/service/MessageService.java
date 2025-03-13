package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.MessageModel;
import com.springboot.MyTodoList.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<MessageModel> findMessagesFromChat(Long chatId) {
        return messageRepository.findByUserIdOrderByCreatedAtDesc(chatId);
    }

    public void saveMessage(MessageModel message) {
        messageRepository.save(message);
    }

    public void deleteMessage(MessageModel message) {
        messageRepository.delete(message);
    }


    public MessageModel findLastMessageByUserId(long chatId) {
        List<MessageModel> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(chatId);
        if (messages != null && !messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }

    public MessageModel findLastAssistantMessageByUserId(long chatId) {
        List<MessageModel> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(chatId);
        if (messages != null && !messages.isEmpty()) {
            return messages.get(1);
        }
        return null;
    }
}