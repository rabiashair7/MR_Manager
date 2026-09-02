package com.example.mr_manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private Button buttonOpenCreateTask;

    private ListView listAdminTasks;
    private ProgressBar progressAdminTasks;
    private TextView textNoAdminTasks;

    private List<Task> taskList;
    private AdminTaskAdapter taskAdapter;

    private DatabaseReference tasksReference;
    private ValueEventListener tasksListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        connectViews();
        prepareTaskList();
        prepareFirebase();
        prepareClickListeners();
        loadTasks();
    }

    private void connectViews() {
        buttonBack = findViewById(R.id.buttonAdminBack);

        buttonOpenCreateTask = findViewById(
                R.id.buttonOpenCreateTask
        );

        listAdminTasks = findViewById(R.id.listAdminTasks);

        progressAdminTasks = findViewById(
                R.id.progressAdminTasks
        );

        textNoAdminTasks = findViewById(
                R.id.textNoAdminTasks
        );
    }

    private void prepareTaskList() {
        taskList = new ArrayList<>();

        taskAdapter = new AdminTaskAdapter(
                this,
                taskList,
                new AdminTaskAdapter.OnEditTaskClickListener() {
                    @Override
                    public void onEditTaskClick(Task task) {
                        openEditTask(task);
                    }
                },
                new AdminTaskAdapter.OnDeleteTaskClickListener() {
                    @Override
                    public void onDeleteTaskClick(Task task) {
                        showDeleteConfirmation(task);
                    }
                }
        );

        listAdminTasks.setAdapter(taskAdapter);
    }

    private void prepareFirebase() {
        tasksReference = FirebaseDatabase.getInstance()
                .getReference("tasks");
    }

    private void prepareClickListeners() {
        buttonBack.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }
        );

        buttonOpenCreateTask.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent createTaskIntent = new Intent(
                                AdminActivity.this,
                                CreateTaskActivity.class
                        );

                        startActivity(createTaskIntent);
                    }
                }
        );
    }

    private void openEditTask(Task task) {
        if (!hasValidTaskId(task)) {
            showInvalidTaskMessage();
            return;
        }

        Intent editTaskIntent = new Intent(
                AdminActivity.this,
                EditTaskActivity.class
        );

        editTaskIntent.putExtra(
                EditTaskActivity.EXTRA_TASK_ID,
                task.getTaskId()
        );

        startActivity(editTaskIntent);
    }

    private void showDeleteConfirmation(Task task) {
        if (!hasValidTaskId(task)) {
            showInvalidTaskMessage();
            return;
        }

        String taskTitle = task.getTitle();

        if (taskTitle == null || taskTitle.trim().isEmpty()) {
            taskTitle = "this task";
        } else {
            taskTitle = "\"" + taskTitle.trim() + "\"";
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage(
                        "Are you sure you want to delete "
                                + taskTitle
                                + "? This action cannot be undone."
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteTask(task)
                )
                .setNegativeButton(
                        "Cancel",
                        (dialog, which) -> dialog.dismiss()
                )
                .setCancelable(false)
                .show();
    }

    private void deleteTask(Task task) {
        if (!hasValidTaskId(task)) {
            showInvalidTaskMessage();
            return;
        }

        tasksReference
                .child(task.getTaskId())
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            AdminActivity.this,
                            "Task deleted successfully.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(
                            AdminActivity.this,
                            exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private boolean hasValidTaskId(Task task) {
        return task != null
                && task.getTaskId() != null
                && !task.getTaskId().trim().isEmpty();
    }

    private void showInvalidTaskMessage() {
        Toast.makeText(
                this,
                "The selected task could not be identified.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void loadTasks() {
        showLoadingState();

        tasksListener = new ValueEventListener() {
            @Override
            public void onDataChange(
                    @NonNull DataSnapshot snapshot
            ) {
                taskList.clear();

                for (DataSnapshot taskSnapshot
                        : snapshot.getChildren()) {

                    Task task = taskSnapshot.getValue(
                            Task.class
                    );

                    if (task == null) {
                        continue;
                    }

                    if (task.getTaskId() == null
                            || task.getTaskId()
                            .trim()
                            .isEmpty()) {

                        task.setTaskId(
                                taskSnapshot.getKey()
                        );
                    }

                    taskList.add(task);
                }

                sortTasksByPriority();
                taskAdapter.notifyDataSetChanged();

                if (taskList.isEmpty()) {
                    showEmptyState(
                            "No tasks have been created yet."
                    );
                } else {
                    showTaskList();
                }
            }

            @Override
            public void onCancelled(
                    @NonNull DatabaseError error
            ) {
                showEmptyState(
                        "Could not load the task list."
                );

                Toast.makeText(
                        AdminActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        tasksReference.addValueEventListener(tasksListener);
    }

    private void sortTasksByPriority() {
        Collections.sort(
                taskList,
                new Comparator<Task>() {
                    @Override
                    public int compare(
                            Task firstTask,
                            Task secondTask
                    ) {
                        return Integer.compare(
                                secondTask.getPriority(),
                                firstTask.getPriority()
                        );
                    }
                }
        );
    }

    private void showLoadingState() {
        progressAdminTasks.setVisibility(View.VISIBLE);
        listAdminTasks.setVisibility(View.GONE);
        textNoAdminTasks.setVisibility(View.GONE);
    }

    private void showTaskList() {
        progressAdminTasks.setVisibility(View.GONE);
        listAdminTasks.setVisibility(View.VISIBLE);
        textNoAdminTasks.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        progressAdminTasks.setVisibility(View.GONE);
        listAdminTasks.setVisibility(View.GONE);

        textNoAdminTasks.setText(message);
        textNoAdminTasks.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tasksReference != null && tasksListener != null) {
            tasksReference.removeEventListener(tasksListener);
        }
    }
}