package com.example.mr_manager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateTaskActivity extends AppCompatActivity {

    private TextInputEditText editTaskTitle;
    private TextInputEditText editTaskDescription;
    private TextInputEditText editTaskPriority;
    private TextInputEditText editTaskColor;
    private TextInputEditText editAssignedPerson;

    private Spinner spinnerTaskStatus;

    private Button buttonCreateTask;
    private Button buttonCancelCreateTask;

    private DatabaseReference tasksReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        tasksReference = FirebaseDatabase.getInstance()
                .getReference("tasks");

        connectViews();
        prepareStatusSpinner();

        buttonCreateTask.setOnClickListener(view -> createTask());
        buttonCancelCreateTask.setOnClickListener(view -> finish());
    }

    private void connectViews() {
        editTaskTitle = findViewById(R.id.editTaskTitle);
        editTaskDescription = findViewById(R.id.editTaskDescription);
        editTaskPriority = findViewById(R.id.editTaskPriority);
        editTaskColor = findViewById(R.id.editTaskColor);
        editAssignedPerson = findViewById(R.id.editAssignedPerson);

        spinnerTaskStatus = findViewById(R.id.spinnerTaskStatus);

        buttonCreateTask = findViewById(R.id.buttonCreateTask);
        buttonCancelCreateTask = findViewById(
                R.id.buttonCancelCreateTask
        );
    }

    private void prepareStatusSpinner() {
        String[] statuses = {
                "Pending",
                "In Progress",
                "Completed"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statuses
                );

        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTaskStatus.setAdapter(statusAdapter);
    }

    private void createTask() {
        String title = getText(editTaskTitle);
        String description = getText(editTaskDescription);
        String priorityText = getText(editTaskPriority);
        String color = getText(editTaskColor);
        String assignedPerson = getText(editAssignedPerson);
        String status = spinnerTaskStatus
                .getSelectedItem()
                .toString();

        if (title.isEmpty()) {
            editTaskTitle.setError("Enter the task title.");
            editTaskTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            editTaskDescription.setError(
                    "Enter the task description."
            );
            editTaskDescription.requestFocus();
            return;
        }

        if (priorityText.isEmpty()) {
            editTaskPriority.setError(
                    "Enter a priority from 1 to 10."
            );
            editTaskPriority.requestFocus();
            return;
        }

        int priority;

        try {
            priority = Integer.parseInt(priorityText);
        } catch (NumberFormatException exception) {
            editTaskPriority.setError(
                    "Priority must be a number."
            );
            editTaskPriority.requestFocus();
            return;
        }

        if (priority < 1 || priority > 10) {
            editTaskPriority.setError(
                    "Priority must be between 1 and 10."
            );
            editTaskPriority.requestFocus();
            return;
        }

        if (color.isEmpty()) {
            editTaskColor.setError(
                    "Enter a task color."
            );
            editTaskColor.requestFocus();
            return;
        }

        try {
            Color.parseColor(color);
        } catch (IllegalArgumentException exception) {
            editTaskColor.setError(
                    "Enter a valid color name or hexadecimal value."
            );
            editTaskColor.requestFocus();
            return;
        }

        if (assignedPerson.isEmpty()) {
            editAssignedPerson.setError(
                    "Enter the assigned person."
            );
            editAssignedPerson.requestFocus();
            return;
        }

        saveTask(
                title,
                description,
                priority,
                color,
                assignedPerson,
                status
        );
    }

    private void saveTask(
            String title,
            String description,
            int priority,
            String color,
            String assignedPerson,
            String status
    ) {
        String taskId = tasksReference.push().getKey();

        if (taskId == null) {
            Toast.makeText(
                    this,
                    "Could not create a task ID.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Task task = new Task();

        task.setTaskId(taskId);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setColor(color);
        task.setAssignedPerson(assignedPerson);
        task.setStatus(status);

        setFormEnabled(false);

        tasksReference.child(taskId)
                .setValue(
                        task,
                        new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(
                                    DatabaseError error,
                                    @NonNull DatabaseReference reference
                            ) {
                                setFormEnabled(true);

                                if (error != null) {
                                    Toast.makeText(
                                            CreateTaskActivity.this,
                                            "Task could not be created: "
                                                    + error.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                Toast.makeText(
                                        CreateTaskActivity.this,
                                        "Task created successfully.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            }
                        }
                );
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText()
                .toString()
                .trim();
    }
    private void setFormEnabled(boolean enabled) {
        editTaskTitle.setEnabled(enabled);
        editTaskDescription.setEnabled(enabled);
        editTaskPriority.setEnabled(enabled);
        editTaskColor.setEnabled(enabled);
        editAssignedPerson.setEnabled(enabled);
        spinnerTaskStatus.setEnabled(enabled);
        buttonCreateTask.setEnabled(enabled);
        buttonCancelCreateTask.setEnabled(enabled);

        buttonCreateTask.setText(
                enabled ? "Create Task" : "Creating..."
        );
    }
}