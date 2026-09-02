package com.example.mr_manager;

public class Task {

    private String taskId;
    private String title;
    private String description;
    private int priority;
    private String color;
    private String assignedPerson;
    private String status;

    public Task() {
        // Required empty constructor for Firebase
    }

    public Task(String taskId,
                String title,
                String description,
                int priority,
                String color,
                String assignedPerson,
                String status) {

        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.color = color;
        this.assignedPerson = assignedPerson;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAssignedPerson() {
        return assignedPerson;
    }

    public void setAssignedPerson(String assignedPerson) {
        this.assignedPerson = assignedPerson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}