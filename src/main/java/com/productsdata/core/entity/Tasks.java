package com.productsdata.core.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.productsdata.core.enumfolder.Priority;
import com.productsdata.core.enumfolder.TaskStatus;


@Entity
@Table(name = "tasks")
public class Tasks {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long taskId;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "due_date")
	private Date dueDate;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private TaskStatus taskStatus;
	
	@Column(name = "completed_date")
	private Date completedDate;
	
	@Column(name = "progress")
	private Integer progress;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "priority")
	 private Priority priority;

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Date getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	
	
	
}
