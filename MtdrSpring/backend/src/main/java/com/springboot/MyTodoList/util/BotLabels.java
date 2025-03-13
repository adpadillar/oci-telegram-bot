package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	VIEW_TASKS("View Tasks"), 
	ADD_TASK("Add Task"),
	UPDATE_TASK("Update Task"),
	DETAILS("Details"),
	FILTER_TASKS("Filter Tasks"),
	DONE("DONE"),
	//UNDO("UNDO"),
	IN_PROGRESS("IN PROGRESS"),
	READY("READY"),
	DELETE_TASK("Delete Task"),
	MY_TASKS("MY TASKS"),
	CREATE_SPRINT("Create Sprint"),
	DELETE_SPRINT("Delete Sprint"),
	SPRINT("Sprint"),
	STATUS("Status"),
	CATEGORY("Category"),
	USER("User"),
	DASH("-");


	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
