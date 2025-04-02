package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
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
			if ("waiting_for_update_selection".equals(lastMessage.getMessageType())) {
				System.out.println("Waiting for update selection");
				handleUpdateTaskSelection(chatId, messageText);
				return;
			}
			
			if ("waiting_for_real_hours_spent".equals(lastMessage.getMessageType())) {
				System.out.println("Waiting for task description");
				handleUpdateRealHours(chatId, messageText);
				return;
			}

			if ("waiting_for_task_description".equals(lastMessage.getMessageType())) {
				System.out.println("Waiting for task description");
				handleTaskDescription(chatId, user, messageText);
				return;
			}

			if ("waiting_for_task_delete_id".equals(lastMessage.getMessageType())) {
				handleTaskDeletion(chatId, messageText);
				return;
			}

			if ("waiting_for_task_id".equals(lastMessage.getMessageType())) {
				handleTaskUpdate(chatId, messageText);
				return;
			}
	
			if ("waiting_for_new_description".equals(lastMessage.getMessageType())) {
				handleNewDescription(chatId, messageText);
				return;
			}
	
			if ("waiting_for_new_status".equals(lastMessage.getMessageType())) {
				handleNewStatus(chatId, messageText);
				return;
				}

			if ("waiting_for_task_estimate_hours".equals(lastMessage.getMessageType())) {
				handleTaskEstimateHours(chatId, user, messageText);
				return;
			}

			if ("waiting_for_task_sprint".equals(lastMessage.getMessageType())) {
				handleTaskSprint(chatId, user, messageText);
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
				} else if (messageText.equals(BotLabels.DELETE_TASK.getLabel())) {
					handleDeleteTask(chatId);
					return;
				} else if (messageText.equals(BotLabels.DETAILS.getLabel())) {
					try {
						handleTaskDetails(chatId);
					} catch (TelegramApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
					try {
						handleTaskDetails(chatId);
					} catch (TelegramApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
			// Sort tasks by ID
			tasks.sort(Comparator.comparingInt(TaskModel::getID));
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
			// Enviar mensaje para solicitar la descripción de la tarea
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("Por favor, ingresa una descripción para la nueva tarea:");

			// Guardar el estado del usuario como "waiting_for_task_description"
			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_task_description");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent("Por favor, ingresa una descripción para la nueva tarea:");
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);

			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar la descripción de la tarea: " + e.getMessage());
		}
	}
	
	private void handleTaskDescription(long chatId, UserModel user, String taskDescription) {
		try {
			// Guardar la descripción temporalmente y solicitar las horas estimadas
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("Gracias. Ahora, ¿cuántas horas estimas que tomará completar esta tarea?");

			// Guardar el estado del usuario como "waiting_for_task_estimate_hours"
			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_task_estimate_hours");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent(taskDescription); // Guardar la descripción temporalmente
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);

			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar las horas estimadas: " + e.getMessage());
		}
	}

	private void handleTaskEstimateHours(long chatId, UserModel user, String estimateHoursText) {
		try {
			// Validate that the estimated hours are a number
			double estimateHours;
			try {
				estimateHours = Double.parseDouble(estimateHoursText);
			} catch (NumberFormatException e) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("Por favor, ingresa un número válido para las horas estimadas.");
				execute(errorMessage);
				return;
			}
	
			// Retrieve the task description saved previously
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String taskDescription = lastMessage.getContent();
	
			// Save the estimated hours and ask for the sprint number
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("Gracias. Ahora, ¿en qué número de sprint deseas agregar esta tarea?");
	
			// Save the state of the user as "waiting_for_task_sprint"
			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_task_sprint");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent(taskDescription + "|" + estimateHours); // Save description and hours temporarily
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);
	
			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar el número del sprint: " + e.getMessage());
		}
	}

	private void handleTaskSprint(long chatId, UserModel user, String sprintNumberText) {
		try {
			// Validate that the sprint number is an integer
			int sprintNumber;
			try {
				sprintNumber = Integer.parseInt(sprintNumberText);
			} catch (NumberFormatException e) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("Por favor, ingresa un número válido para el sprint.");
				execute(errorMessage);
				return;
			}
	
			// Retrieve the task description and estimated hours saved previously
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String[] taskData = lastMessage.getContent().split("\\|");
			String taskDescription = taskData[0];
			double estimateHours = Double.parseDouble(taskData[1]);
	
			// Create the new task with the sprint number
			TaskDTO newTask = new TaskDTO();
			newTask.setDescription(taskDescription);
			newTask.setEstimateHours(estimateHours);
			newTask.setSprint(sprintNumber);
			newTask.setStatus("created");
			newTask.setCreatedBy(user.getID());
	
			taskService.addTodoItemToProject(user.getProject().getID(), newTask);
	
			// Confirm to the user that the task was created
			SendMessage confirmationMessage = new SendMessage();
			confirmationMessage.setChatId(chatId);
			confirmationMessage.setText("¡Tarea creada exitosamente con la descripción: \"" + taskDescription + "\", " + estimateHours + " horas estimadas, y asignada al sprint número " + sprintNumber + "!");
			execute(confirmationMessage);
		} catch (TelegramApiException e) {
			logger.error("Error al crear la tarea: " + e.getMessage());
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
	
	//menu command activator
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

	private void handleTaskUpdate(long chatId, String taskIdText) {
		try {
			int taskId = Integer.parseInt(taskIdText);
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {

				// Ask the user to select wht they want to update
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Please select the field you want to update for task (" + taskId + "):");

				// // Create keyboard for update options
				// ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				// List<KeyboardRow> keyboardRows = new ArrayList<>();
				// KeyboardRow row1 = new KeyboardRow();
				// row1.add("Description");
				// row1.add("Status");
				// keyboardRows.add(row1);
				// keyboard.setKeyboard(keyboardRows);
				// keyboard.setResizeKeyboard(true);

				// Create keyboard for update options
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				KeyboardRow row1 = new KeyboardRow();
				row1.add("Edit Description");
				row1.add("Edit Status");
				keyboardRows.add(row1);
				keyboard.setKeyboard(keyboardRows);
				keyboard.setResizeKeyboard(true);
				
				message.setReplyMarkup(keyboard);

				// // Ask for new description
				// SendMessage message = new SendMessage();
				// message.setChatId(chatId);
				// message.setText("Please enter the new task description of task (" + taskId + "):");
				
				// Save state for next message
				MessageModel stateMessage = new MessageModel();
				stateMessage.setMessageType("waiting_for_update_selection");
				stateMessage.setRole("assistant");
				stateMessage.setUserId(chatId);
				stateMessage.setCreatedAt(OffsetDateTime.now());
				messageService.saveMessage(stateMessage);
				
				execute(message);
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Task not found. Please enter a valid task ID.");
				execute(message);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid task ID format", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Invalid task ID format. Please enter a valid task ID.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		} catch (Exception e) {
			logger.error("Error updating task", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("An error occurred while updating the task. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}

	private void handleUpdateTaskSelection(long chatId, String selection) {
		try {
			if (selection.equals("Edit Description")) {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Please enter the new task description:");
				
				// Save state for next message
				MessageModel stateMessage = new MessageModel();
				stateMessage.setMessageType("waiting_for_new_description");
				stateMessage.setRole("assistant");
				stateMessage.setUserId(chatId);
				stateMessage.setCreatedAt(OffsetDateTime.now());
				messageService.saveMessage(stateMessage);
				
				execute(message);
			} else if (selection.equals("Edit Status")) {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Please select the new task status:");
				
				// Create keyboard for status options
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				KeyboardRow row1 = new KeyboardRow();
				row1.add("created");
				row1.add("in progress");
				row1.add("done");
				keyboardRows.add(row1);
				keyboard.setKeyboard(keyboardRows);
				keyboard.setResizeKeyboard(true);
				
				message.setReplyMarkup(keyboard);
				
				// Save state for next message
				MessageModel stateMessage = new MessageModel();
				stateMessage.setMessageType("waiting_for_new_status");
				stateMessage.setRole("assistant");
				stateMessage.setUserId(chatId);
				stateMessage.setCreatedAt(OffsetDateTime.now());
				messageService.saveMessage(stateMessage);
				
				execute(message);
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Invalid selection. Please select a valid field to update.");
				execute(message);
			}
		} catch (Exception e) {
			logger.error("Error updating task selection", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("An error occurred while updating the task. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}
	
	private void handleNewDescription(long chatId, String newDescription) {
		try {
			List<MessageModel> messages = messageService.findMessagesFromChat(chatId);
			int taskId = Integer.parseInt(messages.get(4).getContent());
			
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {
				TaskModel task = taskOptional.get();
				task.setDescription(newDescription);
				taskService.save(task);
				
				// Ask for new status
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Description updated successfully!");
				execute(message);

				// Show the main menu after updating the task
				showDeveloperMainMenu(chatId);

				// Ask for new status
				// SendMessage message = new SendMessage();
				// message.setChatId(chatId);
				// message.setText("Please select the new task status for task (" + task.getID() + "):");
				
				// Create keyboard for status options
				// ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				// List<KeyboardRow> keyboardRows = new ArrayList<>();
				// KeyboardRow row1 = new KeyboardRow();
				// row1.add("created");
				// row1.add("in progress");
				// row1.add("done");
				// keyboardRows.add(row1);
				// keyboard.setKeyboard(keyboardRows);
				// keyboard.setResizeKeyboard(true);
				
				// message.setReplyMarkup(keyboard);
				
				// Save state for next message
				// MessageModel stateMessage = new MessageModel();
				// stateMessage.setMessageType("waiting_for_new_status");
				// stateMessage.setRole("assistant");
				// stateMessage.setUserId(chatId);
				// stateMessage.setCreatedAt(OffsetDateTime.now());
				// messageService.saveMessage(stateMessage);
				
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Task not found. Please enter a valid task ID.");
				execute(message);
			}
		} catch (Exception e) {
			logger.error("Error updating task description", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("An error occurred while updating the task description. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}


	private void handleNewStatus(long chatId, String newStatus) {
		try {
			List<MessageModel> messages = messageService.findMessagesFromChat(chatId);
			int taskId = Integer.parseInt(messages.get(4).getContent());
			
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {
				TaskModel task = taskOptional.get();
				task.setStatus(newStatus);
				// String newDescription = messages.get(2).getContent();
				// task.setDescription(newDescription);
				taskService.save(task);
				
				SendMessage message = new SendMessage();
				message.setChatId(chatId);

				if (newStatus.equals("done")) {
					message.setText("Task status updated successfully! Please enter the real hours spent on the task:");
					MessageModel stateMessage = new MessageModel();
					stateMessage.setMessageType("waiting_for_real_hours_spent");
					stateMessage.setRole("assistant");
					stateMessage.setUserId(chatId);
					stateMessage.setCreatedAt(OffsetDateTime.now());
					messageService.saveMessage(stateMessage);
				
					execute(message);
				} else {
					message.setText("Task status updated successfully!");
					execute(message);
					// Show the main menu after updating the task
					showDeveloperMainMenu(chatId);
				}

				
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Task not found. Please enter a valid task ID.");
				execute(message);
			}
		} catch (Exception e) {
			logger.error("Error updating task status", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("An error occurred while updating the task status. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}

	private void handleUpdateRealHours(long chatId, String realHours) {
		try {
			List<MessageModel> messages = messageService.findMessagesFromChat(chatId);
			int taskId = Integer.parseInt(messages.get(6).getContent());
			
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {
				TaskModel task = taskOptional.get();
				task.setRealHours(Double.parseDouble(realHours));
				taskService.save(task);
				
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Real hours spent updated successfully!");
				execute(message);
				
				// Show the main menu after updating the task
				showDeveloperMainMenu(chatId);
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Task not found. Please enter a valid task ID.");
				execute(message);
			}
		} catch (Exception e) {
			logger.error("Error updating real hours spent", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("An error occurred while updating the real hours spent. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}
	
	private void handleTaskDetails(long chatId) throws TelegramApiException {
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
			// Create developer keyboard
			ReplyKeyboardMarkup devKeyboardMarkup = new ReplyKeyboardMarkup();
			List<KeyboardRow> devKeyboard = new ArrayList<>();
			
			// Third row - Details Update and Delete
			KeyboardRow thirdRow = new KeyboardRow();
			thirdRow.add(BotLabels.DETAILS.getLabel());
			thirdRow.add(BotLabels.UPDATE_TASK.getLabel());
			thirdRow.add(BotLabels.DELETE_TASK.getLabel());
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

	private void handleDeleteTask(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Please enter the task ID you want to delete:");
			
			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_task_delete_id");
			stateMessage.setRole("assistant");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);
			
			execute(message);
		} catch (Exception e) {
			logger.error("Error initiating task delete", e);
		}
	}


	private void handleTaskDeletion(long chatId, String taskIdText) {
		try {
			int taskId = Integer.parseInt(taskIdText);
			boolean isDeleted = taskService.deleteToDoItem(taskId);
	
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			if (isDeleted) {
				message.setText("Task deleted successfully!");
			} else {
				message.setText("Task not found or could not be deleted.");
			}
			execute(message);
		} catch (NumberFormatException e) {
			logger.error("Invalid task ID format", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Invalid task ID format. Please enter a valid task ID.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		} catch (Exception e) {
			logger.error("Error deleting task", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("An error occurred while deleting the task. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
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
			thirdRow.add(BotLabels.DELETE_TASK.getLabel());
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

}