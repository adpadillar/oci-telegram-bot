package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.MessageModel;
import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.repository.MessageRepository;
import com.springboot.MyTodoList.service.MessageService;
import com.springboot.MyTodoList.service.ProjectService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private String botName;
	private MessageService messageService;
	private UserService userService;
	private TaskService taskService;

	public ToDoItemBotController(String botToken, String botName, MessageService messageService, UserService userService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.messageService = messageService;
		this.botName = botName;
		this.userService = userService;
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			UserModel user = runAuthMiddleware(update);
			if (user == null) return;

			if (user.getRole().equals("developer")) {
				// we will handle this here
				logger.info("This is a message from a developer");
				return;
			}

			if (user.getRole().equals("manager")) {
				// we will handle this here
				logger.info("This is a message from a manager");
				return;
			}

			String messageText = update.getMessage().getText();
        	long chatId = update.getMessage().getChatId();

			// Handle VIEW_TASKS button
			if (messageText.equals(BotLabels.VIEW_TASKS.getLabel())) {
				try {
					List<TaskModel> allTasks = taskService.findAll();
					
					if (allTasks.isEmpty()) {
						SendMessage message = new SendMessage();
						message.setChatId(chatId);
						message.setText("No tasks found in the system.");
						execute(message);
						return;
					}
					StringBuilder taskList = new StringBuilder("📋 *All Tasks*\n\n");
                
					for (TaskModel task : allTasks) {
						taskList.append("🔹 *Task ID:* ").append(task.getID())
							.append("\n📝 *Description:* ").append(task.getDescription())
							.append("\n📊 *Status:* ").append(task.getStatus())
							.append("\n📅 *Created:* ").append(task.getCreatedAt().toLocalDateTime().toString())
							.append("\n\n");
					}

					SendMessage message = new SendMessage();
					message.setChatId(chatId);
					message.setText(taskList.toString());
					message.enableMarkdown(true); // Enable markdown formatting
                
					// Add a keyboard with back button
					ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
					List<KeyboardRow> keyboard = new ArrayList<>();
					KeyboardRow row = new KeyboardRow();
					row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
					keyboard.add(row);
					keyboardMarkup.setKeyboard(keyboard);
					message.setReplyMarkup(keyboardMarkup);
	
					execute(message);
					
				} catch (Exception e) {
					logger.error("Error while fetching tasks", e);
					try {
						SendMessage errorMessage = new SendMessage();
						errorMessage.setChatId(chatId);
						errorMessage.setText("Sorry, there was an error while fetching the tasks. Please try again later.");
						execute(errorMessage);
					} catch (TelegramApiException ex) {
						logger.error("Error sending error message", ex);
					}
				}
				return;
			}
	

			// if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
			// 		|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {

			// 	SendMessage messageToTelegram = new SendMessage();
			// 	messageToTelegram.setChatId(chatId);
			// 	messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

			 	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
			 	List<KeyboardRow> keyboard = new ArrayList<>();

			 	// first row
			 	KeyboardRow row = new KeyboardRow();
			 	row.add(BotLabels.VIEW_TASKS.getLabel());
			 	// Add the first row to the keyboard
			 	keyboard.add(row);

			 	// second row
			 	row = new KeyboardRow();
			 	row.add(BotLabels.DETAILS.getLabel());
			 	row.add(BotLabels.ADD_TASK.getLabel());
			 	keyboard.add(row);

				row = new KeyboardRow();
			 	row.add(BotLabels.FILTER_TASKS.getLabel());
			 	row.add(BotLabels.UPDATE_TASK.getLabel());
			 	keyboard.add(row);

			// 	// Set the keyboard
			// 	keyboardMarkup.setKeyboard(keyboard);

			// 	// Add the keyboard markup
			// 	messageToTelegram.setReplyMarkup(keyboardMarkup);

			// 	try {
			// 		execute(messageToTelegram);
			// 	} catch (TelegramApiException e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}

			// } else if (messageTextFromTelegram.indexOf(BotLabels.DONE.getLabel()) != -1) {

			// 	String done = messageTextFromTelegram.substring(0,
			// 			messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
			// 	Integer id = Integer.valueOf(done);

			// 	try {

			// 		TaskModel item = getToDoItemById(id);
			// 		item.setStatus("done");
			// 		updateToDoItem(item, id);
			// 		BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);

			// 	} catch (Exception e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}

			// } else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {

			// 	String undo = messageTextFromTelegram.substring(0,
			// 			messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
			// 	Integer id = Integer.valueOf(undo);

			// 	try {

			// 		TaskModel item = getToDoItemById(id);
			// 		item.setStatus("created");
			// 		updateToDoItem(item, id);
			// 		BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);

			// 	} catch (Exception e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}

			// } else if (messageTextFromTelegram.indexOf(BotLabels.DELETE.getLabel()) != -1) {

			// 	String delete = messageTextFromTelegram.substring(0,
			// 			messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
			// 	Integer id = Integer.valueOf(delete);

			// 	try {

			// 		deleteToDoItem(id).getBody();
			// 		BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DELETED.getMessage(), this);

			// 	} catch (Exception e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}

			// } else if (messageTextFromTelegram.equals(BotCommands.HIDE_COMMAND.getCommand())
			// 		|| messageTextFromTelegram.equals(BotLabels.HIDE_MAIN_SCREEN.getLabel())) {

			// 	BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), this);

			// } else if (messageTextFromTelegram.equals(BotCommands.TODO_LIST.getCommand())
			// 		|| messageTextFromTelegram.equals(BotLabels.LIST_ALL_ITEMS.getLabel())
			// 		|| messageTextFromTelegram.equals(BotLabels.MY_TODO_LIST.getLabel())) {

			// 	List<TaskModel> allItems = getAllToDoItems();
			// 	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
			// 	List<KeyboardRow> keyboard = new ArrayList<>();

			// 	// command back to main screen
			// 	KeyboardRow mainScreenRowTop = new KeyboardRow();
			// 	mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
			// 	keyboard.add(mainScreenRowTop);

			// 	KeyboardRow firstRow = new KeyboardRow();
			// 	firstRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
			// 	keyboard.add(firstRow);

			// 	KeyboardRow myTodoListTitleRow = new KeyboardRow();
			// 	myTodoListTitleRow.add(BotLabels.MY_TODO_LIST.getLabel());
			// 	keyboard.add(myTodoListTitleRow);

			// 	List<TaskModel> activeItems = allItems.stream().filter(item -> item.getStatus().equals("created"))
			// 			.collect(Collectors.toList());

			// 	for (TaskModel item : activeItems) {

			// 		KeyboardRow currentRow = new KeyboardRow();
			// 		currentRow.add(item.getDescription());
			// 		currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
			// 		keyboard.add(currentRow);
			// 	}

			// 	List<TaskModel> doneItems = allItems.stream().filter(item -> item.getStatus().equals("done"))
			// 			.collect(Collectors.toList());

			// 	for (TaskModel item : doneItems) {
			// 		KeyboardRow currentRow = new KeyboardRow();
			// 		currentRow.add(item.getDescription());
			// 		currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
			// 		currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
			// 		keyboard.add(currentRow);
			// 	}

			// 	// command back to main screen
			// 	KeyboardRow mainScreenRowBottom = new KeyboardRow();
			// 	mainScreenRowBottom.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
			// 	keyboard.add(mainScreenRowBottom);

			// 	keyboardMarkup.setKeyboard(keyboard);

			// 	SendMessage messageToTelegram = new SendMessage();
			// 	messageToTelegram.setChatId(chatId);
			// 	messageToTelegram.setText(BotLabels.MY_TODO_LIST.getLabel());
			// 	messageToTelegram.setReplyMarkup(keyboardMarkup);

			// 	try {
			// 		execute(messageToTelegram);
			// 	} catch (TelegramApiException e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}

			// } else if (messageTextFromTelegram.equals(BotCommands.ADD_ITEM.getCommand())
			// 		|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_ITEM.getLabel())) {
			// 	try {
			// 		SendMessage messageToTelegram = new SendMessage();
			// 		messageToTelegram.setChatId(chatId);
			// 		messageToTelegram.setText(BotMessages.TYPE_NEW_TODO_ITEM.getMessage());
			// 		// hide keyboard
			// 		ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove(true);
			// 		messageToTelegram.setReplyMarkup(keyboardMarkup);

			// 		// send message
			// 		execute(messageToTelegram);

			// 	} catch (Exception e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}

			// }

			// else {
		// 		try {
		// 			TaskModel newItem = new TaskModel();
		// 			newItem.setDescription(messageTextFromTelegram);
		// 			newItem.setCreatedAt(OffsetDateTime.now());
		// 			newItem.setStatus("created");
		// 			ResponseEntity<TaskModel> entity = addToDoItem(newItem);

		// 			SendMessage messageToTelegram = new SendMessage();
		// 			messageToTelegram.setChatId(chatId);
		// 			messageToTelegram.setText(BotMessages.NEW_ITEM_ADDED.getMessage());

		// 			execute(messageToTelegram);
		// 		} catch (Exception e) {
		// 			logger.error(e.getLocalizedMessage(), e);
		// 		}
		// 	}
		}
	}

	@Override
	public String getBotUsername() {		
		return botName;
	}

	public UserModel runAuthMiddleware(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			Message message = update.getMessage();
			Long chatId = message.getChatId();
			UserModel user = userService.findUserByChatId(chatId);

			logger.info("Message from user: " + user);

			MessageModel newMessage = new MessageModel();
			newMessage.setMessageType("text");
			newMessage.setRole("user");
			newMessage.setContent(message.getText());
			newMessage.setUserId(chatId);
			newMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(newMessage);

			logger.info("Saved new message from chat id: " + chatId);

			if (user == null) {
				// this means this is a new user to the system. We want to ask him for his
				// fist name, last name, and project id
				List<MessageModel> messages = messageService.findMessagesFromChat(chatId);

				logger.info("Messages from chat id: " + chatId + " are: " + messages);

				boolean isThereAssistantMessages = false;
				for (MessageModel m : messages) {
					if (m.getRole().equals("assistant")) {
						isThereAssistantMessages = true;
						break;
					}
				}

				if (!isThereAssistantMessages) {
					// we need to send a message to the user asking for his first name
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Hello! Welcome to Oracle TaskBot. Since this is a new account, I need to ask you a few questions. Firstly, what is your first name?");

					try {
						MessageModel assistantMessage = new MessageModel();
						assistantMessage.setMessageType("input-first-name");
						assistantMessage.setRole("assistant");
						assistantMessage.setContent("Hello! Welcome to Oracle TaskBot. Since this is a new account, I need to ask you a few questions. Firstly, what is your first name?");
						assistantMessage.setUserId(chatId);
						assistantMessage.setCreatedAt(OffsetDateTime.now());
						messageService.saveMessage(assistantMessage);

						execute(messageToTelegram);
						// we need to save our message to the database
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}

					return null;
				}

				// we are now looking for the first assistant message
				MessageModel firstAssistantMessage = null;
				for (MessageModel m : messages) {
					if (m.getRole().equals("assistant")) {
						firstAssistantMessage = m;
						break;
					}
				}

				// we check the type of this message
				if (firstAssistantMessage.getMessageType().equals("input-first-name")) {
					// we ask for the last name. We still dont save anything. We will save once we
					// get all the required information
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Great! Now, what is your last name?");
					try {
						MessageModel assistantMessage = new MessageModel();
						assistantMessage.setMessageType("input-last-name");
						assistantMessage.setRole("assistant");
						assistantMessage.setContent("Great! Now, what is your last name?");
						assistantMessage.setUserId(chatId);
						assistantMessage.setCreatedAt(OffsetDateTime.now());
						messageService.saveMessage(assistantMessage);

						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}

					return null;
				}

				if (firstAssistantMessage.getMessageType().equals("input-last-name")) {
					// we ask for the project id. We still dont save anything. We will save once we
					// get all the required information
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Amazing! Lastly, could you please copy/paste your project ID here? (Your manager should have provided you with this ID)");
					try {
						MessageModel assistantMessage = new MessageModel();
						assistantMessage.setMessageType("input-project-id");
						assistantMessage.setRole("assistant");
						assistantMessage.setContent("Amazing! Lastly, could you please copy/paste your project ID here? (Your manager should have provided you with this ID)");
						assistantMessage.setUserId(chatId);
						assistantMessage.setCreatedAt(OffsetDateTime.now());
						messageService.saveMessage(assistantMessage);

						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}

					return null;
				}

				if (firstAssistantMessage.getMessageType().equals("input-project-id")) {
					// we save the user to the database
					String projectId = messages.get(0).getContent();
					String firstName = messages.get(2).getContent();
					String lastName = messages.get(4).getContent();

					UserDTO newUser = new UserDTO();
					newUser.setTelegramId(chatId);
					newUser.setFirstName(firstName);
					newUser.setLastName(lastName);
					newUser.setRole("user-pending-activation");

					userService.createUser(newUser, Integer.parseInt(projectId));

					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Thank you! Your account has been created. Please wait until your account is activated by your manager. You will receive a message once your account is activated.");
					try {
						MessageModel assistantMessage = new MessageModel();
						assistantMessage.setMessageType("account-created");
						assistantMessage.setRole("assistant");
						assistantMessage.setContent("Thank you! Your account has been created. Please wait until your account is activated by your manager. You will receive a message once your account is activated.");
						assistantMessage.setUserId(chatId);
						assistantMessage.setCreatedAt(OffsetDateTime.now());
						messageService.saveMessage(assistantMessage);

						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}

					return null;
				}

				logger.info(messages.toString());
				return null;
			}

			// now we know the user is defined
			// we need to check if the user is activated
			if (user.getRole().equals("user-pending-activation")) {
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Your account is still pending activation. Please wait until your account is activated by your manager. You will receive a message once your account is activated.");

				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}

				// we dont need to save this message to the database
				return null;
			}

			// here we already know the user is activated
			return user;
		}

		return null;
	}

	// // GET /todolist
	// public List<TaskModel> getAllToDoItems() { 
	// 	return toDoItemService.findAll();
	// }

	// // GET BY ID /todolist/{id}
	// public TaskModel getToDoItemById(@PathVariable int id) throws RuntimeException {
	// 		Optional<TaskModel> task = toDoItemService.getItemById(id);
	// 		if (task.isPresent()) {
	// 			return task.get();
	// 		} else {
	// 			throw new RuntimeException("Task not found");
	// 		}
	// }

	// // PUT /todolist
	// public ResponseEntity addToDoItem(@RequestBody TaskModel todoItem) throws Exception {
	// 	TaskModel td = toDoItemService.addToDoItem(todoItem);
	// 	HttpHeaders responseHeaders = new HttpHeaders();
	// 	responseHeaders.set("location", "" + td.getID());
	// 	responseHeaders.set("Access-Control-Expose-Headers", "location");
	// 	// URI location = URI.create(""+td.getID())

	// 	return ResponseEntity.ok().headers(responseHeaders).build();
	// }

	// // UPDATE /todolist/{id}TaskModel
	// public ResponseEntity updateToDoItem(@RequestBody TaskModel toDoItem, @PathVariable int id) {
	// 	try {
	// 		TaskModel toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
	// 		System.out.println(toDoItem1.toString());
	// 		return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
	// 	} catch (Exception e) {
	// 		logger.error(e.getLocalizedMessage(), e);
	// 		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	// 	}
	// }

	// // DELETE todolist/{id}
	// public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id) {
	// 	Boolean flag = false;
	// 	try {
	// 		flag = toDoItemService.deleteToDoItem(id);
	// 		return new ResponseEntity<>(flag, HttpStatus.OK);
	// 	} catch (Exception e) {
	// 		logger.error(e.getLocalizedMessage(), e);
	// 		return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
	// 	}
	// }

}