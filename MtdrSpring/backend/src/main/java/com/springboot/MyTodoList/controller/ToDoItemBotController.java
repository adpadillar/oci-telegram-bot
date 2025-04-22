package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.MessageModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.service.MessageService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.dto.TaskDTO;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.dto.SprintDTO;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.repository.SprintRepository;

/**
 * ToDoItemBotController - Main controller class for the Telegram bot
 * Handles all user interactions and commands through Telegram messages
 */
public class ToDoItemBotController extends TelegramLongPollingBot {
	// Logger for tracking application events and errors
	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	
	// Bot configuration and service dependencies
	private String botName;
	private MessageService messageService;
	private UserService userService;
	private TaskService taskService;
	private SprintService sprintService;
	private SprintRepository sprintRepository;

	// Cache to store login codes with timestamps (code -> timestamp)
	private final Map<String, OffsetDateTime> loginCodes = new ConcurrentHashMap<>();

	/**
	 * Constructor for ToDoItemBotController
	 * @param botToken - Telegram bot token for authentication
	 * @param botName - Name of the bot
	 * @param messageService - Service for handling message persistence
	 * @param userService - Service for user management
	 * @param taskService - Service for task operations
	 * @param sprintService - Service for sprint management
	 * @param sprintRepository - Repository for sprint data access
	 */
	public ToDoItemBotController(String botToken, String botName, MessageService messageService, 
							   UserService userService, TaskService taskService, 
							   SprintService sprintService, SprintRepository sprintRepository) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.messageService = messageService;
		this.botName = botName;
		this.userService = userService;
		this.taskService = taskService;
		this.sprintService = sprintService;
		this.sprintRepository = sprintRepository;
		}

	/**
	 * Sends a login code to a user via Telegram
	 * @param chatId - Telegram chat ID of the user
	 * @param code - The login code to send
	 * @return boolean - Whether the code was sent successfully
	 */
	public boolean sendLoginCode(Long chatId, String code) {
		try {
			// Store the code with expiration time (5 minutes from now)
			loginCodes.put(code, OffsetDateTime.now().plusMinutes(5));

			SendMessage loginMessage = new SendMessage();
			loginMessage.setChatId(chatId);
			loginMessage.setText("🔐 *Login Code*\n\n" + code);
			loginMessage.enableMarkdown(true);
			execute(loginMessage);
			return true;
		} catch (TelegramApiException e) {
			logger.error("Error sending login code: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Validates a login code
	 * @param code - The code to validate
	 * @return boolean - Whether the code is valid and not expired
	 */
	public boolean validateLoginCode(String code) {
		OffsetDateTime expiryTime = loginCodes.get(code);
		if (expiryTime == null) {
			return false;
		}

		if (expiryTime.isBefore(OffsetDateTime.now())) {
			loginCodes.remove(code);
			return false;  
		}

		loginCodes.remove(code); // Remove after successful use
		return true;
	}

	/**
	 * Main method that processes incoming Telegram updates
	 * Handles message routing based on user role and message content
	 */
	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			UserModel user = runAuthMiddleware(update);
			String messageText = update.getMessage().getText();
        	long chatId = update.getMessage().getChatId();

			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
		
			

			if (user == null) return;
			
			// Handle pagination events
			if (messageText.equals("⏮️ First 5") || messageText.equals("⏭️ Next 5")) {
				handleTaskPagination(chatId, messageText);
				return;
			}
			
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

			if ("waiting_for_task_details_id".equals(lastMessage.getMessageType())) {
				handleTaskDetailsResponse(chatId, messageText);
				return;
			}

			if ("waiting_for_task_category".equals(lastMessage.getMessageType())) {
				handleTaskCategory(chatId, user, messageText);
				return;
			}

			if ("waiting_for_sprint_name".equals(lastMessage.getMessageType())) {
				handleSetSprintName(chatId, messageText);
				return;
			}

			if ("waiting_for_start_date".equals(lastMessage.getMessageType())) {
				handleSetSprintStartDate(chatId, messageText);
				return;
			}

			if ("waiting_for_end_date".equals(lastMessage.getMessageType())) {
				handleSetSprintEndDate(chatId, messageText);
				return;
			}

			if ("waiting_for_sprint_delete_name".equals(lastMessage.getMessageType())) {
				handleSprintDeletion(chatId, messageText);
				return;
			}

			if ("waiting_for_sprint_update_name".equals(lastMessage.getMessageType())) {
				handleSprintUpdateName(chatId, messageText);
				return;
			}

			if ("waiting_for_sprint_update_selection".equals(lastMessage.getMessageType())) {
				handleSprintUpdateSelection(chatId, messageText);
				return;
			}

			if ("waiting_for_sprint_update_value".equals(lastMessage.getMessageType())) {
				handleSprintUpdateValue(chatId, messageText);
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
						e.printStackTrace();
					}
					return;
				} else if (messageText.equals(BotLabels.HELP.getLabel())) {
					handleHelp(chatId, "developer");
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
				// Handle manager-specific commands
				if (messageText.equals("📋 View Sprints")) {
					handleViewSprints(chatId);
					return;
				}
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
						e.printStackTrace();
					}
					return;
				} else if (messageText.equals(BotLabels.CREATE_SPRINT.getLabel())) {
					handleCreateSprint(chatId);
					return;
				} else if (messageText.equals(BotLabels.UPDATE_SPRINT.getLabel())) {
					handleUpdateSprint(chatId);
					return;
				} else if (messageText.equals(BotLabels.DELETE_SPRINT.getLabel())) {
					handleDeleteSprint(chatId);
					return;
				} else if (messageText.equals(BotLabels.HELP.getLabel())) {
					handleHelp(chatId, "manager");
					return;
				}

				showManagerMainMenu(chatId);
				return;
			}
		}
	}

	/**
	 * Authentication middleware to verify user identity and handle new user registration
	 * @param update - Telegram update containing user message
	 * @return UserModel if authenticated, null otherwise
	 */
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

	/**
	 * Displays all tasks with pagination support
	 * @param chatId - Telegram chat ID of the user
	 */
	private void handleViewTasks(long chatId) {
		try {
			// Get tasks from service
			List<TaskModel> tasks = taskService.findAll();
			// Sort tasks by ID
			tasks.sort(Comparator.comparingInt(TaskModel::getID));
			
			// Create keyboard with pagination options
			ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboardRows = new ArrayList<>();
			
			// Add pagination buttons if there are more than 5 tasks
			if (tasks.size() > 5) {
				KeyboardRow paginationRow = new KeyboardRow();
				paginationRow.add("⏮️ First 5");
				paginationRow.add("⏭️ Next 5");
				keyboardRows.add(paginationRow);
			}
			
			KeyboardRow backRow = new KeyboardRow();
			backRow.add("↩️ Back to Main Menu");
			keyboardRows.add(backRow);
			
			keyboard.setKeyboard(keyboardRows);
			keyboard.setResizeKeyboard(true);
			keyboard.setOneTimeKeyboard(false);

			// Show first 5 tasks by default
			StringBuilder message = new StringBuilder("📋 *All Tasks*\n\n");
			
			if (tasks.isEmpty()) {
				message = new StringBuilder("📭 *No Tasks Found*\n\n" +
										  "There are no tasks in the system yet.\n" +
										  "Use the \"Add Task\" option to create new tasks.");
			} else {
				int tasksToShow = Math.min(5, tasks.size());
				for (int i = 0; i < tasksToShow; i++) {
					TaskModel task = tasks.get(i);
					UserModel assignedTo = task.getAssignedToId() != null ? 
						userService.findUserById(task.getAssignedToId()) : null;
					UserModel createdBy = userService.findUserById(task.getCreatedById());
					
					message.append("🔹 *Task #" + task.getID() + "*\n")
						.append("📝 *Description:* ").append(task.getDescription()).append("\n")
						.append("📊 *Status:* ").append(getStatusEmoji(task.getStatus())).append(" ").append(task.getStatus()).append("\n")
						.append("⏱️ *Hours:* ").append(task.getEstimateHours() != null ? task.getEstimateHours() : "Not set").append(" (est.)");
					
					if (task.getRealHours() != null) {
						message.append(" / ").append(task.getRealHours()).append(" (real)");
					}
					
					message.append("\n")
						.append("📅 *Sprint:* ").append(task.getSprintId() != null ? task.getSprintId() : "Not set").append("\n");
					
					if (task.getCategory() != null) {
						message.append("🏷️ *Category:* ").append(getCategoryEmoji(task.getCategory())).append(" ").append(task.getCategory()).append("\n");
					}
					
					message.append("👤 *Assigned to:* ").append(assignedTo != null ? 
						assignedTo.getFirstName() + " " + assignedTo.getLastName() : "Unassigned").append("\n")
						.append("👥 *Created by:* ").append(createdBy.getFirstName() + " " + createdBy.getLastName()).append("\n")
						.append("📅 *Created:* ").append(task.getCreatedAt() != null ? task.getCreatedAt() : "Not set").append("\n\n");
				}
				
				if (tasks.size() > 5) {
					message.append("\nShowing 1-5 of ").append(tasks.size()).append(" tasks. Use the buttons below to navigate.");
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
			try {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Error al mostrar las tareas. Por favor, intenta nuevamente.");
				execute(errorMessage);
			} catch (TelegramApiException ex) {
				logger.error("Error sending error message", ex);
			}
		} catch (Exception e) {
			logger.error("Error viewing tasks: " + e.getMessage(), e);
			try {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Error al mostrar las tareas. Por favor, intenta nuevamente.");
				execute(errorMessage);
			} catch (TelegramApiException ex) {
				logger.error("Error sending error message", ex);
			}
		}
	}

	private String getStatusEmoji(String status) {
		if (status == null) return "❓";
		switch (status.toLowerCase()) {
			case "created":
				return "⭕";
			case "in_progress":
				return "📊";
			case "done":
				return "✅";
			default:
				return "❓";
		}
	}

	private String getCategoryEmoji(String category) {
		if (category == null) return "🏷️";
		switch (category.toLowerCase()) {
			case "bug":
				return "🐛";
			case "issue":
				return "📝";
			case "feature":
				return "✨";
			default:
				return "🏷️";
		}
	}

	/**
	 * Initiates the task creation process
	 * @param chatId - Telegram chat ID of the user
	 */
	private void handleAddTask(long chatId) {
		try {
			// Enviar mensaje para solicitar la descripción de la tarea
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("📝 *Crear Nueva Tarea*\n\n" +
									"Por favor, ingresa una descripción clara y detallada para la tarea.\n" +
									"Ejemplo: \"Implementar sistema de autenticación con JWT\"");

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
	
	/**
	 * Processes task description input and requests estimated hours
	 * @param chatId - Telegram chat ID of the user
	 * @param user - UserModel of the current user
	 * @param taskDescription - Description of the new task
	 */
	private void handleTaskDescription(long chatId, UserModel user, String taskDescription) {
		try {
			// Guardar la descripción temporalmente y solicitar las horas estimadas
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("⏱️ *Horas Estimadas*\n\n" +
									"¿Cuántas horas estimas que tomará completar esta tarea?\n" +
									"Ingresa un número (por ejemplo: 4, 2.5, 8)");

			// Guardar el estado del usuario como "waiting_for_task_estimate_hours"
			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_task_estimate_hours");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent(taskDescription);
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);

			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar las horas estimadas: " + e.getMessage());
		}
	}

	/**
	 * Processes estimated hours input and requests sprint assignment
	 * @param chatId - Telegram chat ID of the user
	 * @param user - UserModel of the current user
	 * @param estimateHoursText - Estimated hours for the task
	 */
	private void handleTaskEstimateHours(long chatId, UserModel user, String estimateHoursText) {
		try {
			// Validate that the estimated hours are a number
			double estimateHours;
			try {
				estimateHours = Double.parseDouble(estimateHoursText);
				if (estimateHours <= 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Por favor, ingresa un número válido mayor a 0 para las horas estimadas.");
				execute(errorMessage);
				return;
			}
	
			// Retrieve the task description saved previously
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String taskDescription = lastMessage.getContent();
	
			// Save the estimated hours and ask for the sprint number
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("📅 *Número de Sprint*\n\n" +
									"¿En qué sprint deseas agregar esta tarea?\n" +
									"Ingresa el número del sprint (por ejemplo: 1, 2, 3)");
	
			// Save the state of the user as "waiting_for_task_sprint"
			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_task_sprint");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent(taskDescription + "|" + estimateHours);
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);
	
			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar el número del sprint: " + e.getMessage());
		}
	}

	/**
	 * Processes sprint assignment and requests task category
	 * @param chatId - Telegram chat ID of the user
	 * @param user - UserModel of the current user
	 * @param sprintNumberText - Sprint number for the task
	 */
	private void handleTaskSprint(long chatId, UserModel user, String sprintNumberText) {
		try {
			// Validate that the sprint number is an integer
			int sprintNumber;
			try {
				sprintNumber = Integer.parseInt(sprintNumberText);
				if (sprintNumber <= 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Por favor, ingresa un número válido mayor a 0 para el sprint.");
				execute(errorMessage);
				return;
			}
	
			// Retrieve the task description and estimated hours saved previously
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String[] taskData = lastMessage.getContent().split("\\|");
			String taskDescription = taskData[0];
			double estimateHours = Double.parseDouble(taskData[1]);
	
			// Save the current state with sprint number
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_task_category");
			stateMessage.setRole("assistant");
			stateMessage.setContent(taskDescription + "|" + estimateHours + "|" + sprintNumber);
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);

			// Create keyboard for category options
			ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboardRows = new ArrayList<>();
			KeyboardRow row = new KeyboardRow();
			row.add("🐛 Bug");
			row.add("📝 Issue");
			row.add("✨ Feature");
			keyboardRows.add(row);
			keyboard.setKeyboard(keyboardRows);
			keyboard.setResizeKeyboard(true);

			// Ask for category
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("🏷️ *Categoría de la Tarea*\n\n" +
						   "Selecciona la categoría que mejor describe la tarea:\n\n" +
						   "🐛 *Bug* - Para corregir errores o problemas\n" +
						   "📝 *Issue* - Para mejoras o cambios en el sistema\n" +
						   "✨ *Feature* - Para nuevas funcionalidades");
			message.setReplyMarkup(keyboard);
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar la categoría de la tarea: " + e.getMessage());
		}
	}

	/**
	 * Processes task category selection and creates the task
	 * @param chatId - Telegram chat ID of the user
	 * @param user - UserModel of the current user
	 * @param category - Selected category for the task
	 */
	private void handleTaskCategory(long chatId, UserModel user, String category) {
		try {
			// Validate category
			String validCategory;
			String categoryDisplay;
			switch (category) {
				case "🐛 Bug":
					validCategory = "bug";
					categoryDisplay = "Bug";
					break;
				case "📝 Issue":
					validCategory = "issue";
					categoryDisplay = "Issue";
					break;
				case "✨ Feature":
					validCategory = "feature";
					categoryDisplay = "Feature";
					break;
				default:
					SendMessage errorMessage = new SendMessage();
					errorMessage.setChatId(chatId);
					errorMessage.setText("❌ Por favor, selecciona una categoría válida.");
					execute(errorMessage);
					return;
			}

			// Retrieve the task data saved previously
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String[] taskData = lastMessage.getContent().split("\\|");
			String taskDescription = taskData[0];
			double estimateHours = Double.parseDouble(taskData[1]);
			int sprintNumber = Integer.parseInt(taskData[2]);

			// Create the new task
			TaskDTO newTask = new TaskDTO();
			newTask.setDescription(taskDescription);
			newTask.setEstimateHours(estimateHours);
			newTask.setSprint(sprintNumber);
			newTask.setStatus("created");
			newTask.setCreatedBy(user.getID());
			newTask.setAssignedTo(user.getID());
			newTask.setCategory(validCategory);

			// Save the task and get its ID
			TaskModel savedTask = taskService.addTodoItemToProject(user.getProjectId(), newTask);

			// Create keyboard with back button
			ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboardRows = new ArrayList<>();
			KeyboardRow row = new KeyboardRow();
			row.add("↩️ Back to Main Menu");
			keyboardRows.add(row);
			keyboard.setKeyboard(keyboardRows);
			keyboard.setResizeKeyboard(true);

			// Confirm to the user that the task was created with its ID
			SendMessage confirmationMessage = new SendMessage();
			confirmationMessage.setChatId(chatId);
			confirmationMessage.setText("✅ *¡Tarea Creada Exitosamente!*\n\n" +
									 "🆔 *ID de la Tarea:* " + savedTask.getID() + "\n" +
									 "📝 *Descripción:* " + taskDescription + "\n" +
									 "⏱️ *Horas Estimadas:* " + estimateHours + "\n" +
									 "📅 *Sprint:* " + sprintNumber + "\n" +
									 "🏷️ *Categoría:* " + categoryDisplay + "\n\n" +
									 "Puedes ver esta tarea en el menú \"View Tasks\"");
			confirmationMessage.setReplyMarkup(keyboard);
			confirmationMessage.enableMarkdown(true);
			execute(confirmationMessage);

			// Show the main menu based on user role
			if (user.getRole().equals("developer")) {
				showDeveloperMainMenu(chatId);
			} else {
				showManagerMainMenu(chatId);
			}
		} catch (TelegramApiException e) {
			logger.error("Error al crear la tarea: " + e.getMessage());
		}
	}
	
	/**
	 * Shows filter options for tasks
	 * @param chatId - Telegram chat ID of the user
	 */
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
	
	/**
	 * Initiates the task update process
	 * @param chatId - Telegram chat ID of the user
	 */
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

	/**
	 * Processes task ID input and shows update options
	 * @param chatId - Telegram chat ID of the user
	 * @param taskIdText - ID of the task to update
	 */
	private void handleTaskUpdate(long chatId, String taskIdText) {
		try {
			int taskId = Integer.parseInt(taskIdText);
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {
				TaskModel task = taskOptional.get();
				UserModel assignedTo = task.getAssignedToId() != null ? 
						userService.findUserById(task.getAssignedToId()) : null;
				UserModel createdBy = userService.findUserById(task.getCreatedById());

				// Show current task details
				StringBuilder currentTaskInfo = new StringBuilder();
				currentTaskInfo.append("📋 *Estado Actual de la Tarea #").append(taskId).append("*\n\n")
					.append("📝 *Descripción:* ").append(task.getDescription()).append("\n")
					.append("📊 *Estado:* ").append(getStatusEmoji(task.getStatus())).append(" ").append(task.getStatus()).append("\n")
					.append("⏱️ *Horas Estimadas:* ").append(task.getEstimateHours() != null ? task.getEstimateHours() : "Not set").append("\n");
				
				if (task.getRealHours() != null) {
					currentTaskInfo.append("⏰ *Horas Reales:* ").append(task.getRealHours()).append("\n");
				}
				
				currentTaskInfo.append("📅 *Sprint:* ").append(task.getSprintId() != null ? task.getSprintId() : "Not set").append("\n");
				
				if (task.getCategory() != null) {
					currentTaskInfo.append("🏷️ *Categoría:* ").append(getCategoryEmoji(task.getCategory())).append(" ").append(task.getCategory()).append("\n");
				}
				
				currentTaskInfo.append("👤 *Asignado a:* ").append(assignedTo != null ? 
						assignedTo.getFirstName() + " " + assignedTo.getLastName() : "Unassigned").append("\n")
					.append("👥 *Creado por:* ").append(createdBy.getFirstName() + " " + createdBy.getLastName()).append("\n")
					.append("📅 *Creado:* ").append(task.getCreatedAt() != null ? task.getCreatedAt() : "Not set").append("\n\n")
					.append("¿Qué campo deseas actualizar?");

				// Create keyboard for update options
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				KeyboardRow row1 = new KeyboardRow();
				row1.add("Edit Description");
				row1.add("Edit Status");
				keyboardRows.add(row1);
				keyboard.setKeyboard(keyboardRows);
				keyboard.setResizeKeyboard(true);
				
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText(currentTaskInfo.toString());
				message.setReplyMarkup(keyboard);
				message.enableMarkdown(true);
				
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
				message.setText("❌ Tarea no encontrada. Por favor, ingresa un ID de tarea válido.");
				execute(message);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid task ID format", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("❌ Formato de ID inválido. Por favor, ingresa un número válido.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		} catch (Exception e) {
			logger.error("Error updating task", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("❌ Ocurrió un error al actualizar la tarea. Por favor, intenta nuevamente.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}

	/**
	 * Processes update selection and requests new value
	 * @param chatId - Telegram chat ID of the user
	 * @param selection - Selected field to update
	 */
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
	
	/**
	 * Processes new description input and updates the task
	 * @param chatId - Telegram chat ID of the user
	 * @param newDescription - New description for the task
	 */
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

	/**
	 * Processes new status input and updates the task
	 * @param chatId - Telegram chat ID of the user
	 * @param newStatus - New status for the task
	 */
	private void handleNewStatus(long chatId, String newStatus) {
		try {
			List<MessageModel> messages = messageService.findMessagesFromChat(chatId);
			int taskId = Integer.parseInt(messages.get(4).getContent());
			
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {
				TaskModel task = taskOptional.get();
				task.setStatus(newStatus);
				taskService.save(task);
				
				SendMessage message = new SendMessage();
				message.setChatId(chatId);

				if (newStatus.equals("done")) {
					message.setText("✅ Estado actualizado exitosamente! Por favor, ingresa las horas reales gastadas en la tarea:");
					MessageModel stateMessage = new MessageModel();
					stateMessage.setMessageType("waiting_for_real_hours_spent");
					stateMessage.setRole("assistant");
					stateMessage.setUserId(chatId);
					stateMessage.setCreatedAt(OffsetDateTime.now());
					messageService.saveMessage(stateMessage);
				
					execute(message);
				} else {
					message.setText("✅ Estado actualizado exitosamente!");
					execute(message);
					
					// Get user to determine which menu to show
					UserModel user = userService.findUserByChatId(chatId);
					if (user != null) {
						if (user.getRole().equals("developer")) {
					showDeveloperMainMenu(chatId);
						} else {
							showManagerMainMenu(chatId);
				}
					}
				}
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("❌ Tarea no encontrada. Por favor, ingresa un ID de tarea válido.");
				execute(message);
				
				// Get user to determine which menu to show
				UserModel user = userService.findUserByChatId(chatId);
				if (user != null) {
					if (user.getRole().equals("developer")) {
						showDeveloperMainMenu(chatId);
					} else {
						showManagerMainMenu(chatId);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error updating task status", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("❌ Ocurrió un error al actualizar el estado de la tarea. Por favor, intenta nuevamente.");
			try {
				execute(message);
				
				// Get user to determine which menu to show
				UserModel user = userService.findUserByChatId(chatId);
				if (user != null) {
					if (user.getRole().equals("developer")) {
						showDeveloperMainMenu(chatId);
					} else {
						showManagerMainMenu(chatId);
					}
				}
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		}
	}

	/**
	 * Processes real hours input and updates the task
	 * @param chatId - Telegram chat ID of the user
	 * @param realHours - Actual hours spent on the task
	 */
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
	
	/**
	 * Shows task details for a specific task
	 * @param chatId - Telegram chat ID of the user
	 */
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
	


	/**
	 * Shows tasks assigned to the current user
	 * @param chatId - Telegram chat ID of the user
	 * @param user - UserModel of the current user
	 */
	private void handleMyTasks(long chatId, UserModel user) {
		try {
			List<TaskModel> tasks = taskService.findByUserAssigned(user.getID());
			sendTaskList(chatId, tasks, "📋 *My Assigned Tasks*\n\n");
		} catch (Exception e) {
			logger.error("Error getting user tasks", e);
			
		}
	}

	/**
	 * Initiates the task deletion process
	 * @param chatId - Telegram chat ID of the user
	 */
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


	/**
	 * Processes task deletion request
	 * @param chatId - Telegram chat ID of the user
	 * @param taskIdText - ID of the task to delete
	 */
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
	 * Shows tasks filtered by status
	 * @param chatId - Telegram chat ID of the user
	 * @param status - Status to filter tasks by
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

	/**
	 * Sends a formatted list of tasks to the user
	 * @param chatId - Telegram chat ID of the user
	 * @param tasks - List of tasks to display
	 * @param title - Title for the task list
	 */
	private void sendTaskList(long chatId, List<TaskModel> tasks, String title) {
		try {
			StringBuilder message = new StringBuilder(title);
			
			if (tasks.isEmpty()) {
				message.append("📭 *No Tasks Found*\n\n" +
							  "There are no tasks matching your criteria.\n" +
							  "Try using different filters or create new tasks.");
			} else {
				for (TaskModel task : tasks) {
					UserModel assignedTo = task.getAssignedToId() != null ? 
						userService.findUserById(task.getAssignedToId()) : null;
					UserModel createdBy = userService.findUserById(task.getCreatedById());
					
					message.append("🔹 *Task #" + task.getID() + "*\n")
						.append("📝 *Description:* ").append(task.getDescription()).append("\n")
						.append("📊 *Status:* ").append(getStatusEmoji(task.getStatus())).append(" ").append(task.getStatus()).append("\n")
						.append("⏱️ *Hours:* ").append(task.getEstimateHours() != null ? task.getEstimateHours() : "Not set").append(" (est.)");
					
					if (task.getRealHours() != null) {
						message.append(" / ").append(task.getRealHours()).append(" (real)");
					}
					
					message.append("\n")
						.append("📅 *Sprint:* ").append(task.getSprintId() != null ? task.getSprintId() : "Not set").append("\n");
					
					if (task.getCategory() != null) {
						message.append("🏷️ *Category:* ").append(getCategoryEmoji(task.getCategory())).append(" ").append(task.getCategory()).append("\n");
					}
					
					message.append("👤 *Assigned to:* ").append(assignedTo != null ? 
						assignedTo.getFirstName() + " " + assignedTo.getLastName() : "Unassigned").append("\n")
						.append("👥 *Created by:* ").append(createdBy.getFirstName() + " " + createdBy.getLastName()).append("\n")
						.append("📅 *Created:* ").append(task.getCreatedAt() != null ? task.getCreatedAt() : "Not set").append("\n\n");
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
			try {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Error al mostrar las tareas. Por favor, intenta nuevamente.");
				execute(errorMessage);
			} catch (TelegramApiException ex) {
				logger.error("Error sending error message", ex);
			}
		}
	}

	/**
	 * Initiates the sprint creation process
	 * @param chatId - Telegram chat ID of the user
	 */
	private void handleCreateSprint(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("📅 *Crear Nuevo Sprint*\n\n" +
						  "Por favor, ingresa un nombre descriptivo para el sprint.\n" +
						  "Ejemplo: \"Sprint 1 - Implementación Inicial\"\n\n" +
						  "💡 *Consejo:* Usa un nombre que refleje el objetivo principal del sprint.");

			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_sprint_name");
			stateMessage.setRole("assistant");
			stateMessage.setContent("Por favor ingresa el nombre del nuevo sprint:");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);

			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error initiating sprint creation", e);
		}
	}

	/**
	 * Processes sprint name input and requests start date
	 * @param chatId - Telegram chat ID of the user
	 * @param sprintName - Name of the new sprint
	 */
	private void handleSetSprintName(long chatId, String sprintName) {
		try {
			if (sprintName.trim().isEmpty()) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ El nombre del sprint no puede estar vacío. Por favor, ingresa un nombre válido.");
				execute(errorMessage);
				return;
			}

			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("📅 *Fecha de Inicio*\n\n" +
									"Ahora, ingresa la fecha de inicio del sprint.\n" +
									"Formato: YYYY-MM-DD\n" +
									"Ejemplo: 2024-04-08\n\n" +
									"💡 *Consejo:* La fecha debe ser posterior o igual a hoy.");

			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_start_date");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent(sprintName);
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);

			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar la fecha de inicio: " + e.getMessage());
		}
	}

	/**
	 * Processes start date input and requests end date
	 * @param chatId - Telegram chat ID of the user
	 * @param sprintStartDate - Start date for the sprint
	 */
	private void handleSetSprintStartDate(long chatId, String sprintStartDate) {
		try {
			if (!sprintStartDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Formato de fecha inválido.\n\n" +
								   "Por favor, ingresa la fecha en el formato YYYY-MM-DD\n" +
								   "Ejemplo: 2024-04-08");
				execute(errorMessage);
				return;
			}

			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String sprintName = lastMessage.getContent();

			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("📅 *Fecha de Finalización*\n\n" +
									"Ingresa la fecha de finalización del sprint.\n" +
									"Formato: YYYY-MM-DD\n" +
									"Ejemplo: 2024-04-22\n\n" +
									"💡 *Consejo:* Un sprint típico dura entre 1 y 4 semanas.");

			MessageModel assistantMessage = new MessageModel();
			assistantMessage.setMessageType("waiting_for_end_date");
			assistantMessage.setRole("assistant");
			assistantMessage.setContent(sprintName + "|" + sprintStartDate);
			assistantMessage.setUserId(chatId);
			assistantMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(assistantMessage);

			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error("Error al solicitar la fecha de finalización: " + e.getMessage());
		}
	}

	/**
	 * Processes end date input and creates the sprint
	 * @param chatId - Telegram chat ID of the user
	 * @param sprintEndDate - End date for the sprint
	 */
	private void handleSetSprintEndDate(long chatId, String sprintEndDate) {
		try {
			if (!sprintEndDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ Formato de fecha inválido.\n\n" +
								   "Por favor, ingresa la fecha en el formato YYYY-MM-DD\n" +
								   "Ejemplo: 2024-04-22");
				execute(errorMessage);
				return;
			}

			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String[] sprintData = lastMessage.getContent().split("\\|");
			String sprintName = sprintData[0];
			String sprintStartDate = sprintData[1];

			// Validate dates
			OffsetDateTime startDate = OffsetDateTime.parse(sprintStartDate + "T00:00:00Z");
			OffsetDateTime endDate = OffsetDateTime.parse(sprintEndDate + "T00:00:00Z");
			
			if (endDate.isBefore(startDate)) {
				SendMessage errorMessage = new SendMessage();
				errorMessage.setChatId(chatId);
				errorMessage.setText("❌ La fecha de finalización debe ser posterior a la fecha de inicio.\n\n" +
								   "Fecha de inicio: " + sprintStartDate + "\n" +
								   "Por favor, ingresa una fecha válida.");
				execute(errorMessage);
				return;
			}

			SprintDTO newSprint = new SprintDTO();
			newSprint.setName(sprintName);
			newSprint.setStartedAt(startDate);
			newSprint.setEndsAt(endDate);

			UserModel user = userService.findUserByChatId(chatId);
			if (user != null) {
				sprintService.addSprintToProject(user.getProjectId(), newSprint);

				SendMessage confirmationMessage = new SendMessage();
				confirmationMessage.setChatId(chatId);
				confirmationMessage.setText("✅ *¡Sprint Creado Exitosamente!*\n\n" +
										 "🏷️ *Nombre:* " + sprintName + "\n" +
										 "📅 *Fecha de Inicio:* " + sprintStartDate + "\n" +
										 "📅 *Fecha de Finalización:* " + sprintEndDate + "\n\n" +
										 "Puedes ver y gestionar este sprint desde el menú principal.");
				confirmationMessage.enableMarkdown(true);
				execute(confirmationMessage);

				if (user.getRole().equals("developer")) {
					showDeveloperMainMenu(chatId);
				} else {
					showManagerMainMenu(chatId);
				}
			}
		} catch (TelegramApiException e) {
			logger.error("Error al confirmar la creación del sprint: " + e.getMessage());
		}
	}

	/**
	 * Initiates the sprint update process
	 * @param chatId - Telegram chat ID of the user
	 */
	private void handleUpdateSprint(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Por favor ingresa el nombre del sprint que deseas actualizar:");

			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_sprint_update_name");
			stateMessage.setRole("assistant");
			stateMessage.setContent("Por favor ingresa el nombre del sprint que deseas actualizar:");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);

			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error initiating sprint update", e);
		}
	}

	/**
	 * Processes sprint name input and shows update options
	 * @param chatId - Telegram chat ID of the user
	 * @param sprintName - Name of the sprint to update
	 */
	private void handleSprintUpdateName(long chatId, String sprintName) {
		try {
			// Get the user's project ID
			UserModel user = userService.findUserByChatId(chatId);
			if (user != null) {
				// Find the sprint by name
				SprintModel sprint = sprintRepository.findByProjectIdAndName(user.getProjectId(), sprintName);
				
				if (sprint != null) {
					// Show current sprint details
					StringBuilder currentSprintInfo = new StringBuilder();
					currentSprintInfo.append("📋 *Estado Actual del Sprint*\n\n")
						.append("🏷️ *Nombre:* ").append(sprint.getName()).append("\n")
						.append("📝 *Descripción:* ").append(sprint.getDescription() != null ? sprint.getDescription() : "No hay descripción").append("\n")
						.append("📅 *Fecha de Inicio:* ").append(sprint.getStartedAt()).append("\n")
						.append("📅 *Fecha de Finalización:* ").append(sprint.getEndsAt()).append("\n\n")
						.append("¿Qué campo deseas actualizar?");

					// Create keyboard for update options
					ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
					List<KeyboardRow> keyboardRows = new ArrayList<>();
					KeyboardRow row1 = new KeyboardRow();
					row1.add("Edit Name");
					row1.add("Edit Description");
					keyboardRows.add(row1);
					KeyboardRow row2 = new KeyboardRow();
					row2.add("Edit Start Date");
					row2.add("Edit End Date");
					keyboardRows.add(row2);
					keyboard.setKeyboard(keyboardRows);
					keyboard.setResizeKeyboard(true);

					SendMessage message = new SendMessage();
					message.setChatId(chatId);
					message.setText(currentSprintInfo.toString());
					message.setReplyMarkup(keyboard);
					message.enableMarkdown(true);

					// Save state for next message
					MessageModel stateMessage = new MessageModel();
					stateMessage.setMessageType("waiting_for_sprint_update_selection");
					stateMessage.setRole("assistant");
					stateMessage.setContent(sprintName); // Save sprint name for later use
					stateMessage.setUserId(chatId);
					stateMessage.setCreatedAt(OffsetDateTime.now());
					messageService.saveMessage(stateMessage);

					execute(message);
				} else {
					SendMessage message = new SendMessage();
					message.setChatId(chatId);
					message.setText("❌ No se encontró el sprint '" + sprintName + "'. Por favor, intenta con otro nombre.");
					execute(message);
					showManagerMainMenu(chatId);
				}
			}
		} catch (TelegramApiException e) {
			logger.error("Error showing sprint details", e);
		}
	}

	/**
	 * Processes update selection and requests new value
	 * @param chatId - Telegram chat ID of the user
	 * @param selection - Selected field to update
	 */
	private void handleSprintUpdateSelection(long chatId, String selection) {
		try {
			// Get the sprint name from the previous message
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String sprintName = lastMessage.getContent();

			// Save the selection and ask for the new value
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			
			String prompt = "";
			switch (selection) {
				case "Edit Name":
					prompt = "Por favor ingresa el nuevo nombre del sprint:";
					break;
				case "Edit Description":
					prompt = "Por favor ingresa la nueva descripción del sprint:";
					break;
				case "Edit Start Date":
					prompt = "Por favor ingresa la nueva fecha de inicio (formato: YYYY-MM-DD):";
					break;
				case "Edit End Date":
					prompt = "Por favor ingresa la nueva fecha de finalización (formato: YYYY-MM-DD):";
					break;
				default:
					message.setText("❌ Selección inválida. Por favor, selecciona una opción válida.");
					execute(message);
					return;
			}

			message.setText(prompt);

			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_sprint_update_value");
			stateMessage.setRole("assistant");
			stateMessage.setContent(sprintName + "|" + selection); // Save sprint name and selection
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);

			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error handling sprint update selection", e);
		}
	}

	/**
	 * Processes new value input and updates the sprint
	 * @param chatId - Telegram chat ID of the user
	 * @param newValue - New value for the selected field
	 */
	private void handleSprintUpdateValue(long chatId, String newValue) {
		try {
			// Get the sprint data from the previous message
			MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
			String[] sprintData = lastMessage.getContent().split("\\|");
			String sprintName = sprintData[0];
			String fieldToUpdate = sprintData[1];

			// Get the user's project ID
			UserModel user = userService.findUserByChatId(chatId);
			if (user != null) {
				// Find the sprint by name
				SprintModel sprint = sprintRepository.findByProjectIdAndName(user.getProjectId(), sprintName);
				
				if (sprint != null) {
					// Create a SprintDTO with the new value
					SprintDTO sprintUpdate = new SprintDTO();
					
					switch (fieldToUpdate) {
						case "Edit Name":
							sprintUpdate.setName(newValue);
							break;
						case "Edit Description":
							sprintUpdate.setDescription(newValue);
							break;
						case "Edit Start Date":
							if (!newValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
								throw new IllegalArgumentException("Formato de fecha inválido");
							}
							sprintUpdate.setStartedAt(OffsetDateTime.parse(newValue + "T00:00:00Z"));
							break;
						case "Edit End Date":
							if (!newValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
								throw new IllegalArgumentException("Formato de fecha inválido");
							}
							sprintUpdate.setEndsAt(OffsetDateTime.parse(newValue + "T00:00:00Z"));
							break;
					}

					// Update the sprint
					SprintModel updatedSprint = sprintService.patchSprintFromProject(sprint.getID(), user.getProjectId(), sprintUpdate);

					// Show confirmation message
					SendMessage message = new SendMessage();
					message.setChatId(chatId);
					message.setText("✅ *Sprint Actualizado Exitosamente!*\n\n" +
								  "🏷️ *Nombre:* " + updatedSprint.getName() + "\n" +
								  "📝 *Descripción:* " + (updatedSprint.getDescription() != null ? updatedSprint.getDescription() : "No hay descripción") + "\n" +
								  "📅 *Fecha de Inicio:* " + updatedSprint.getStartedAt() + "\n" +
								  "📅 *Fecha de Finalización:* " + updatedSprint.getEndsAt());
					message.enableMarkdown(true);
					execute(message);

					// Show the main menu
					showManagerMainMenu(chatId);
				}
			}
		} catch (IllegalArgumentException e) {
			SendMessage errorMessage = new SendMessage();
			errorMessage.setChatId(chatId);
			errorMessage.setText("❌ Formato de fecha inválido. Por favor, ingresa la fecha en el formato YYYY-MM-DD.");
			try {
				execute(errorMessage);
			} catch (TelegramApiException ex) {
				logger.error("Error sending message", ex);
			}
		} catch (TelegramApiException e) {
			logger.error("Error updating sprint", e);
		}
	}

	/**
	 * Initiates the sprint deletion process
	 * @param chatId - Telegram chat ID of the user
	 */
	private void handleDeleteSprint(long chatId) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Por favor ingresa el nombre del sprint que deseas eliminar:");

			// Save state for next message
			MessageModel stateMessage = new MessageModel();
			stateMessage.setMessageType("waiting_for_sprint_delete_name");
			stateMessage.setRole("assistant");
			stateMessage.setContent("Por favor ingresa el nombre del sprint que deseas eliminar:");
			stateMessage.setUserId(chatId);
			stateMessage.setCreatedAt(OffsetDateTime.now());
			messageService.saveMessage(stateMessage);

			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error initiating sprint deletion", e);
		}
	}

	/**
	 * Processes sprint deletion request
	 * @param chatId - Telegram chat ID of the user
	 * @param sprintName - Name of the sprint to delete
	 */
	private void handleSprintDeletion(long chatId, String sprintName) {
		try {
			// Get the user's project ID
			UserModel user = userService.findUserByChatId(chatId);
			if (user != null) {
				// Delete the sprint from the database
				boolean isDeleted = sprintService.deleteSprintByName(user.getProjectId(), sprintName);

				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				if (isDeleted) {
					message.setText("✅ *Sprint Eliminado Exitosamente!*\n\n" +
								  "El sprint '" + sprintName + "' ha sido eliminado.");
				} else {
					message.setText("❌ *Error al Eliminar Sprint*\n\n" +
								  "No se encontró el sprint '" + sprintName + "' o no se pudo eliminar.");
				}
				message.enableMarkdown(true);
				execute(message);

				// Show the main menu
				showManagerMainMenu(chatId);
			}
		} catch (TelegramApiException e) {
			logger.error("Error deleting sprint", e);
		}
	}

	/**
	 * Shows the main menu for developers
	 * @param chatId - Telegram chat ID of the user
	 */
	private void showDeveloperMainMenu(long chatId) {
		try {
			// Create developer keyboard
			ReplyKeyboardMarkup devKeyboardMarkup = new ReplyKeyboardMarkup();
			List<KeyboardRow> devKeyboard = new ArrayList<>();
			
			// First row - View and Filter tasks
			KeyboardRow firstRow = new KeyboardRow();
			firstRow.add(BotLabels.VIEW_TASKS.getLabel());
			firstRow.add(BotLabels.FILTER_TASKS.getLabel());
			devKeyboard.add(firstRow);
			
			// Second row - Add and Update tasks
			KeyboardRow secondRow = new KeyboardRow();
			secondRow.add(BotLabels.ADD_TASK.getLabel());
			secondRow.add(BotLabels.UPDATE_TASK.getLabel());
			devKeyboard.add(secondRow);

			// Third row - Details and Delete
			KeyboardRow thirdRow = new KeyboardRow();
			thirdRow.add(BotLabels.DETAILS.getLabel());
			thirdRow.add(BotLabels.DELETE_TASK.getLabel());
			devKeyboard.add(thirdRow);
			
			// Fourth row - Help
			KeyboardRow fourthRow = new KeyboardRow();
			fourthRow.add(BotLabels.HELP.getLabel());
			devKeyboard.add(fourthRow);
			
			// Configure keyboard
			devKeyboardMarkup.setKeyboard(devKeyboard);
			devKeyboardMarkup.setResizeKeyboard(true);
			devKeyboardMarkup.setOneTimeKeyboard(false);
			
			// Create and send message with keyboard
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("👋 Welcome to TaskBot!\n\n" +
						  "Here are your available options:\n" +
						  "📋 View Tasks - See all your tasks\n" +
						  "🔍 Filter Tasks - Filter tasks by status\n" +
						  "➕ Add Task - Create a new task\n" +
						  "✏️ Update Task - Modify existing tasks\n" +
						  "ℹ️ Details - View task details\n" +
						  "🗑️ Delete Task - Remove tasks\n" +
						  "❓ Help - Get assistance\n\n" +
						  "Please select an option:");
			message.setReplyMarkup(devKeyboardMarkup);
			
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error showing developer menu", e);
		}
	}

	/**
	 * Shows the main menu for managers
	 * @param chatId - Telegram chat ID of the user
	 */
	private void showManagerMainMenu(long chatId) {
		try {
			// Create manager keyboard
			ReplyKeyboardMarkup managerKeyboardMarkup = new ReplyKeyboardMarkup();
			List<KeyboardRow> manKeyboard = new ArrayList<>();
			
			// First row - View and Filter tasks
			KeyboardRow firstRow = new KeyboardRow();
			firstRow.add(BotLabels.VIEW_TASKS.getLabel());
			firstRow.add(BotLabels.FILTER_TASKS.getLabel());
			manKeyboard.add(firstRow);
			
			// Second row - Add and Update tasks
			KeyboardRow secondRow = new KeyboardRow();
			secondRow.add(BotLabels.ADD_TASK.getLabel());
			secondRow.add(BotLabels.UPDATE_TASK.getLabel());
			manKeyboard.add(secondRow);

			// Third row - Details and Sprints
			KeyboardRow thirdRow = new KeyboardRow();
			thirdRow.add(BotLabels.DETAILS.getLabel());
			thirdRow.add(BotLabels.CREATE_SPRINT.getLabel());
			manKeyboard.add(thirdRow);
			
			// Fourth row - Sprint Management
			KeyboardRow fourthRow = new KeyboardRow();
			fourthRow.add("✏️ Update Sprint");
			fourthRow.add(BotLabels.DELETE_SPRINT.getLabel());
			manKeyboard.add(fourthRow);

			// Fifth row - View Sprints and Help
			KeyboardRow fifthRow = new KeyboardRow();
			fifthRow.add("📋 View Sprints");
			fifthRow.add(BotLabels.HELP.getLabel());
			manKeyboard.add(fifthRow);

			// Configure keyboard
			managerKeyboardMarkup.setKeyboard(manKeyboard);
			managerKeyboardMarkup.setResizeKeyboard(true);
			managerKeyboardMarkup.setOneTimeKeyboard(false);
			
			// Create and send message with keyboard
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("👋 Welcome to TaskBot Manager!\n\n" +
						  "Here are your available options:\n" +
						  "📋 View Tasks - See all project tasks\n" +
						  "🔍 Filter Tasks - Filter tasks by status\n" +
						  "➕ Add Task - Create new tasks\n" +
						  "✏️ Update Task - Modify existing tasks\n" +
						  "📅 *Sprint Management*\n" +
						  "- Create new sprints\n" +
						  "- Update sprint details\n" +
						  "- Track sprint progress\n\n" +
						  "ℹ️ *Details*\n" +
						  "View detailed information about any task\n\n" +
						  "Need more help? Contact system administrator.");
			message.setReplyMarkup(managerKeyboardMarkup);
			
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error showing Manager menu", e);
		}
	}

	/**
	 * Handles task list pagination
	 * @param chatId - Telegram chat ID of the user
	 * @param action - Pagination action (First/Next)
	 */
	private void handleTaskPagination(long chatId, String action) {
		try {
			List<TaskModel> tasks = taskService.findAll();
			tasks.sort(Comparator.comparingInt(TaskModel::getID));
			
			// Create keyboard with pagination options
			ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboardRows = new ArrayList<>();
			
			// Add pagination buttons if there are more than 5 tasks
			if (tasks.size() > 5) {
				KeyboardRow paginationRow = new KeyboardRow();
				paginationRow.add("⏮️ First 5");
				paginationRow.add("⏭️ Next 5");
				keyboardRows.add(paginationRow);
			}
			
			KeyboardRow backRow = new KeyboardRow();
			backRow.add("↩️ Back to Main Menu");
			keyboardRows.add(backRow);
			
			keyboard.setKeyboard(keyboardRows);
			keyboard.setResizeKeyboard(true);
			keyboard.setOneTimeKeyboard(false);

			StringBuilder message = new StringBuilder("📋 *All Tasks*\n\n");
			
			if (tasks.isEmpty()) {
				message = new StringBuilder("No tasks found. Use the Add Task option to create new tasks.");
			} else {
				// Get the current page from the last message
				MessageModel lastMessage = messageService.findLastAssistantMessageByUserId(chatId);
				int currentPage = 1; // Default to first page
				
				if (lastMessage != null && lastMessage.getMessageType().equals("task_pagination")) {
					currentPage = Integer.parseInt(lastMessage.getContent());
				}
				
				// Calculate start and end indices based on current page and action
				if (action.equals("⏮️ First 5")) {
					currentPage = 1;
				} else if (action.equals("⏭️ Next 5")) {
					currentPage++;
				}
				
				int startIndex = (currentPage - 1) * 5;
				int endIndex = Math.min(startIndex + 5, tasks.size());
				
				// If we're at the last page and trying to go next, stay on the last page
				if (startIndex >= tasks.size()) {
					currentPage--;
					startIndex = (currentPage - 1) * 5;
					endIndex = Math.min(startIndex + 5, tasks.size());
				}
				
				// Save current page state
				MessageModel stateMessage = new MessageModel();
				stateMessage.setMessageType("task_pagination");
				stateMessage.setRole("assistant");
				stateMessage.setContent(String.valueOf(currentPage));
				stateMessage.setUserId(chatId);
				stateMessage.setCreatedAt(OffsetDateTime.now());
				messageService.saveMessage(stateMessage);
				
				for (int i = startIndex; i < endIndex; i++) {
					TaskModel task = tasks.get(i);
					message.append("🔹 *Task ID:* `").append(task.getID()).append("`\n")
						.append("📝 *Description:* ").append(task.getDescription()).append("\n")
						.append("📊 *Status:* ").append(task.getStatus()).append("\n")
						.append("👤 *Assigned to:* ").append(task.getAssignedToId() != null ? 
							userService.findUserById(task.getAssignedToId()).getFirstName() : "Unknown").append("\n\n");
				}
				
				if (tasks.size() > 5) {
					message.append("\nShowing ").append(startIndex + 1).append("-").append(endIndex)
						.append(" of ").append(tasks.size()).append(" tasks. Use the buttons below to navigate.");
				}
			}

			SendMessage response = new SendMessage();
			response.setChatId(chatId);
			response.setText(message.toString());
			response.enableMarkdown(true);
			response.setReplyMarkup(keyboard);

			execute(response);
		} catch (Exception e) {
			logger.error("Error handling task pagination: " + e.getMessage(), e);
		}
	}

	/**
	 * Shows help information based on user role
	 * @param chatId - Telegram chat ID of the user
	 * @param role - User role (developer/manager)
	 */
	private void handleHelp(long chatId, String role) {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			
			if (role.equals("developer")) {
				message.setText("🤖 *TaskBot Help - Developer Guide*\n\n" +
							  "Here's how to use TaskBot:\n\n" +
							  "📋 *View Tasks*\n" +
							  "See all your assigned tasks\n\n" +
							  "🔍 *Filter Tasks*\n" +
							  "Filter tasks by status (Created, In Progress, Done)\n\n" +
							  "➕ *Add Task*\n" +
							  "Create a new task by providing:\n" +
							  "1. Task description\n" +
							  "2. Estimated hours\n" +
							  "3. Sprint number\n\n" +
							  "✏️ *Update Task*\n" +
							  "Modify existing tasks by:\n" +
							  "1. Entering task ID\n" +
							  "2. Selecting what to update\n" +
							  "3. Providing new values\n\n" +
							  "ℹ️ *Details*\n" +
							  "View detailed information about a specific task\n\n" +
							  "🗑️ *Delete Task*\n" +
							  "Remove tasks by entering their ID\n\n" +
							  "Need more help? Contact your manager.");
			} else {
				message.setText("🤖 *TaskBot Help - Manager Guide*\n\n" +
							  "Here's how to use TaskBot:\n\n" +
							  "📋 *View Tasks*\n" +
							  "See all project tasks\n\n" +
							  "🔍 *Filter Tasks*\n" +
							  "Filter tasks by status (Created, In Progress, Done)\n\n" +
							  "➕ *Add Task*\n" +
							  "Create new tasks for developers\n\n" +
							  "✏️ *Update Task*\n" +
							  "Modify any task in the project\n\n" +
							  "📅 *Sprint Management*\n" +
							  "- Create new sprints\n" +
							  "- Update sprint details\n" +
							  "- Track sprint progress\n\n" +
							  "ℹ️ *Details*\n" +
							  "View detailed information about any task\n\n" +
							  "Need more help? Contact system administrator.");
			}
			
			message.enableMarkdown(true);
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error showing help", e);
		}
	}

	/**
	 * Shows detailed information about a specific task
	 * @param chatId - Telegram chat ID of the user
	 * @param taskIdText - ID of the task to show details for
	 */
	private void handleTaskDetailsResponse(long chatId, String taskIdText) {
		try {
			int taskId = Integer.parseInt(taskIdText);
			Optional<TaskModel> taskOptional = taskService.getItemById(taskId);
			
			if (taskOptional.isPresent()) {
				TaskModel task = taskOptional.get();
				
				// Get user information
				UserModel createdBy = userService.findUserById(task.getCreatedById());
				UserModel assignedTo = task.getAssignedToId() != null ? 
						userService.findUserById(task.getAssignedToId()) : null;
				
				// Format the message with task details
				StringBuilder messageText = new StringBuilder();
				messageText.append("📋 *Task Details*\n\n")
						  .append("🆔 *ID:* `").append(task.getID()).append("`\n")
						  .append("📝 *Description:* ").append(task.getDescription()).append("\n")
						  .append("📊 *Status:* ").append(task.getStatus()).append("\n")
						  .append("⏱️ *Estimated Hours:* ").append(task.getEstimateHours() != null ? 
							  task.getEstimateHours() : "Not set").append("\n")
						  .append("⏰ *Real Hours:* ").append(task.getRealHours() != null ? 
							  task.getRealHours() : "Not set").append("\n")
						  .append("📅 *Sprint:* ").append(task.getSprintId()).append("\n")
						  .append("👤 *Created by:* ").append(createdBy.getFirstName()).append(" ")
						  .append(createdBy.getLastName()).append("\n")
						  .append("👥 *Assigned to:* ").append(assignedTo != null ? 
							  assignedTo.getFirstName() + " " + assignedTo.getLastName() : "Unassigned").append("\n")
						  .append("📅 *Created at:* ").append(task.getCreatedAt()).append("\n");
				
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText(messageText.toString());
				message.enableMarkdown(true);
				
				// Add back button
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				KeyboardRow row = new KeyboardRow();
				row.add("↩️ Back to Main Menu");
				keyboardRows.add(row);
				keyboard.setKeyboard(keyboardRows);
				keyboard.setResizeKeyboard(true);
				message.setReplyMarkup(keyboard);
				
				try {
					execute(message);
				} catch (TelegramApiException e) {
					logger.error("Error sending task details message", e);
				}
			} else {
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("❌ Task not found. Please enter a valid task ID.");
				try {
					execute(message);
				} catch (TelegramApiException e) {
					logger.error("Error sending task not found message", e);
				}
			}
		} catch (NumberFormatException e) {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("❌ Invalid task ID format. Please enter a valid task ID.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending invalid ID format message", ex);
			}
		} catch (Exception e) {
			logger.error("Error showing task details", e);
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("❌ An error occurred while showing task details. Please try again.");
			try {
				execute(message);
			} catch (TelegramApiException ex) {
				logger.error("Error sending error message", ex);
			}
		}
	}

	/**
	 * Returns the bot's username
	 * @return String - Bot's username
	 */
	@Override
	public String getBotUsername() {
		return botName;
	}

	/**
	 * Shows all sprints for the current project
	 * @param chatId - Telegram chat ID of the user
	 */
	private void handleViewSprints(long chatId) {
		try {
			// Get the user's project ID
			UserModel user = userService.findUserByChatId(chatId);
			if (user != null) {
				// Get all sprints for the project
				List<SprintModel> sprints = sprintService.findByProjectId(user.getProjectId());
				
				if (sprints.isEmpty()) {
					SendMessage message = new SendMessage();
					message.setChatId(chatId);
					message.setText("📭 *No Sprints Found*\n\n" +
								  "There are no sprints in your project yet.\n" +
								  "Use the \"Create Sprint\" option to add new sprints.");
					message.enableMarkdown(true);
					execute(message);
					return;
				}

				// Create keyboard with back button
				ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboardRows = new ArrayList<>();
				KeyboardRow row = new KeyboardRow();
				row.add("↩️ Back to Main Menu");
				keyboardRows.add(row);
				keyboard.setKeyboard(keyboardRows);
				keyboard.setResizeKeyboard(true);

				// Format the message with all sprints
				StringBuilder messageText = new StringBuilder();
				messageText.append("📋 *All Sprints*\n\n");
				
				for (SprintModel sprint : sprints) {
					messageText.append("🔹 *Sprint:* ").append(sprint.getName()).append("\n")
							  .append("📝 *Description:* ").append(sprint.getDescription() != null ? sprint.getDescription() : "No description").append("\n")
							  .append("📅 *Start Date:* ").append(sprint.getStartedAt()).append("\n")
							  .append("📅 *End Date:* ").append(sprint.getEndsAt()).append("\n\n");
				}

				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText(messageText.toString());
				message.setReplyMarkup(keyboard);
				message.enableMarkdown(true);
				execute(message);
			}
		} catch (TelegramApiException e) {
			logger.error("Error showing sprints", e);
		}
	}
}