package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("🏠 Show Main Screen"), 
	HIDE_MAIN_SCREEN("❌ Hide Main Screen"),
	VIEW_TASKS("📋 View Tasks"), 
	ADD_TASK("➕ Add Task"),
	UPDATE_TASK("✏️ Update Task"),
	DETAILS("ℹ️ Details"),
	FILTER_TASKS("🔍 Filter Tasks"),
	DONE("✅ Done"),
	//UNDO("UNDO"),
	IN_PROGRESS("🔄 In Progress"),
	READY("🚀 Ready"),
	DELETE_TASK("🗑️ Delete Task"),
	MY_TASKS("👤 My Tasks"),
	CREATE_SPRINT("📅 Create Sprint"),
	UPDATE_SPRINT("�� Update Sprint"),
	DELETE_SPRINT("🗑️ Delete Sprint"),
	SPRINT("📅 Sprint"),
	STATUS("📊 Status"),
	CATEGORY("🏷️ Category"),
	USER("👤 User"),
	HELP("❓ Help"),
	DASH("-");


	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
