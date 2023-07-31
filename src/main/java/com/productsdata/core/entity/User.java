package com.productsdata.core.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    @Column(name="username")
    private String username;
    
    @Column(name="email")
    private String email;
    
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<AssignedTask> assignedTasks;


	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<AssignedTask> getAssignedTasks() {
		return assignedTasks;
	}

	public void setAssignedTasks(List<AssignedTask> assignedTasks) {
		this.assignedTasks = assignedTasks;
	}

    
}