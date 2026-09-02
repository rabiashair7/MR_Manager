package com.example.mr_manager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class AdminTaskAdapter extends BaseAdapter {

    public interface OnEditTaskClickListener {
        void onEditTaskClick(Task task);
    }

    public interface OnDeleteTaskClickListener {
        void onDeleteTaskClick(Task task);
    }

    private final List<Task> taskList;
    private final LayoutInflater layoutInflater;

    private final OnEditTaskClickListener editTaskClickListener;
    private final OnDeleteTaskClickListener deleteTaskClickListener;

    public AdminTaskAdapter(
            Context context,
            List<Task> taskList,
            OnEditTaskClickListener editTaskClickListener,
            OnDeleteTaskClickListener deleteTaskClickListener
    ) {
        this.taskList = taskList;
        this.layoutInflater = LayoutInflater.from(context);
        this.editTaskClickListener = editTaskClickListener;
        this.deleteTaskClickListener = deleteTaskClickListener;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Task getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent
    ) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.item_admin_task,
                    parent,
                    false
            );

            viewHolder = new ViewHolder();

            viewHolder.viewTaskColor = convertView.findViewById(
                    R.id.viewAdminTaskColor
            );

            viewHolder.textTaskTitle = convertView.findViewById(
                    R.id.textAdminTaskTitle
            );

            viewHolder.textTaskId = convertView.findViewById(
                    R.id.textAdminTaskId
            );

            viewHolder.textTaskDescription = convertView.findViewById(
                    R.id.textAdminTaskDescription
            );

            viewHolder.textTaskPriority = convertView.findViewById(
                    R.id.textAdminTaskPriority
            );

            viewHolder.textTaskStatus = convertView.findViewById(
                    R.id.textAdminTaskStatus
            );

            viewHolder.textAssignedPerson = convertView.findViewById(
                    R.id.textAdminAssignedPerson
            );

            viewHolder.buttonEdit = convertView.findViewById(
                    R.id.buttonEditAdminTask
            );

            viewHolder.buttonDelete = convertView.findViewById(
                    R.id.buttonDeleteAdminTask
            );

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Task task = getItem(position);

        viewHolder.textTaskTitle.setText(
                valueOrDefault(task.getTitle(), "Untitled task")
        );

        viewHolder.textTaskId.setText(
                "Task ID: "
                        + valueOrDefault(task.getTaskId(), "Unknown")
        );

        viewHolder.textTaskDescription.setText(
                valueOrDefault(
                        task.getDescription(),
                        "No description"
                )
        );

        viewHolder.textTaskPriority.setText(
                "Priority: " + task.getPriority()
        );

        viewHolder.textTaskStatus.setText(
                "Status: "
                        + valueOrDefault(
                        task.getStatus(),
                        "Unknown"
                )
        );

        viewHolder.textAssignedPerson.setText(
                "Assigned to: "
                        + valueOrDefault(
                        task.getAssignedPerson(),
                        "Nobody"
                )
        );

        setTaskColor(
                viewHolder.viewTaskColor,
                task.getColor()
        );

        viewHolder.buttonEdit.setEnabled(true);

        viewHolder.buttonEdit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editTaskClickListener != null) {
                            editTaskClickListener.onEditTaskClick(task);
                        }
                    }
                }
        );

        viewHolder.buttonDelete.setEnabled(true);

        viewHolder.buttonDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (deleteTaskClickListener != null) {
                            deleteTaskClickListener.onDeleteTaskClick(task);
                        }
                    }
                }
        );

        return convertView;
    }

    private void setTaskColor(
            View colorView,
            String colorValue
    ) {
        if (colorValue == null || colorValue.trim().isEmpty()) {
            colorView.setBackgroundColor(Color.GRAY);
            return;
        }

        try {
            colorView.setBackgroundColor(
                    Color.parseColor(colorValue.trim())
            );
        } catch (IllegalArgumentException exception) {
            colorView.setBackgroundColor(Color.GRAY);
        }
    }

    private String valueOrDefault(
            String value,
            String defaultValue
    ) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }

    private static class ViewHolder {
        private View viewTaskColor;
        private TextView textTaskTitle;
        private TextView textTaskId;
        private TextView textTaskDescription;
        private TextView textTaskPriority;
        private TextView textTaskStatus;
        private TextView textAssignedPerson;
        private Button buttonEdit;
        private Button buttonDelete;
    }
}