package com.example.mr_manager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "taskId";

    private EditText editTaskTitle;
    private EditText editTaskDescription;
    private EditText editTaskPriority;
    private EditText editTaskColor;
    private EditText editAssignedPerson;
    private Spinner spinnerTaskStatus;
    private Button buttonUpdateTask;
    private Button buttonCancel;

    private DatabaseReference taskReference;

    private String taskId;

    private final String[] taskStatuses = {
            "Pending",
            "In Progress",
            "Completed"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        connectViews();
        prepareStatusSpinner();
        prepareTaskReference();
        prepareClickListeners();
        loadTask();
    }

    private void connectViews() {
        editTaskTitle = findViewById(
                R.id.editUpdateTaskTitle
        );

        editTaskDescription = findViewById(
                R.id.editUpdateTaskDescription
        );

        editTaskPriority = findViewById(
                R.id.editUpdateTaskPriority
        );

        editTaskColor = findViewById(
                R.id.editUpdateTaskColor
        );

        editAssignedPerson = findViewById(
                R.id.editUpdateAssignedPerson
        );

        spinnerTaskStatus = findViewById(
                R.id.spinnerUpdateTaskStatus
        );

        buttonUpdateTask = findViewById(
                R.id.buttonUpdateTask
        );

        buttonCancel = findViewById(
                R.id.buttonCancelUpdateTask
        );
    }

    private void prepareStatusSpinner() {
        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        taskStatuses
                );

        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTaskStatus.setAdapter(statusAdapter);
    }

    private void prepareTaskReference() {
        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);

        if (taskId == null || taskId.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "The selected task could not be identified.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        taskReference = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(taskId);
    }

    private void prepareClickListeners() {
        buttonUpdateTask.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validateAndUpdateTask();
                    }
                }
        );

        buttonCancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }
        );
    }

    private void loadTask() {
        if (taskReference == null) {
            return;
        }

        setFormEnabled(false);
        buttonUpdateTask.setText("Loading...");

        taskReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {
                        Task task = snapshot.getValue(Task.class);

                        if (task == null) {
                            Toast.makeText(
                                    EditTaskActivity.this,
                                    "The selected task no longer exists.",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                            return;
                        }

                        displayTask(task);
                        setFormEnabled(true);
                        buttonUpdateTask.setText("Update Task");
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {
                        setFormEnabled(true);
                        buttonUpdateTask.setText("Update Task");

                        Toast.makeText(
                                EditTaskActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void displayTask(Task task) {
        editTaskTitle.setText(safeText(task.getTitle()));

        editTaskDescription.setText(
                safeText(task.getDescription())
        );

        editTaskPriority.setText(
                String.valueOf(task.getPriority())
        );

        editTaskColor.setText(safeText(task.getColor()));

        editAssignedPerson.setText(
                safeText(task.getAssignedPerson())
        );

        selectTaskStatus(task.getStatus());
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }

    private void selectTaskStatus(String status) {
        if (status == null) {
            spinnerTaskStatus.setSelection(0);
            return;
        }

        for (int index = 0;
             index < taskStatuses.length;
             index++) {

            if (taskStatuses[index].equals(status)) {
                spinnerTaskStatus.setSelection(index);
                return;
            }
        }

        spinnerTaskStatus.setSelection(0);
    }

    private void validateAndUpdateTask() {
        String title = editTaskTitle.getText()
                .toString()
                .trim();

        String description = editTaskDescription.getText()
                .toString()
                .trim();

        String priorityText = editTaskPriority.getText()
                .toString()
                .trim();

        String color = editTaskColor.getText()
                .toString()
                .trim();

        String assignedPerson = editAssignedPerson.getText()
                .toString()
                .trim();

        String status = spinnerTaskStatus
                .getSelectedItem()
                .toString();

        if (title.isEmpty()) {
            editTaskTitle.setError(
                    "Please enter a task title."
            );

            editTaskTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            editTaskDescription.setError(
                    "Please enter a task description."
            );

            editTaskDescription.requestFocus();
            return;
        }

        if (priorityText.isEmpty()) {
            editTaskPriority.setError(
                    "Please enter a priority."
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
                    "Please enter a task color."
            );

            editTaskColor.requestFocus();
            return;
        }

        try {
            Color.parseColor(color);
        } catch (IllegalArgumentException exception) {
            editTaskColor.setError(
                    "Enter a valid color name or hexadecimal color."
            );

            editTaskColor.requestFocus();
            return;
        }

        if (assignedPerson.isEmpty()) {
            editAssignedPerson.setError(
                    "Please enter the assigned person."
            );

            editAssignedPerson.requestFocus();
            return;
        }

        updateTask(
                title,
                description,
                priority,
                color,
                assignedPerson,
                status
        );
    }

    private void updateTask(
            String title,
            String description,
            int priority,
            String color,
            String assignedPerson,
            String status
    ) {
        if (taskReference == null) {
            return;
        }

        Task updatedTask = new Task();

        updatedTask.setTaskId(taskId);
        updatedTask.setTitle(title);
        updatedTask.setDescription(description);
        updatedTask.setPriority(priority);
        updatedTask.setColor(color);
        updatedTask.setAssignedPerson(assignedPerson);
        updatedTask.setStatus(status);

        setFormEnabled(false);
        buttonUpdateTask.setText("Updating...");

        taskReference.setValue(updatedTask)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            EditTaskActivity.this,
                            "Task updated successfully.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(exception -> {
                    setFormEnabled(true);
                    buttonUpdateTask.setText("Update Task");

                    Toast.makeText(
                            EditTaskActivity.this,
                            exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void setFormEnabled(boolean enabled) {
        editTaskTitle.setEnabled(enabled);
        editTaskDescription.setEnabled(enabled);
        editTaskPriority.setEnabled(enabled);
        editTaskColor.setEnabled(enabled);
        editAssignedPerson.setEnabled(enabled);
        spinnerTaskStatus.setEnabled(enabled);
        buttonUpdateTask.setEnabled(enabled);
        buttonCancel.setEnabled(enabled);
    }
}