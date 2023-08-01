package com.productsdata.core.controller;

import com.productsdata.core.entity.AssignedTask;
import com.productsdata.core.entity.Tasks;
import com.productsdata.core.entity.User;
import com.productsdata.core.enumfolder.TaskStatus;
import com.productsdata.core.model.CreateTask;
import com.productsdata.core.model.CreateUser;
import com.productsdata.core.repository.TasksRepository;
import com.productsdata.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @InjectMocks
    private TaskController taskController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TasksRepository tasksRepository;

    // Test data
    private Tasks sampleTask;
    private User sampleUser;
    private AssignedTask sampleAssignedTask;

    @Test
    public void testGetAllTasks() {
        // Given
        List<Tasks> tasksList = new ArrayList<>();
        tasksList.add(new Tasks());
        tasksList.add(new Tasks());

        when(tasksRepository.findAll()).thenReturn(tasksList);

        // When
        ResponseEntity<List<Tasks>> responseEntity = taskController.getAllTasks();

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, responseEntity.getBody().size());
    }

    @Test
    public void testCreateTask() {
        // Given
        CreateTask createTask = new CreateTask();
        createTask.setTitle("Test Task");
        createTask.setDescription("Test Description");
        createTask.setDueDate(LocalDate.now());

        when(tasksRepository.save(any(Tasks.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<Tasks> responseEntity = taskController.createTask(createTask);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(createTask.getTitle(), responseEntity.getBody().getTitle());
        assertEquals(createTask.getDescription(), responseEntity.getBody().getDescription());
    }


    @BeforeEach
    public void setup() {
        // Create sample task
        sampleTask = new Tasks();
        sampleTask.setTitle("Sample Task");
        sampleTask.setDescription("This is a sample task");
        sampleTask.setDueDate(new Date());
        sampleTask.setTaskStatus(TaskStatus.INPROGRESS);
        sampleTask.setProgress(50);

        // Create sample user
        sampleUser = new User();
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("testuser@example.com");

        // Create sample assigned task
        sampleAssignedTask = new AssignedTask();
        sampleAssignedTask.setTask(sampleTask);
        sampleAssignedTask.setUser(sampleUser);
    }

    @Test
    public void testCreateUser() {
        // Given
        CreateUser createUser = new CreateUser();
        createUser.setUsername("testuser");
        createUser.setEmail("testuser@example.com");

        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        // When
        ResponseEntity<User> responseEntity = taskController.createUser(createUser);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(sampleUser, responseEntity.getBody());
    }

    //
    @Test
    public void testGetTaskStatistics() {
        // Given
        List<Tasks> allTasks = new ArrayList<>();
        allTasks.add(new Tasks());
        allTasks.add(new Tasks());
        allTasks.add(new Tasks());
        allTasks.add(new Tasks());
        allTasks.get(0).setTaskStatus(TaskStatus.COMPLETED);
        allTasks.get(1).setTaskStatus(TaskStatus.COMPLETED);
        allTasks.get(2).setTaskStatus(TaskStatus.COMPLETED);

        when(tasksRepository.findAll()).thenReturn(allTasks);

        // When
        ResponseEntity<Map<String, Object>> responseEntity = taskController.getTaskStatistics();

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Map<String, Object> statistics = responseEntity.getBody();
        assertEquals(4, statistics.get("totalTasks"));
        assertEquals(3L, statistics.get("completedTasks"));
        assertEquals(75.0, statistics.get("completedPercentage"));
    }
}
