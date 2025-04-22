package com.springboot.MyTodoList.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.springboot.MyTodoList.controller.ToDoItemBotController;
import com.springboot.MyTodoList.service.MessageService;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.repository.SprintRepository;

@Configuration
public class BotConfig {
    
    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${telegram.bot.name}")
    private String botName;

    @Bean
    public ToDoItemBotController todoItemBotController(
            MessageService messageService,
            UserService userService,
            TaskService taskService,
            SprintService sprintService,
            SprintRepository sprintRepository) {
        return new ToDoItemBotController(
            telegramBotToken,
            botName,
            messageService,
            userService,
            taskService,
            sprintService,
            sprintRepository
        );
    }
}