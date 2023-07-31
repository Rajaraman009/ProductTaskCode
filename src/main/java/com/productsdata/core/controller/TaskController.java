package com.productsdata.core.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mysql.cj.protocol.Message;
import com.productsdata.core.entity.AssignedTask;
import com.productsdata.core.entity.Tasks;
import com.productsdata.core.entity.User;
import com.productsdata.core.enumfolder.Priority;
import com.productsdata.core.enumfolder.TaskStatus;
import com.productsdata.core.model.CreateTask;
import com.productsdata.core.model.CreateUser;
import com.productsdata.core.model.MessageResponse;
import com.productsdata.core.model.Progress;
import com.productsdata.core.model.TaskComparator;
import com.productsdata.core.model.UserIdModel;
import com.productsdata.core.repository.AssignedTaskRepository;
import com.productsdata.core.repository.TasksRepository;
import com.productsdata.core.repository.UserRepository;

@RestController
public class TaskController {
	Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private Queue<Tasks> taskQueue = new PriorityQueue<>(new TaskComparator());

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignedTaskRepository assignedTaskRepository;

    // Get all tasks
    @GetMapping("/api/tasks")
    public ResponseEntity<List<Tasks>> getAllTasks() {
        List<Tasks> tasksList = tasksRepository.findAll();
        return ResponseEntity.ok(tasksList);
    }

    // Create a new task
    @PostMapping("/api/tasks")
    public ResponseEntity<Tasks> createTask(@RequestBody CreateTask createTask) {
        Tasks task = new Tasks();
        task.setTitle(createTask.getTitle());
        task.setDescription(createTask.getDescription());
        task.setDueDate(convertLocalDateToDate(createTask.getDueDate()));
        task.setTaskStatus(TaskStatus.OPEN);
        task.setPriority(Priority.LOW);

        taskQueue.add(task); // Add the task to the priority-based task queue
        task = tasksRepository.save(task); // Save the new task in the repository

        return ResponseEntity.ok(task);
    }

    // Create a new user
    @PostMapping("/api/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUser createUser) {
        User user = new User();
        user.setUsername(createUser.getUsername());
        user.setEmail(createUser.getEmail());

        user = userRepository.save(user); // Save the new user in the repository

        return ResponseEntity.ok(user);
    }

    // Update a task
    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<MessageResponse> updateTask(@PathVariable Long id, @RequestBody CreateTask createTask) {
        Tasks task = tasksRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Task not found with ID: " + id));
        }

        task.setTitle(createTask.getTitle());
        task.setDescription(createTask.getDescription());
        task.setDueDate(convertLocalDateToDate(createTask.getDueDate()));
        task.setTaskStatus(TaskStatus.valueOf(createTask.getStatus()));
        if (createTask.getStatus().equalsIgnoreCase(TaskStatus.COMPLETED.toString())) {
            task.setCompletedDate(new Date());
        }

