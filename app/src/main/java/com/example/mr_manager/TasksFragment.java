package com.example.mr_manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private ListView listTasks;
    private TextView textNoTasks;
    private ProgressBar progressTasks;

    private List<Task> taskList;
    private TaskAdapter taskAdapter;

    private DatabaseReference tasksReference;
    private ValueEventListener tasksListener;

    public TasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_tasks,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        listTasks = view.findViewById(R.id.listTasks);
        textNoTasks = view.findViewById(R.id.textNoTasks);
        progressTasks = view.findViewById(R.id.progressTasks);

        taskList = new ArrayList<>();

        taskAdapter = new TaskAdapter(
                requireContext(),
                taskList
        );

        listTasks.setAdapter(taskAdapter);

        tasksReference = FirebaseDatabase
                .getInstance()
                .getReference("tasks");

        loadTasks();
    }

    private void loadTasks() {
        progressTasks.setVisibility(View.VISIBLE);
        listTasks.setVisibility(View.GONE);
        textNoTasks.setVisibility(View.GONE);

        tasksListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);

                    if (task != null) {
                        if (task.getTaskId() == null
                                || task.getTaskId().trim().isEmpty()) {

                            task.setTaskId(taskSnapshot.getKey());
                        }

                        taskList.add(task);
                    }
                }

                taskAdapter.notifyDataSetChanged();
                updateTasksDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressTasks.setVisibility(View.GONE);
                listTasks.setVisibility(View.GONE);

                textNoTasks.setText("Could not load tasks.");
                textNoTasks.setVisibility(View.VISIBLE);

                if (isAdded()) {
                    Toast.makeText(
                            requireContext(),
                            "Failed to load tasks: " + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        };

        tasksReference.addValueEventListener(tasksListener);
    }

    private void updateTasksDisplay() {
        progressTasks.setVisibility(View.GONE);

        if (taskList.isEmpty()) {
            listTasks.setVisibility(View.GONE);

            textNoTasks.setText("No tasks are available.");
            textNoTasks.setVisibility(View.VISIBLE);
        } else {
            listTasks.setVisibility(View.VISIBLE);
            textNoTasks.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (tasksReference != null && tasksListener != null) {
            tasksReference.removeEventListener(tasksListener);
        }

        listTasks = null;
        textNoTasks = null;
        progressTasks = null;
        taskAdapter = null;
    }
}