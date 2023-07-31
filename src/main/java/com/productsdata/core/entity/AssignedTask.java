package com.productsdata.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "assignedtasks")
public class AssignedTask {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long assignedTaskId;

	@ManyToOne
	@JoinColumn(name = "userId")
	private User user;

	@ManyToOne
	@JoinColumn(name = "taskId")
	private Tasks task;

	public Long getAssignedTaskId() {
		return assignedTaskId;
	}

	public void setAssignedTaskId(Long assignedTaskId) {
		this.assignedTaskId = assignedTaskId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Tasks getTask() {
		return task;
	}

	public void setTask(Tasks task) {
		this.task = task;
	}

	
}
