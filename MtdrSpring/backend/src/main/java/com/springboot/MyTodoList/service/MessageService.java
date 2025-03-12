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

    public MessageModel findLastAssistantMessageFromChat(long chatId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findLastAssistantMessageFromChat'");
    }
}
