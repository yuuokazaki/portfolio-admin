package com.seattleacademy.team20;

public class SkillCategory {

	private int id;
	private String category;

	public SkillCategory(int id, String category) {
		this.id = id;
		this.category = category;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}