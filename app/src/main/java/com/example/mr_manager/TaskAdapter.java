package com.example.mr_manager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

    private final Context context;
    private final List<Task> taskList;
    private final LayoutInflater layoutInflater;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.layoutInflater = LayoutInflater.from(context);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.item_task,
                    parent,
                    false
            );

            viewHolder = new ViewHolder();

            viewHolder.viewTaskColor =
                    convertView.findViewById(R.id.viewTaskColor);

            viewHolder.textTaskTitle =
                    convertView.findViewById(R.id.textTaskTitle);

            viewHolder.textTaskId =
                    convertView.findViewById(R.id.textTaskId);

            viewHolder.textTaskDescription =
                    convertView.findViewById(R.id.textTaskDescription);

            viewHolder.textTaskPriority =
                    convertView.findViewById(R.id.textTaskPriority);

            viewHolder.textTaskStatus =
                    convertView.findViewById(R.id.textTaskStatus);

            viewHolder.textTaskAssignedPerson =
                    convertView.findViewById(R.id.textTaskAssignedPerson);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Task task = getItem(position);

        viewHolder.textTaskTitle.setText(
                getDisplayText(task.getTitle(), "Untitled task")
        );

        viewHolder.textTaskId.setText(
                "Task ID: " + getDisplayText(task.getTaskId(), "Not available")
        );

        viewHolder.textTaskDescription.setText(
                getDisplayText(task.getDescription(), "No description")
        );

        viewHolder.textTaskPriority.setText(
                "Priority: " + task.getPriority() + "/10"
        );

        viewHolder.textTaskStatus.setText(
                "Status: " + getDisplayText(task.getStatus(), "Not specified")
        );

        viewHolder.textTaskAssignedPerson.setText(
                "Assigned to: "
                        + getDisplayText(
                        task.getAssignedPerson(),
                        "Unassigned"
                )
        );

        applyTaskColor(viewHolder.viewTaskColor, task.getColor());

        return convertView;
    }

    private String getDisplayText(String value, String fallbackText) {
        if (value == null || value.trim().isEmpty()) {
            return fallbackText;
        }

        return value;
    }

    private void applyTaskColor(View colorView, String colorValue) {
        int fallbackColor = Color.GRAY;

        if (colorValue == null || colorValue.trim().isEmpty()) {
            colorView.setBackgroundColor(fallbackColor);
            return;
        }

        try {
            int parsedColor = Color.parseColor(colorValue);
            colorView.setBackgroundColor(parsedColor);

        } catch (IllegalArgumentException exception) {
            colorView.setBackgroundColor(fallbackColor);
        }
    }

    private static class ViewHolder {
        View viewTaskColor;
        TextView textTaskTitle;
        TextView textTaskId;
        TextView textTaskDescription;
        TextView textTaskPriority;
        TextView textTaskStatus;
        TextView textTaskAssignedPerson;
    }
}