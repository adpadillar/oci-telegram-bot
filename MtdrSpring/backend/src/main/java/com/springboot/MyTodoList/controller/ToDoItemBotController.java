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
import com.springboot.MyTodoList.dto.TaskDTO;
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

	public ToDoItemBotController(String botToken, String botName, MessageService messageService, UserService userService, TaskService taskService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.messageService = messageService;
		this.botName = botName;
		this.userService = userService;
		this.taskService = taskService;
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			UserModel user = runAuthMiddleware(update);
			String messageText = update.getMessage().getText();
        	long chatId = update.getMessage().getChatId();

			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
		
			

			if (user == null) return;
			
			// Check if the user is in the state of waiting for task description
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			if ("waiting_for_task_description".equals(lastMessage.getMessageType())) {
				System.out.println("Waiting for task description");
				handleTaskDescription(chatId, user, messageText);
				return;
			}

				
			if ("waiting_for_task_details_id".equals(lastMessage.getMessageType())) {
				List<MessageModel> messages = messageService.findMessagesFromChat(chatId);
				String taskIdStr = messages.get(0).getContent();
				showTaskDetails(chatId, taskIdStr);
				return;
			}

			
			if (user.getRole().equals("developer")) {
				// we will handle this here
				logger.info("This is a message from a developer");

				if (messageText.equals("↩️ Back to Main Menu")) {
					showDeveloperMainMenu(chatId);
					return;
				}
				
				// Handle button actions
				if (messageText.equals(BotLabels.VIEW_TASKS.getLabel())) {
					handleViewTasks(chatId);
					return;
				} else if (messageText.equals(BotLabels.ADD_TASK.getLabel())) {
					handleAddTask(chatId);
					return;
				} else if (messageText.equals(BotLabels.FILTER_TASKS.getLabel())) {
					handleFilterTasks(chatId);
					return;
				} else if (messageText.equals(BotLabels.UPDATE_TASK.getLabel())) {
					handleUpdateTask(chatId);
					return;
				} else if (messageText.equals(BotLabels.DETAILS.getLabel())) {
					handleTaskDetails(chatId);
					return;
					}
					// Check for filter options
					if (messageText.equals("⏰ My Tasks")) {
						handleMyTasks(chatId, user);
						return;
					} else if (messageText.equals("⭕ Created Tasks")) {
						handleTasksByStatus(chatId, "created");
						return;
					} else if (messageText.equals("📊 In progress Tasks")) {
						handleTasksByStatus(chatId, "in_progress");
						return;
					} else if (messageText.equals("✅ Done Tasks")) {
						handleTasksByStatus(chatId, "done");
						return;
					}
				// Show the main menu by default
				showDeveloperMainMenu(chatId);
				return;
			}

			if (user.getRole().equals("manager")) {
				// we will handle this here
				logger.info("This is a message from a manager");
				if (messageText.equals("↩️ Back to Main Menu")) {
					showManagerMainMenu(chatId);
					return;
				}
				
				// Handle button actions
				if (messageText.equals(BotLabels.VIEW_TASKS.getLabel())) {
					handleViewTasks(chatId);
					return;
				} else if (messageText.equals(BotLabels.ADD_TASK.getLabel())) {
					handleAddTask(chatId);
					return;
				} else if (messageText.equals(BotLabels.FILTER_TASKS.getLabel())) {
					handleFilterTasks(chatId);
					return;
				} else if (messageText.equals(BotLabels.UPDATE_TASK.getLabel())) {
					handleUpdateTask(chatId);
					return;
				} else if (messageText.equals(BotLabels.DETAILS.getLabel())) {
					handleTaskDetails(chatId);
					return;
				} else if (messageText.equals(BotLabels.CREATE_SPRINT.getLabel())) {
					handleCreateSprint(chatId);
					return;
				} else if (messageText.equals(BotLabels.UPDATE_SPRINT.getLabel())) {
					handleUpdateSprint(chatId);
					return;
				}

				showManagerMainMenu(chatId);
				return;
			}

			
	

			// if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
			// 		|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {

			// 	SendMessage messageToTelegram = new SendMessage();
			// 	messageToTelegram.setChatId(chatId);
			// 	messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());


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
					String lastName = messages.get(2).getContent();
					String firstName = messages.get(4).getContent();

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

	private void handleViewTasks(long chatId) {
		try {

			ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboardRows = new ArrayList<>();
			KeyboardRow row = new KeyboardRow();
			row.add("↩️ Back to Main Menu");
			keyboardRows.add(row);
			keyboard.setKeyboard(keyboardRows);
			keyboard.setResizeKeyboard(true);
			keyboard.setOneTimeKeyboard(false);

			// Get tasks from service
			List<TaskModel> tasks = taskService.findAll(); 
			StringBuilder message = new StringBuilder("📋 *All Tasks*\n\n");
			
			if (tasks == null || tasks.isEmpty()) {
				message = new StringBuilder("No tasks found. Use the Add Task option to create new tasks.");
			} else {
				for (TaskModel task : tasks) {
					message.append("🔹 *Task ID:* `").append(task.getID()).append("`\n")
						.append("📝 *Description:* ").append(task.getDescription()).append("\n")
						.append("📊 *Status:* ").append(task.getStatus()).append("\n")
						.append("👤 *Assigned to:* ").append(task.getAssignedTo() != null ? 
							task.getCreatedBy().getFirstName() : "Unknown").append("\n\n");
				}
			}
	
			SendMessage response = new SendMessage();
			response.setChatId(chatId);
			response.setText(message.toString());
			response.enableMarkdown(true);
			response.setReplyMarkup(keyboard); 
			
			execute(response);
		} catch (NullPointerException e) {
			logger.error("TaskService not properly initialized or null reference", e);
			
		} catch (Exception e) {
			logger.error("Error viewing tasks: " + e.getMessage(), e);
			
		}
			
	}
	
	private void handleAddTask(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Please enter the task description:");
			
			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_task_description");
			stateMessage.setRole("assistant");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);
			
			execute(message);			
		} catch (Exception e) {
			logger.error("Error initiating add task", e);
		}
	}
	
	private void handleTaskDescription(long chatId, UserModel user, String taskDescription) {
		System.out.println("Task description: " + taskDescription);
		try {
			int projectId = user.getProject().getID();
			TaskDTO taskDTO = new TaskDTO();
			taskDTO.setStatus("created");
			taskDTO.setCreatedBy(user.getID());
			taskDTO.setAssignedTo(user.getID());
			taskDTO.setDescription(taskDescription);
			
			// Add task to the project
			TaskModel newTask = taskService.addTodoItemToProject(projectId, taskDTO);
			logger.info("Task added to project: " + newTask);
			
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Task added successfully!");
			execute(message);
			
		} catch (Exception e) {
			logger.error("Error adding task", e);
		}
	}
	
	private void handleFilterTasks(long chatId) {
		try {

			// Get user to check role
			UserModel user = userService.findUserByChatId(chatId);
			ReplyKeyboardMarkup filterKeyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboard = new ArrayList<>();
			
			// Only add "My Tasks" option for developers
			if ("developer".equals(user.getRole())) {
				KeyboardRow row1 = new KeyboardRow();
				row1.add("⏰ My Tasks");
				keyboard.add(row1);
			}
			
			// Common filter options for both roles
			KeyboardRow row2 = new KeyboardRow();
			row2.add("⭕ Created Tasks");
			row2.add("📊 In progress Tasks");
			row2.add("✅ Done Tasks");
			keyboard.add(row2);
			
			KeyboardRow row3 = new KeyboardRow();
			row3.add("↩️ Back to Main Menu");
			keyboard.add(row3);
			
			filterKeyboard.setKeyboard(keyboard);
			filterKeyboard.setResizeKeyboard(true);
			
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText(user.getRole().equals("manager") ? 
				"Select a status filter:" : 
				"Select a filter option:");
			message.setReplyMarkup(filterKeyboard);
			
			execute(message);
		} catch (Exception e) {
			logger.error("Error showing filters", e);
			
		}
	}
	
	private void handleUpdateTask(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Please enter the task ID you want to update:");
			
			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_task_id");
			stateMessage.setRole("assistant");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);
			
			execute(message);
		} catch (Exception e) {
			logger.error("Error initiating task update", e);
		}
	}
	
	private void handleTaskDetails(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Please enter the task ID to view details:");
			
			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_task_details_id");
			stateMessage.setRole("assistant");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);
			execute(message);


		} catch (Exception e) {
			logger.error("Error getting task details", e);
			
		}
		
	}

	private void showTaskDetails(long chatId, String taskIdStr) {
		try {
			int taskId = Integer.parseInt(taskIdStr);
			Optional<TaskModel> taskOpt = taskService.getItemById(taskId);
			
			if (taskOpt.isPresent()) {
				TaskModel task = taskOpt.get();
				StringBuilder detailsMsg = new StringBuilder();
				detailsMsg.append("📋 *Task Details*\n\n")
						.append("🔹 *ID:* `").append(task.getID()).append("`\n")
						.append("📝 *Description:* ").append(task.getDescription()).append("\n")
						.append("📊 *Status:* ").append(task.getStatus()).append("\n")
						.append("⏱ *Estimated Hours:* ").append(task.getEstimateHours() != null ? task.getEstimateHours() : "Not set").append("\n")
						.append("⌛ *Real Hours:* ").append(task.getRealHours() != null ? task.getRealHours() : "Not set").append("\n")
						.append("🏷 *Category:* ").append(task.getCategory() != null ? task.getCategory() : "Not set").append("\n")
						.append("👤 *Created By:* ").append(task.getCreatedBy() != null ? task.getCreatedBy().getFirstName() : "Unknown").append("\n")
						.append("👥 *Assigned To:* ").append(task.getAssignedTo() != null ? task.getAssignedTo().getFirstName() : "Unassigned").append("\n")
						.append("📅 *Created At:* ").append(task.getCreatedAt()).append("\n")
						.append("🏢 *Project:* ").append(task.getProject() != null ? task.getProject().getName() : "No project");

				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				KeyboardRow row = new KeyboardRow();
				row.add("↩️ Back to Main Menu");
				keyboardRows.add(row);
				keyboard.setKeyboard(keyboardRows);
				keyboard.setResizeKeyboard(true);

				SendMessage response = new SendMessage();
				response.setChatId(chatId);
				response.setText(detailsMsg.toString());
				response.enableMarkdown(true);
				response.setReplyMarkup(keyboard);
				
				execute(response);
			} else {
				SendMessage errorMsg = new SendMessage();
				errorMsg.setChatId(chatId);
				errorMsg.setText("❌ Task not found with ID: " + taskId);
				execute(errorMsg);
				showDeveloperMainMenu(chatId);
			}
		} catch (NumberFormatException e) {
			SendMessage errorMsg = new SendMessage();
			errorMsg.setChatId(chatId);
			errorMsg.setText("❌ Please enter a valid task ID (number)");
			try {
				execute(errorMsg);
				handleTaskDetails(chatId); // Ask again for the ID
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		} catch (Exception e) {
			logger.error("Error showing task details", e);
			
		}
	}

	private void handleMyTasks(long chatId, UserModel user) {
		try {
			List<TaskModel> tasks = taskService.findByUserAssigned(user);
			sendTaskList(chatId, tasks, "📋 *My Assigned Tasks*\n\n");
		} catch (Exception e) {
			logger.error("Error getting user tasks", e);
			
		}
	}

	/**
	 * @param chatId
	 * @param status
	 */
	private void handleTasksByStatus(long chatId, String status) {
		/* try {
			List<TaskModel> tasks = taskService.findByStatus(status);
			String title = switch (status) {
				case "created" -> "⭕ *Created Tasks*\n\n";
				case "in_progress" -> "📊 *In Progress Tasks*\n\n";
				case "done" -> "✅ *Done Tasks*\n\n";
				default -> "📋 *Tasks*\n\n";
			};
			sendTaskList(chatId, tasks, title);
		} catch (Exception e) {
			logger.error("Error getting " + status + " tasks", e);
			
		} */
	}

	private void sendTaskList(long chatId, List<TaskModel> tasks, String title) {
		try {
			StringBuilder message = new StringBuilder(title);
			
			if (tasks.isEmpty()) {
				message.append("No tasks found in this category.");
			} else {
				for (TaskModel task : tasks) {
					message.append("🔹 *Task ID:* `").append(task.getID()).append("`\n")
						.append("📝 *Description:* ").append(task.getDescription()).append("\n")
						.append("📊 *Status:* ").append(task.getStatus()).append("\n")
						.append("👤 *Assigned to:* ").append(task.getAssignedTo() != null ? 
							task.getAssignedTo().getFirstName() : "Unassigned").append("\n\n");
				}
			}

			// Add back button
			ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboardRows = new ArrayList<>();
			KeyboardRow row = new KeyboardRow();
			row.add("↩️ Back to Main Menu");
			keyboardRows.add(row);
			keyboard.setKeyboard(keyboardRows);
			keyboard.setResizeKeyboard(true);
			
			SendMessage response = new SendMessage();
			response.setChatId(chatId);
			response.setText(message.toString());
			response.enableMarkdown(true);
			response.setReplyMarkup(keyboard);
			
			execute(response);
		} catch (Exception e) {
			logger.error("Error sending task list", e);
			
		}
	}

	private void handleCreateSprint(long chatId){

	}
					
	private void handleUpdateSprint(long chatId){
		
	}

	private void showDeveloperMainMenu(long chatId) {
		try {
			// Create developer keyboard
			ReplyKeyboardMarkup devKeyboardMarkup = new ReplyKeyboardMarkup();
			List<KeyboardRow> devKeyboard = new ArrayList<>();
			
			// First row - View tasks
			KeyboardRow firstRow = new KeyboardRow();
			firstRow.add(BotLabels.VIEW_TASKS.getLabel());
			devKeyboard.add(firstRow);
			
			// Second row - Filter and Add
			KeyboardRow secondRow = new KeyboardRow();
			secondRow.add(BotLabels.FILTER_TASKS.getLabel());
			secondRow.add(BotLabels.ADD_TASK.getLabel());
			devKeyboard.add(secondRow);

			// Third row - Details and Update
			KeyboardRow thirdRow = new KeyboardRow();
			thirdRow.add(BotLabels.DETAILS.getLabel());
			thirdRow.add(BotLabels.UPDATE_TASK.getLabel());
			devKeyboard.add(thirdRow);
			
			
			
			// Configure keyboard
			devKeyboardMarkup.setKeyboard(devKeyboard);
			devKeyboardMarkup.setResizeKeyboard(true);
			devKeyboardMarkup.setOneTimeKeyboard(false);
			
			// Create and send message with keyboard
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Developer Menu - Please select an option:");
			message.setReplyMarkup(devKeyboardMarkup);
			
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error showing developer menu", e);
		}
	}

	private void showManagerMainMenu(long chatId) {
		try {
			// Create developer keyboard
			ReplyKeyboardMarkup devKeyboardMarkup = new ReplyKeyboardMarkup();
			List<KeyboardRow> manKeyboard = new ArrayList<>();
			
			// First row - View tasks
			KeyboardRow firstRow = new KeyboardRow();
			firstRow.add(BotLabels.VIEW_TASKS.getLabel());
			manKeyboard.add(firstRow);
			
			// Second row - Filter and Add
			KeyboardRow secondRow = new KeyboardRow();
			secondRow.add(BotLabels.FILTER_TASKS.getLabel());
			secondRow.add(BotLabels.ADD_TASK.getLabel());
			manKeyboard.add(secondRow);

			// Third row - Details and Update
			KeyboardRow thirdRow = new KeyboardRow();
			thirdRow.add(BotLabels.DETAILS.getLabel());
			thirdRow.add(BotLabels.UPDATE_TASK.getLabel());
			manKeyboard.add(thirdRow);
			
			// Fourth row - Sprints
			KeyboardRow fourthRow = new KeyboardRow();
			fourthRow.add(BotLabels.CREATE_SPRINT.getLabel());
			fourthRow.add(BotLabels.UPDATE_SPRINT.getLabel());
			manKeyboard.add(fourthRow);

			// Configure keyboard
			devKeyboardMarkup.setKeyboard(manKeyboard);
			devKeyboardMarkup.setResizeKeyboard(true);
			devKeyboardMarkup.setOneTimeKeyboard(false);
			
			// Create and send message with keyboard
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Manager Menu - Please select an option:");
			message.setReplyMarkup(devKeyboardMarkup);
			
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error showing Manager menu", e);
		}
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