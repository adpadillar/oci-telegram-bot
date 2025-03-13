package com.springboot.MyTodoList.util;

import com.springboot.MyTodoList.MyTodoListApplication;

public enum BotCommands {

	START_COMMAND("/start"), 
	HIDE_COMMAND("/hide"), 
	VIEW_TASKS("/viewtasks"),
	ADD_TASK("/addtask"),
	MY_TASKS("/mytasks"),
	DELETE_TASK("/deletetask"),
	UPDATE_TASK("/updatetask"),
	CREATE_SPRINT("/createsprint"),
	DELETE_SPRINT("/deletesprint"),;

	private String command;

	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	public String getCommand() {
		return command;
	}
}