        task = tasksRepository.save(task);
        return ResponseEntity.ok(new MessageResponse(gson.toJson(task)));
    }

    // Delete a task
    @DeleteMapping("/api/tasks/{taskId}")
    public ResponseEntity<MessageResponse> deleteTask(@PathVariable Long taskId) {
        Tasks task = tasksRepository.findById(taskId).orElse(null);

        if (task == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("No Task  found with ID: " + taskId));
        }

        tasksRepository.delete(task);
        return ResponseEntity.ok(new MessageResponse("Your Task Deleted Successfully"));
    }

    // Assign a task to a user
    @PostMapping("/api/tasks/{taskId}/assign")
    public ResponseEntity<MessageResponse> assignTask(@PathVariable Long taskId, @RequestBody UserIdModel userId) {
        Tasks task = tasksRepository.findById(taskId).orElse(null);
        User user = userRepository.findById(userId.getUserID()).orElse(null);

        if (task == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("There was no Task not found with ID: " + taskId));
        }

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found with ID: " + userId.getUserID()));
        }

        AssignedTask assignedTask = new AssignedTask();
        assignedTask.setTask(task);
        assignedTask.setUser(user);

        assignedTask = assignedTaskRepository.save(assignedTask);
        return ResponseEntity.ok(new MessageResponse(gson.toJson(assignedTask)));
    }

    // Get tasks assigned to a specific user
    @GetMapping("/api/users/{userId}/tasks")
    public ResponseEntity<MessageResponse> userSpecificTasks(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found with ID: " + userId));
        }

        List<Tasks> tasksList = user.getAssignedTasks().stream()
                .map(AssignedTask::getTask)
                .collect(Collectors.toList());

        if (tasksList.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("No tasks assigned to this user."));
        }

        return ResponseEntity.ok(new MessageResponse(gson.toJson(tasksList)));
    }

    // Update task progress
    @PutMapping("/api/tasks/{taskId}/progress")
    public ResponseEntity<MessageResponse> updateTaskProgress(@PathVariable Long taskId, @RequestBody Progress progress) {
        Tasks task = tasksRepository.findById(taskId).orElse(null);

        if (task == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("No Task  with ID: " + taskId));
        }

        // Validate progress percentage (0-100)
        if (progress.getProgress() < 0 || progress.getProgress() > 100) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid progress percentage. Progress should be between 0 and 100."));
        }

        task.setProgress(progress.getProgress());
        task = tasksRepository.save(task);
        return ResponseEntity.ok(new MessageResponse(gson.toJson(task)));
    }

    // Get overdue tasks
    @GetMapping("/api/tasks/overdue")
    public ResponseEntity<MessageResponse> getDelayedTasks() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Tasks> overdueTasks = tasksRepository.findAll()
                .stream()
                .filter(task -> task.getDueDate().before(convertLocalDateTimeToDate(currentDateTime)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new MessageResponse(gson.toJson(overdueTasks)));
    }

    
    @GetMapping("/api/tasks/status/{status}")
    public ResponseEntity<MessageResponse> getSpecificStatus(@PathVariable String status){
    	TaskStatus requestedStatus;
    	 List<Tasks> specificTasks = new ArrayList<Tasks>();
        try {
            // Parse the status string to an enum value
            requestedStatus = TaskStatus.valueOf(status.toUpperCase());
    	
            specificTasks    = tasksRepository.findAll().stream()
                    .filter(task -> task.getTaskStatus() == requestedStatus)
                    .collect(Collectors.toList());

    	
            
        }catch(Exception ex) {
        	
        }
        return ResponseEntity.ok(new MessageResponse(gson.toJson(specificTasks)));
    }
    
    
    
    
    // Get completed tasks by date range
    @GetMapping("/api/tasks/completed")
    public ResponseEntity<MessageResponse> getCompletedTasksByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Date startDateAsDate = convertLocalDateToDate(startDate);
        Date endDateAsDate = convertLocalDateToDate(endDate);

        List<Tasks> tasksList = tasksRepository.findByTaskStatusAndCompletedDateBetween(TaskStatus.COMPLETED, startDateAsDate, endDateAsDate);

        if (tasksList.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("No completed tasks found within the specified date range."));
        }

        return ResponseEntity.ok(new MessageResponse(gson.toJson(tasksList)));
    }

    // Get task statistics
    @GetMapping("/api/tasks/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        List<Tasks> allTasks = tasksRepository.findAll();
        int totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(task -> task.getTaskStatus() == TaskStatus.COMPLETED)
                .count();
        double completedPercentage = (completedTasks * 100.0) / totalTasks;

        Map<String, Object> statistics = Map.of(
                "totalTasks", totalTasks,
                "completedTasks", completedTasks,
                "completedPercentage", completedPercentage
        );

        return ResponseEntity.ok(statistics);
    }

    // Utility method to convert LocalDate to Date
    private static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Utility method to convert LocalDateTime to Date
    private static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
